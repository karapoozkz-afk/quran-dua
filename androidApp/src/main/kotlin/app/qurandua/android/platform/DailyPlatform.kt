package app.qurandua.android.platform

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import app.qurandua.android.MainActivity
import app.qurandua.android.R
import app.qurandua.android.ui.DailyController
import app.qurandua.shared.daily.DailyContent
import app.qurandua.shared.daily.DailyParser
import app.qurandua.shared.daily.DailySettings
import app.qurandua.shared.daily.DailySlot
import app.qurandua.shared.daily.dailyStringsFor
import app.qurandua.shared.daily.forLang
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json

private const val DAILY_PREFS = "daily"
private const val KEY_DAILY = "settings"
private const val DAILY_CHANNEL_ID = "daily_sunnah"
private const val EXTRA_SLOT = "slot"
/** MainActivity opens the "Sunnah of the day" screen when its intent carries this extra. */
const val EXTRA_OPEN_DAILY = "open_daily"

private val dailyJson = Json { ignoreUnknownKeys = true }

object DailyStore {
    fun load(context: Context): DailySettings {
        val raw = context.getSharedPreferences(DAILY_PREFS, Context.MODE_PRIVATE).getString(KEY_DAILY, null)
        return raw?.let { runCatching { dailyJson.decodeFromString<DailySettings>(it) }.getOrNull() } ?: DailySettings()
    }

    fun save(context: Context, settings: DailySettings) {
        context.getSharedPreferences(DAILY_PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_DAILY, dailyJson.encodeToString(DailySettings.serializer(), settings)).apply()
    }

    /** Parsed once per process; the asset is a few megabytes. */
    @Volatile private var content: DailyContent? = null

    fun content(context: Context): DailyContent = content ?: runCatching {
        DailyParser.parse(context.assets.open("content/daily.json").bufferedReader().use { it.readText() })
    }.getOrDefault(DailyContent()).also { content = it }
}

class AndroidDailyController(private val context: Context) : DailyController {
    private val _settings = MutableStateFlow(DailyStore.load(context))
    override val settings: StateFlow<DailySettings> = _settings.asStateFlow()
    override val content: DailyContent get() = DailyStore.content(context)
    override val openRequested = MutableStateFlow(false)

    override fun update(transform: (DailySettings) -> DailySettings) {
        val updated = transform(_settings.value)
        _settings.value = updated
        DailyStore.save(context, updated)
        DailyAlarms.schedule(context)
    }
}

object DailyAlarms {
    private fun requestCode(slot: DailySlot) = 4200 + slot.ordinal

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val name = dailyStringsFor(PrayerStore.language(context)).channel
        // Default importance: it shows and sounds once, but never pops over what the person is doing.
        val channel = NotificationChannel(DAILY_CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT)
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /** Sets the next morning and the next evening reminder, or cancels both when they are off. */
    fun schedule(context: Context) {
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val settings = DailyStore.load(context)
        for (slot in DailySlot.entries) {
            val pending = pendingIntent(context, slot)
            alarm.cancel(pending)
            if (!settings.on) continue
            val minute = if (slot == DailySlot.MORNING) settings.morningMinute else settings.eveningMinute
            val at = nextAt(minute).toEpochMilliseconds()
            val exactAllowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarm.canScheduleExactAlarms()
            if (exactAllowed) alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending)
            else alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending)
        }
    }

    private fun nextAt(minute: Int): kotlinx.datetime.Instant {
        val zone = TimeZone.currentSystemDefault()
        val now = Clock.System.now()
        val today = now.toLocalDateTime(zone).date
        val time = LocalTime(minute / 60, minute % 60)
        val todayAt = LocalDateTime(today, time).toInstant(zone)
        return if (todayAt > now) todayAt else LocalDateTime(today.plus(DatePeriod(days = 1)), time).toInstant(zone)
    }

    private fun pendingIntent(context: Context, slot: DailySlot): PendingIntent {
        val intent = Intent(context, DailyAlarmReceiver::class.java).putExtra(EXTRA_SLOT, slot.name)
        return PendingIntent.getBroadcast(
            context, requestCode(slot), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    @SuppressLint("MissingPermission") // POST_NOTIFICATIONS is checked first
    fun notify(context: Context, slot: DailySlot) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val hadith = DailyStore.content(context).forDay(today, slot) ?: return
        val lang = PrayerStore.language(context)
        val strings = dailyStringsFor(lang)
        createChannel(context)
        val open = PendingIntent.getActivity(
            context, requestCode(slot),
            Intent(context, MainActivity::class.java).putExtra(EXTRA_OPEN_DAILY, true)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val heading = if (slot == DailySlot.MORNING) strings.notifyMorning else strings.notifyEvening
        val title = hadith.title.forLang(lang)
        val summary = hadith.summary.forLang(lang)
        val notification = NotificationCompat.Builder(context, DAILY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(summary)
            .setSubText(heading)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$summary\n${strings.source(hadith)}"))
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(5000 + slot.ordinal, notification)
    }
}

class DailyAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val slot = intent.getStringExtra(EXTRA_SLOT)?.let { name -> DailySlot.entries.firstOrNull { it.name == name } }
        if (slot != null) DailyAlarms.notify(context, slot)
        DailyAlarms.schedule(context)
    }
}
