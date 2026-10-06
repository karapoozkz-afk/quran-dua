package app.qurandua.shared.prayer

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.DatePeriod
import kotlinx.serialization.Serializable
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.tan
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

enum class Prayer { FAJR, SUNRISE, DHUHR, ASR, MAGHRIB, ISHA }

/** Asr begins when an object's shadow equals its length (majority) or twice its length (Hanafi). */
enum class AsrSchool(val shadowFactor: Int) { STANDARD(1), HANAFI(2) }

/**
 * Twilight angles of the common calculation conventions. Offsets are the
 * convention's own fixed adjustments in minutes; the user adds their own on top.
 */
enum class CalculationMethod(
    val fajrAngle: Double,
    val ishaAngle: Double,
    /** Isha this many minutes after Maghrib instead of an angle (Umm al-Qura). */
    val ishaMinutesAfterMaghrib: Int = 0,
    val offsets: Map<Prayer, Int> = emptyMap(),
    val defaultAsr: AsrSchool = AsrSchool.STANDARD,
) {
    /**
     * Approximation of the timetable of the Spiritual Administration of Muslims of
     * Kazakhstan (muftyat.kz). Its exact parameters are not published in machine-readable
     * form, so users compare with muftyat.kz for their city and adjust minutes.
     */
    KAZAKHSTAN_DUMK(18.0, 15.0, defaultAsr = AsrSchool.HANAFI),
    MUSLIM_WORLD_LEAGUE(18.0, 17.0),
    ISNA(15.0, 15.0),
    EGYPT(19.5, 17.5),
    UMM_AL_QURA(18.5, 0.0, ishaMinutesAfterMaghrib = 90),
    KARACHI(18.0, 18.0, defaultAsr = AsrSchool.HANAFI),
    TURKEY_DIYANET(18.0, 17.0, offsets = mapOf(Prayer.SUNRISE to -7, Prayer.DHUHR to 5, Prayer.ASR to 4, Prayer.MAGHRIB to 7), defaultAsr = AsrSchool.STANDARD),
    INDONESIA_KEMENAG(20.0, 18.0, offsets = mapOf(Prayer.DHUHR to 2)),
    RUSSIA_DUM(16.0, 15.0, defaultAsr = AsrSchool.HANAFI),
}

/** Used when the sun does not reach the twilight angle (summer in the north of Kazakhstan, Europe). */
enum class HighLatitudeRule { ANGLE_BASED, MIDDLE_OF_NIGHT, SEVENTH_OF_NIGHT }

@Serializable
data class PrayerConfig(
    val latitude: Double,
    val longitude: Double,
    val method: CalculationMethod = CalculationMethod.MUSLIM_WORLD_LEAGUE,
    val asr: AsrSchool = method.defaultAsr,
    val highLatitude: HighLatitudeRule = HighLatitudeRule.ANGLE_BASED,
    /** The user's own corrections in minutes, e.g. to match the local mosque. */
    val userOffsets: Map<Prayer, Int> = emptyMap(),
)

data class DayPrayerTimes(val date: LocalDate, val times: Map<Prayer, Instant>) {
    operator fun get(prayer: Prayer): Instant = times.getValue(prayer)
}

/**
 * Astronomical prayer times computed on the device, no network needed. The algorithm is
 * the widely used one from PrayTimes.org (sun declination and equation of time per
 * Jean Meeus' low-precision formulas), precise to about a minute.
 */
object PrayerTimesCalculator {

