package app.qurandua.android.platform

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import app.qurandua.android.R
import app.qurandua.android.ui.theme.QuranDuaTheme
import app.qurandua.shared.i18n.prayerStringsFor
import app.qurandua.shared.model.ThemeMode
import app.qurandua.shared.prayer.AdhanSound
import app.qurandua.shared.prayer.Prayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private const val ADHAN_CHANNEL_ID = "prayer_adhan"
private const val ADHAN_NOTIFICATION_ID = 4200
private const val EXTRA_SOUND = "sound"
private const val EXTRA_PRAYER_NAME = "prayer"
private const val ACTION_STOP = "app.qurandua.STOP_ADHAN"

/** Plays the bundled adhan on the alarm stream, so it sounds even when the ringer is on vibrate. */
object AdhanPlayer {
    private var player: MediaPlayer? = null
    private val _playing = MutableStateFlow(false)
    val playing: StateFlow<Boolean> = _playing.asStateFlow()

    fun play(context: Context, sound: AdhanSound, onDone: () -> Unit = {}) {
        stop()
        val res = when (sound) {
            AdhanSound.FULL -> R.raw.adhan_full
            AdhanSound.SHORT -> R.raw.adhan_short
            else -> return onDone()
        }
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
        val created = MediaPlayer.create(context.applicationContext, res, attributes, 0) ?: return onDone()
        // Keeps the CPU awake for the length of the adhan when the screen is off.
        created.setWakeMode(context.applicationContext, android.os.PowerManager.PARTIAL_WAKE_LOCK)
        player = created
        _playing.value = true
        created.setOnCompletionListener {
            stop()
            onDone()
        }
        created.start()
    }

    fun stop() {
        player?.run {
            runCatching { stop() }
            release()
        }
        player = null
        _playing.value = false
    }
}

/**
 * Sounds the adhan when a prayer time comes: a foreground service keeps playback alive while
 * the screen is off, and its notification opens [AdhanActivity] full screen, like the call from
 * a mosque. Android 14+ may withhold the full-screen permission; the notification then shows
 * as a heads-up banner and the adhan still plays.
 */
class AdhanService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            finish()
            return START_NOT_STICKY
        }
        val prayer = intent?.getStringExtra(EXTRA_PRAYER_NAME)?.let { name -> Prayer.entries.firstOrNull { it.name == name } }
        val sound = intent?.getStringExtra(EXTRA_SOUND)?.let { name -> AdhanSound.entries.firstOrNull { it.name == name } }
        if (prayer == null || sound == null) {
            finish()
            return START_NOT_STICKY
        }
        val notification = Adhan.notification(this, prayer)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(ADHAN_NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(ADHAN_NOTIFICATION_ID, notification)
        }
        AdhanPlayer.play(this, sound) { finish() }
        return START_NOT_STICKY
    }

    private fun finish() {
        AdhanPlayer.stop()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        AdhanPlayer.stop()
        super.onDestroy()
    }
}

object Adhan {
    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val strings = prayerStringsFor(PrayerStore.language(context))
        // The adhan itself is the sound; the channel stays silent so they never overlap.
        val channel = NotificationChannel(ADHAN_CHANNEL_ID, strings.adhan, NotificationManager.IMPORTANCE_HIGH).apply {
            setSound(null, null)
            enableVibration(false)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun canUseFullScreen(context: Context): Boolean =
        Build.VERSION.SDK_INT < 34 || context.getSystemService(NotificationManager::class.java).canUseFullScreenIntent()

    /** Starts the adhan for [prayer]; returns false when Android refused, so the caller can fall back. */
    fun start(context: Context, prayer: Prayer, sound: AdhanSound): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return false
        createChannel(context)
        val intent = Intent(context, AdhanService::class.java)
            .putExtra(EXTRA_PRAYER_NAME, prayer.name)
            .putExtra(EXTRA_SOUND, sound.name)
        // An exact alarm may start a foreground service from the background; an inexact one may not.
        return runCatching { ContextCompat.startForegroundService(context, intent) }.isSuccess
    }

    fun stop(context: Context) {
        AdhanPlayer.stop()
        runCatching { context.startService(Intent(context, AdhanService::class.java).setAction(ACTION_STOP)) }
        NotificationManagerCompat.from(context).cancel(ADHAN_NOTIFICATION_ID)
    }

    fun notification(context: Context, prayer: Prayer): Notification {
        val strings = prayerStringsFor(PrayerStore.language(context))
        val settings = PrayerStore.load(context)
        val window = PendingIntent.getActivity(
            context, 1, AdhanActivity.intent(context, prayer),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val stop = PendingIntent.getService(
            context, 2, Intent(context, AdhanService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val builder = NotificationCompat.Builder(context, ADHAN_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(strings.prayerName(prayer))
            .setContentText(strings.notificationText(prayer))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(window)
            .setDeleteIntent(stop)
            .addAction(0, strings.stop, stop)
            .setOngoing(true)
        if (settings.fullScreen && canUseFullScreen(context)) builder.setFullScreenIntent(window, true)
        return builder.build()
    }
}

/** The prayer window: shown over the lock screen while the adhan plays. */
class AdhanActivity : ComponentActivity() {

    companion object {
        fun intent(context: Context, prayer: Prayer): Intent =
            Intent(context, AdhanActivity::class.java)
                .putExtra(EXTRA_PRAYER_NAME, prayer.name)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val prayer = intent.getStringExtra(EXTRA_PRAYER_NAME)?.let { name -> Prayer.entries.firstOrNull { it.name == name } } ?: Prayer.DHUHR
        val strings = prayerStringsFor(PrayerStore.language(this))
        val settings = PrayerStore.load(this)
        val zone = settings.place?.let { runCatching { TimeZone.of(it.timeZone) }.getOrNull() } ?: TimeZone.currentSystemDefault()
        val now = Clock.System.now().toLocalDateTime(zone)
        val time = "%02d:%02d".format(now.hour, now.minute)
        val arabic = FontFamily(Font(R.font.amiri_quran))

        setContent {
            QuranDuaTheme(ThemeMode.DARK) {
                val playing by AdhanPlayer.playing.collectAsState()
                // Close the window by itself once the adhan has ended.
                LaunchedEffect(playing) { if (!playing && settings.adhan.let { it == AdhanSound.FULL || it == AdhanSound.SHORT }) finishAfterDelay() }
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column(
                        Modifier.fillMaxSize().padding(32.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("ٱللَّهُ أَكۡبَرُ", fontFamily = arabic, fontSize = 52.sp, color = MaterialTheme.colorScheme.primary)
                        Text(strings.prayerName(prayer), style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center)
                        Text(time, style = MaterialTheme.typography.headlineMedium)
                        settings.place?.let { Text(it.name, style = MaterialTheme.typography.titleMedium) }
                        Text(strings.notificationText(prayer), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                        Button(
                            onClick = {
                                Adhan.stop(this@AdhanActivity)
                                finish()
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                        ) { Text(strings.stop, style = MaterialTheme.typography.titleMedium) }
                    }
                }
            }
        }
    }

    private suspend fun finishAfterDelay() {
        kotlinx.coroutines.delay(3_000)
        if (!AdhanPlayer.playing.value) finish()
    }
}
