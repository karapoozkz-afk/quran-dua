package app.qurandua.android.platform

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import app.qurandua.android.ui.AudioController
import app.qurandua.android.ui.PlatformActions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class AndroidPlatformActions(private val context: Context) : PlatformActions {

    override fun share(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    override fun copy(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(null, text))
        // Android 13+ shows its own copy confirmation.
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(context, android.R.string.copy, Toast.LENGTH_SHORT).show()
        }
    }

    override fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }
}

/**
 * Streams ayah recitations with ExoPlayer. Audio is not bundled, so the app stays small;
 * what was played once is cached on disk and replays offline.
 * The cache holds up to [CACHE_BYTES] and drops the least recently played ayahs first.
 */
@androidx.annotation.OptIn(UnstableApi::class)
class ExoAudioController(context: Context) : AudioController {
    private val cache = SimpleCache(
        File(context.applicationContext.cacheDir, "recitations"),
        LeastRecentlyUsedCacheEvictor(CACHE_BYTES),
        StandaloneDatabaseProvider(context.applicationContext),
    )
    private val dataSource = CacheDataSource.Factory()
        .setCache(cache)
        .setUpstreamDataSourceFactory(DefaultDataSource.Factory(context.applicationContext))
        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    private val player = ExoPlayer.Builder(context.applicationContext)
        .setMediaSourceFactory(DefaultMediaSourceFactory(dataSource))
        .build()
    private val _playing = MutableStateFlow<String?>(null)
    override val playing: StateFlow<String?> = _playing.asStateFlow()

    init {
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED || state == Player.STATE_IDLE) _playing.value = null
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                _playing.value = null
            }
        })
    }

    override fun play(key: String, urls: List<String>) {
        player.setMediaItems(urls.map(MediaItem::fromUri))
        player.prepare()
        player.play()
        _playing.value = key
    }

    override fun stop() {
        player.stop()
        player.clearMediaItems()
        _playing.value = null
    }

    fun release() {
        player.release()
        cache.release()
        _playing.value = null
    }

    private companion object {
        const val CACHE_BYTES = 300L * 1024 * 1024
    }
}