    fun compute(date: LocalDate, config: PrayerConfig): DayPrayerTimes {
        val lat = config.latitude
        val lng = config.longitude
        val method = config.method
        val jDate = julian(date.year, date.monthNumber, date.dayOfMonth) - lng / (15 * 24.0)

        fun sun(dayFraction: Double) = sunPosition(jDate + dayFraction)
        fun midDay(t: Double): Double = fixHour(12 - sun(t).equation)
        fun angleTime(angle: Double, t: Double, beforeNoon: Boolean): Double {
            val decl = sun(t).declination
            val noon = midDay(t)
            val cosV = (-dsin(angle) - dsin(decl) * dsin(lat)) / (dcos(decl) * dcos(lat))
            if (cosV < -1 || cosV > 1) return Double.NaN
            val v = darccos(cosV) / 15.0
            return noon + if (beforeNoon) -v else v
        }
        fun asrTime(factor: Int, t: Double): Double {
            val decl = sun(t).declination
            val angle = -darccot(factor + dtan(abs(lat - decl)))
            return angleTime(angle, t, beforeNoon = false)
        }

        // Two passes: first with rough guesses, then at the times found, so the sun position fits each time.
        var fajr = 5.0; var sunrise = 6.0; var dhuhr = 12.0; var asr = 13.0; var maghrib = 18.0; var isha = 18.0
        repeat(2) {
            fajr = angleTime(method.fajrAngle, fajr / 24, beforeNoon = true)
            sunrise = angleTime(SUN_HORIZON, sunrise / 24, beforeNoon = true)
            dhuhr = midDay(dhuhr / 24)
            asr = asrTime(config.asr.shadowFactor, asr / 24)
            maghrib = angleTime(SUN_HORIZON, maghrib / 24, beforeNoon = false)
            isha = if (method.ishaMinutesAfterMaghrib > 0) maghrib + method.ishaMinutesAfterMaghrib / 60.0
            else angleTime(method.ishaAngle, isha / 24, beforeNoon = false)
            if (fajr.isNaN()) fajr = 5.0
            if (isha.isNaN() && method.ishaMinutesAfterMaghrib == 0) isha = 18.0
        }
        // Recompute NaN-prone ones for the high-latitude rule.
        fajr = angleTime(method.fajrAngle, fajr / 24, beforeNoon = true)
        if (method.ishaMinutesAfterMaghrib == 0) isha = angleTime(method.ishaAngle, isha / 24, beforeNoon = false)

        // High latitudes: twilight never ends or starts too far from sunset/sunrise.
        val night = 24 - (maghrib - sunrise)
        fun portion(angle: Double) = when (config.highLatitude) {
            HighLatitudeRule.ANGLE_BASED -> angle / 60.0 * night
            HighLatitudeRule.MIDDLE_OF_NIGHT -> night / 2
            HighLatitudeRule.SEVENTH_OF_NIGHT -> night / 7
        }
        val fajrLimit = portion(method.fajrAngle)
        if (fajr.isNaN() || sunrise - fajr > fajrLimit) fajr = sunrise - fajrLimit
        if (method.ishaMinutesAfterMaghrib == 0) {
            val ishaLimit = portion(method.ishaAngle)
            if (isha.isNaN() || isha - maghrib > ishaLimit) isha = maghrib + ishaLimit
        }

        val local = mapOf(
            Prayer.FAJR to fajr, Prayer.SUNRISE to sunrise, Prayer.DHUHR to dhuhr,
            Prayer.ASR to asr, Prayer.MAGHRIB to maghrib, Prayer.ISHA to isha,
        )
        val utcMidnight = date.atStartOfDayIn(TimeZone.UTC)
        val times = local.mapValues { (prayer, solarHours) ->
            // Solar time at this longitude -> hours after UTC midnight.
            val utcHours = solarHours - lng / 15.0
            val offset = (method.offsets[prayer] ?: 0) + (config.userOffsets[prayer] ?: 0)
            roundToMinute(utcMidnight + (utcHours * 3600).roundToLong().seconds + offset.minutes)
        }
        return DayPrayerTimes(date, times)
    }

    /** The next prayer (not sunrise) after [now], looking into tomorrow if needed. */
    fun next(now: Instant, today: LocalDate, config: PrayerConfig): Pair<Prayer, Instant> {
        for (day in listOf(today, today.plus(DatePeriod(days = 1)))) {
            val times = compute(day, config)
            Prayer.entries.filter { it != Prayer.SUNRISE }.forEach { p ->
                if (times[p] > now) return p to times[p]
            }
        }
        error("no prayer within two days")
    }

    private fun roundToMinute(instant: Instant): Instant {
        val s = instant.epochSeconds
        return Instant.fromEpochSeconds(floor((s + 30) / 60.0).toLong() * 60)
    }

    /** Refraction plus the sun's radius: sunrise and sunset are at 0.833° below the horizon. */
    private const val SUN_HORIZON = 0.833

    private class SunPosition(val declination: Double, val equation: Double)

    private fun sunPosition(jd: Double): SunPosition {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * dsin(g) + 0.020 * dsin(2 * g))
        val e = 23.439 - 0.00000036 * d
        val ra = darctan2(dcos(e) * dsin(l), dcos(l)) / 15.0
        val eqt = q / 15.0 - fixHour(ra)
        val decl = darcsin(dsin(e) * dsin(l))
        return SunPosition(decl, eqt)
    }

    private fun julian(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) { y -= 1; m += 12 }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun rad(d: Double) = d * PI / 180
    private fun deg(r: Double) = r * 180 / PI
    private fun dsin(d: Double) = sin(rad(d))
    private fun dcos(d: Double) = cos(rad(d))
    private fun dtan(d: Double) = tan(rad(d))
    private fun darcsin(x: Double) = deg(asin(x))
    private fun darccos(x: Double) = deg(acos(x))
    private fun darctan2(y: Double, x: Double) = deg(atan2(y, x))
    private fun darccot(x: Double) = deg(atan(1 / x))
    private fun fixAngle(a: Double) = a - 360 * floor(a / 360)
    private fun fixHour(h: Double) = h - 24 * floor(h / 24)
}
