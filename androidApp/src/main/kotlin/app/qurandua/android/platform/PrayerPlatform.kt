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
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import app.qurandua.android.MainActivity
import app.qurandua.android.R
import app.qurandua.android.ui.PrayerController
import app.qurandua.shared.i18n.prayerStringsFor
import app.qurandua.shared.prayer.Place
import app.qurandua.shared.prayer.Prayer
import app.qurandua.shared.prayer.PrayerSettings
import app.qurandua.shared.prayer.PrayerTimesCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume

private const val PREFS = "prayer"
private const val KEY_SETTINGS = "settings"
private const val KEY_LANGUAGE = "language"
private const val CHANNEL_ID = "prayer_times"
private const val EXTRA_PRAYER = "prayer"

private val json = Json { ignoreUnknownKeys = true }

/** Prayer settings live in SharedPreferences so alarm receivers can read them without the database. */
object PrayerStore {
    fun load(context: Context): PrayerSettings {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_SETTINGS, null)
        return raw?.let { runCatching { json.decodeFromString<PrayerSettings>(it) }.getOrNull() } ?: PrayerSettings()
    }

    fun save(context: Context, settings: PrayerSettings) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_SETTINGS, json.encodeToString(PrayerSettings.serializer(), settings)).apply()
    }

    fun language(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_LANGUAGE, null) ?: "en"

    fun setLanguage(context: Context, language: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_LANGUAGE, language).apply()
    }
}

class AndroidPrayerController(private val context: Context) : PrayerController {
    private val _settings = MutableStateFlow(PrayerStore.load(context))
    override val settings: StateFlow<PrayerSettings> = _settings.asStateFlow()

    override fun update(transform: (PrayerSettings) -> PrayerSettings) {
        val updated = transform(_settings.value)
        _settings.value = updated
        PrayerStore.save(context, updated)
        PrayerAlarms.schedule(context)
    }

    override fun setLanguage(language: String) {
        if (PrayerStore.language(context) != language) PrayerStore.setLanguage(context, language)
    }

    @SuppressLint("MissingPermission") // checked right below
    override suspend fun currentLocation(): Place? {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!granted) return null
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER, LocationManager.GPS_PROVIDER)
            .filter { runCatching { manager.isProviderEnabled(it) }.getOrDefault(false) }
        val last = providers.mapNotNull { runCatching { manager.getLastKnownLocation(it) }.getOrNull() }.maxByOrNull { it.time }
        val location = last ?: providers.firstOrNull()?.let { provider ->
            withTimeoutOrNull(15_000) {
                suspendCancellableCoroutine { cont ->
                    val listener = object : LocationListener {
                        override fun onLocationChanged(location: Location) {
                            manager.removeUpdates(this)
                            if (cont.isActive) cont.resume(location)
                        }
                        @Deprecated("Required on API < 29")
                        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
                        override fun onProviderEnabled(provider: String) = Unit
                        override fun onProviderDisabled(provider: String) = Unit
                    }
                    @Suppress("DEPRECATION")
                    manager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
                    cont.invokeOnCancellation { manager.removeUpdates(listener) }
                }
            }
        } ?: return null
        val zone = TimeZone.currentSystemDefault().id
        val name = "%.2f, %.2f".format(location.latitude, location.longitude)
        return Place(name, location.latitude, location.longitude, zone, country = countryFromZone(zone))
    }

    /** Best guess of the country from the time zone, for the default calculation method. */
    private fun countryFromZone(zone: String): String = when {
        zone in setOf("Asia/Almaty", "Asia/Aqtobe", "Asia/Aqtau", "Asia/Atyrau", "Asia/Oral", "Asia/Qostanay", "Asia/Qyzylorda") -> "KZ"
        zone == "Europe/Istanbul" -> "TR"
        zone in setOf("Asia/Jakarta", "Asia/Makassar", "Asia/Jayapura", "Asia/Pontianak") -> "ID"
        zone == "Asia/Karachi" -> "PK"
        zone == "Asia/Riyadh" -> "SA"
        zone == "Africa/Cairo" -> "EG"
        zone in setOf("Europe/Moscow", "Europe/Samara", "Europe/Volgograd", "Asia/Yekaterinburg") -> "RU"
        zone.startsWith("America/") -> "US"
        else -> ""
    }
}

object PrayerAlarms {
    private const val REQUEST_CODE = 4100

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val name = prayerStringsFor(PrayerStore.language(context)).notifications
        val channel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH)
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /** Schedules one alarm for the next prayer the user wants to be notified about, or cancels it. */
    fun schedule(context: Context) {
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pending = pendingIntent(context, null)
        alarm.cancel(pending)
        val settings = PrayerStore.load(context)
        val config = settings.config() ?: return
        if (!settings.notificationsOn || settings.notify.isEmpty()) return

        val zone = runCatching { TimeZone.of(settings.place!!.timeZone) }.getOrDefault(TimeZone.currentSystemDefault())
        val now = Clock.System.now()
        val today = now.toLocalDateTime(zone).date
        val next = (0..2).asSequence()
            .map { PrayerTimesCalculator.compute(today.plus(DatePeriod(days = it)), config) }
            .flatMap { day -> settings.notify.map { it to day[it] } }
            .filter { it.second > now }
            .minByOrNull { it.second } ?: return

        val at = next.second.toEpochMilliseconds()
        val intent = pendingIntent(context, next.first)
        val exactAllowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarm.canScheduleExactAlarms()
        if (exactAllowed) alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, intent)
        else alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, intent)
    }

    private fun pendingIntent(context: Context, prayer: Prayer?): PendingIntent {
        val intent = Intent(context, PrayerAlarmReceiver::class.java)
        if (prayer != null) intent.putExtra(EXTRA_PRAYER, prayer.name)
        return PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    @SuppressLint("MissingPermission") // POST_NOTIFICATIONS is checked on the first line
    fun notify(context: Context, prayer: Prayer) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        createChannel(context)
        val strings = prayerStringsFor(PrayerStore.language(context))
        val open = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(strings.prayerName(prayer))
            .setContentText(strings.notificationText(prayer))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(prayer.ordinal, notification)
    }
}

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayer = intent.getStringExtra(EXTRA_PRAYER)?.let { name -> Prayer.entries.firstOrNull { it.name == name } }
        if (prayer != null) PrayerAlarms.notify(context, prayer)
        PrayerAlarms.schedule(context)
    }
}

/** Alarms are lost on reboot and must follow time-zone and clock changes. */
class PrayerRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        PrayerAlarms.schedule(context)
    }
}
