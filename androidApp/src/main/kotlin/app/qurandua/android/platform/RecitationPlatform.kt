package app.qurandua.android.platform

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.CacheWriter
import androidx.media3.datasource.cache.ContentMetadata
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import app.qurandua.android.MainActivity
import app.qurandua.android.R
import app.qurandua.android.ui.DownloadProgress
import app.qurandua.android.ui.RecitationLibrary
import app.qurandua.android.ui.ayahAudioUrl
import app.qurandua.shared.i18n.stringsFor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

private const val DOWNLOAD_CHANNEL_ID = "recitation_download"
private const val DOWNLOAD_NOTIFICATION_ID = 4300
private const val ACTION_CANCEL_DOWNLOAD = "app.qurandua.CANCEL_DOWNLOAD"

/**
 * Recitations kept on the phone for good: every ayah that was played or downloaded is stored
 * in the app's own files (which Android never clears on its own) and is never fetched again.
 * Only "delete saved audio" removes it.
 */
@androidx.annotation.OptIn(UnstableApi::class)
object RecitationStore : RecitationLibrary {
    private lateinit var appContext: Context
    lateinit var cache: SimpleCache
        private set
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null
    @Volatile private var writer: CacheWriter? = null

    private val _savedBytes = MutableStateFlow(0L)
    override val savedBytes: StateFlow<Long> = _savedBytes.asStateFlow()
    private val _savedSurahs = MutableStateFlow<Set<Int>>(emptySet())
    override val savedSurahs: StateFlow<Set<Int>> = _savedSurahs.asStateFlow()
    private val _download = MutableStateFlow<DownloadProgress?>(null)
    override val download: StateFlow<DownloadProgress?> = _download.asStateFlow()
    private var ayahCounts: List<Int> = emptyList()

    @Synchronized
    fun init(context: Context) {
        if (::cache.isInitialized) return
        appContext = context.applicationContext
        // Earlier versions kept a 300 MB cache in cacheDir, which Android may wipe; start clean here.
        File(appContext.cacheDir, "recitations").deleteRecursively()
        cache = SimpleCache(
            File(appContext.filesDir, "recitations"),
            NoOpCacheEvictor(),
            StandaloneDatabaseProvider(appContext),
        )
    }

    /** Reads through the store: what is saved plays offline, what is not is fetched once and saved. */
    fun dataSourceFactory(): CacheDataSource.Factory = CacheDataSource.Factory()
        .setCache(cache)
        .setUpstreamDataSourceFactory(DefaultDataSource.Factory(appContext))
        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

    private fun isSaved(url: String): Boolean {
        val length = ContentMetadata.getContentLength(cache.getContentMetadata(url))
        return length > 0 && cache.isCached(url, 0, length)
    }

    override fun refresh(ayahCounts: List<Int>) {
        if (ayahCounts.isNotEmpty()) this.ayahCounts = ayahCounts
        val counts = this.ayahCounts
        scope.launch {
            _savedBytes.value = cache.cacheSpace
            _savedSurahs.value = counts.indices
                .filter { i -> (1..counts[i]).all { isSaved(ayahAudioUrl(i + 1, it)) } }
                .map { it + 1 }
                .toSet()
        }
    }

    override fun downloadSurah(surah: Int, ayahCount: Int) = start(listOf(surah to ayahCount), all = false)

    override fun downloadAll(ayahCounts: List<Int>) {
        this.ayahCounts = ayahCounts
        start(ayahCounts.mapIndexed { i, n -> (i + 1) to n }, all = true)
    }

    private fun start(surahs: List<Pair<Int, Int>>, all: Boolean) {
        if (job?.isActive == true) return
        val todo = surahs.filter { it.first !in _savedSurahs.value }
        if (todo.isEmpty()) return
        val total = todo.sumOf { it.second }
        _download.value = DownloadProgress(surah = todo.first().first, all = all, done = 0, total = total, failed = 0)
        DownloadService.start(appContext)
        job = scope.launch {
            var done = 0
            var failed = 0
            for ((surah, count) in todo) {
                for (ayah in 1..count) {
                    ensureActive()
                    val url = ayahAudioUrl(surah, ayah)
                    if (!isSaved(url) && !fetch(url)) failed++
                    done++
                    _download.value = DownloadProgress(surah, all, done, total, failed)
                }
            }
        }.also { it.invokeOnCompletion { finish() } }
    }

    /** Downloads one ayah, retrying a flaky connection; a cancelled download stops at once. */
    private fun fetch(url: String): Boolean {
        repeat(3) {
            val w = CacheWriter(dataSourceFactory().createDataSource(), DataSpec(Uri.parse(url)), null, null)
            writer = w
            try {
                w.cache()
                return true
            } catch (e: Exception) {
                if (job?.isCancelled == true) return false
            } finally {
                writer = null
            }
        }
        return false
    }

    private fun finish() {
        _download.value = null
        job = null
        refresh(emptyList())
    }

    override fun cancel() {
        job?.cancel()
        writer?.cancel()
    }

    override fun deleteAll() {
        val running = job
        cancel()
        scope.launch {
            running?.join()
            cache.keys.toList().forEach { cache.removeResource(it) }
            refresh(emptyList())
        }
    }
}

/** Keeps a long download going with the screen off and shows its progress. */
class DownloadService : Service() {
    companion object {
        fun start(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) return // The download still runs while the app is open; it just has no notification.
            runCatching { ContextCompat.startForegroundService(context, Intent(context, DownloadService::class.java)) }
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_CANCEL_DOWNLOAD) {
            RecitationStore.cancel()
            return START_NOT_STICKY
        }
        createChannel()
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC else 0
        ServiceCompat.startForeground(this, DOWNLOAD_NOTIFICATION_ID, notification(RecitationStore.download.value), type)
        scope.launch {
            RecitationStore.download.collect { progress ->
                if (progress == null) {
                    ServiceCompat.stopForeground(this@DownloadService, ServiceCompat.STOP_FOREGROUND_REMOVE)
                    stopSelf()
                } else {
                    runCatching { NotificationManagerCompat.from(this@DownloadService).notify(DOWNLOAD_NOTIFICATION_ID, notification(progress)) }
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        scope.coroutineContext[Job]?.cancel()
        super.onDestroy()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val strings = stringsFor(PrayerStore.language(this))
        val channel = NotificationChannel(DOWNLOAD_CHANNEL_ID, strings.audioDownload, NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun notification(progress: DownloadProgress?): android.app.Notification {
        val strings = stringsFor(PrayerStore.language(this))
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE,
        )
        val cancel = PendingIntent.getService(
            this, 3, Intent(this, DownloadService::class.java).setAction(ACTION_CANCEL_DOWNLOAD), PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, DOWNLOAD_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(strings.audioDownload)
            .setContentText(progress?.let { strings.downloadingSurah(it.surah) } ?: "")
            .setProgress(progress?.total ?: 0, progress?.done ?: 0, progress == null)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setContentIntent(open)
            .addAction(0, strings.stop, cancel)
            .build()
    }
}
