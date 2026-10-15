package app.bibliya.android.platform

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
import app.bibliya.android.MainActivity
import app.bibliya.android.R
import app.bibliya.shared.content.ContentParser
import app.bibliya.shared.i18n.stringsFor
import app.bibliya.shared.model.Book
import app.bibliya.shared.model.DailyVerse
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

private const val PREFS = "reminder"
private const val CHANNEL_ID = "verse_of_day"
private const val REQUEST_CODE = 4100
private const val NOTIFICATION_ID = 4101
/** MainActivity scrolls Home to the verse of the day when its intent carries this extra. */
const val EXTRA_OPEN_VERSE = "open_verse_of_day"

/** What the alarm needs after a reboot, when the database is not open: kept in SharedPreferences. */
data class ReminderSettings(val on: Boolean, val minute: Int, val uiLang: String, val translation: String)

object Reminder {

    fun save(context: Context, settings: ReminderSettings) {
        val previous = load(context)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean("on", settings.on)
            .putInt("minute", settings.minute)
            .putString("lang", settings.uiLang)
            .putString("translation", settings.translation)
            .apply()
        if (previous != settings) {
            createChannel(context)
            schedule(context)
        }
    }

    fun load(context: Context): ReminderSettings {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return ReminderSettings(
            on = prefs.getBoolean("on", true),
            minute = prefs.getInt("minute", 8 * 60),
            uiLang = prefs.getString("lang", "ru") ?: "ru",
            translation = prefs.getString("translation", "ru") ?: "ru",
        )
    }

    fun createChannel(context: Context) {
        val name = stringsFor(load(context).uiLang).notificationChannel
        // Default importance: it shows once a day and never pops over what the person is doing.
        val channel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT)
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /**
     * Sets the next reminder, or cancels it when it is off. An inexact alarm is enough for a
     * verse of the day and needs no special permission (SCHEDULE_EXACT_ALARM is reviewed by Google Play).
     */
    fun schedule(context: Context) {
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pending = pendingIntent(context)
        alarm.cancel(pending)
        val settings = load(context)
        if (!settings.on) return
        alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextAt(settings.minute).toEpochMilliseconds(), pending)
    }

    private fun nextAt(minute: Int): kotlinx.datetime.Instant {
        val zone = TimeZone.currentSystemDefault()
        val now = Clock.System.now()
        val today = now.toLocalDateTime(zone).date
        val time = LocalTime(minute / 60, minute % 60)
        val todayAt = LocalDateTime(today, time).toInstant(zone)
        return if (todayAt > now) todayAt else LocalDateTime(today.plus(DatePeriod(days = 1)), time).toInstant(zone)
    }

    private fun pendingIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context, REQUEST_CODE, Intent(context, ReminderReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    @SuppressLint("MissingPermission") // POST_NOTIFICATIONS is checked first
    fun notify(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        val settings = load(context)
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val (verse, books) = runCatching { content(context) }.getOrNull() ?: return
        val daily = verseFor(verse, today) ?: return
        val text = daily.plainText(settings.translation)
        val reference = daily.refFor(settings.translation)?.let { ref ->
            val book = books.firstOrNull { it.id == ref.book }?.shortName(settings.translation) ?: ref.book
            val verses = if (ref.verseEnd != ref.verse) "${ref.verse}–${ref.verseEnd}" else "${ref.verse}"
            "$book ${ref.chapter}:$verses"
        }.orEmpty()
        createChannel(context)
        val open = PendingIntent.getActivity(
            context, REQUEST_CODE,
            Intent(context, MainActivity::class.java).putExtra(EXTRA_OPEN_VERSE, true)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(reference)
            .setContentText(text)
            .setSubText(stringsFor(settings.uiLang).notificationTitle)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    private fun content(context: Context): Pair<List<DailyVerse>, List<Book>> {
        fun read(name: String) = context.assets.open("content/$name").bufferedReader().use { it.readText() }
        return ContentParser.dailyVerses(read("votd.json")) to ContentParser.books(read("books.json"))
    }

    /** Same rule as ContentRepository.verseOfDay, so the notification and Home show the same verse. */
    private fun verseFor(list: List<DailyVerse>, date: LocalDate): DailyVerse? =
        list.takeIf { it.isNotEmpty() }?.let { it[(date.toEpochDays() % it.size + it.size) % it.size] }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Reminder.notify(context)
        Reminder.schedule(context)
    }
}

/** Alarms are lost on reboot and shift with the clock; set the next one again. */
class RescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Reminder.schedule(context)
    }
}
