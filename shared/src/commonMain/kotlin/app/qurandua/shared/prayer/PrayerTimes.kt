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
    val offsets: Map<Prayer, Double> = emptyMap(),
    val defaultAsr: AsrSchool = AsrSchool.STANDARD,
    /** Maghrib when the sun is this far below the horizon instead of at sunset (Tehran); 0 means sunset. */
    val maghribAngle: Double = 0.0,
    /** The authority's own name, used where no translated name exists. */
    val authority: String = "",
    /**
     * Minutes before sunrise / after sunset used on nights when the sun never reaches the Fajr
     * angle (Russian boards); 0 means the high-latitude rule applies instead. With these set, the
     * angle times are never capped otherwise.
     */
    val fajrFallbackMinutes: Int = 0,
    val ishaFallbackMinutes: Int = 0,
    /** DUM RF: in summer the Isha cap grows from 15/60 to 18/60 of the night (see ishaPortionAngle). */
    val summerIshaRamp: Boolean = false,
) {
    /**
     * Timetable of the Spiritual Administration of Muslims of Kazakhstan (muftyat.kz).
     * Fitted to its official Almaty timetable for 2026 (api.muftyat.kz): the sun 15° below
     * the horizon for Fajr and Isha, Hanafi Asr, and a few minutes of precaution added to
     * sunrise, Dhuhr, Asr and Maghrib. See DumkTimetableTest.
     */
    KAZAKHSTAN_DUMK(
        15.0, 15.0,
        offsets = mapOf(Prayer.SUNRISE to -3.0, Prayer.DHUHR to 3.0, Prayer.ASR to 3.0, Prayer.MAGHRIB to 3.0),
        defaultAsr = AsrSchool.HANAFI,
    ),
    /**
     * Spiritual Administration of Muslims of Kyrgyzstan (muftiyat.kg). Fitted to its official
     * calendar API for Bishkek and Osh (26 dates of 2026): Fajr 18°, Isha 16°, Hanafi Asr,
     * Maghrib 7 minutes after sunset. The board truncates seconds; the half minutes reproduce
     * that with our rounding. See KgUzTimetableTest.
     */
    KYRGYZSTAN_DUMK(
        18.0, 16.0,
        offsets = mapOf(Prayer.FAJR to -0.5, Prayer.SUNRISE to -0.5, Prayer.ASR to 1.5, Prayer.MAGHRIB to 6.5, Prayer.ISHA to -0.5),
        defaultAsr = AsrSchool.HANAFI,
    ),
    /**
     * Muslim Board of Uzbekistan (muslim.uz, islom.uz taqvim): Fajr and Isha at 15.5°,
     * Hanafi Asr, Shom 4 minutes after sunset. Matches the board's own daily times for
     * Tashkent and Samarkand. See KgUzTimetableTest.
     */
    UZBEKISTAN_MUSLIM_BOARD(
        15.5, 15.5,
        offsets = mapOf(Prayer.MAGHRIB to 4.0),
        defaultAsr = AsrSchool.HANAFI,
    ),
    MUSLIM_WORLD_LEAGUE(18.0, 17.0),
    ISNA(15.0, 15.0),
    EGYPT(19.5, 17.5),
    UMM_AL_QURA(18.5, 0.0, ishaMinutesAfterMaghrib = 90),
    KARACHI(18.0, 18.0, defaultAsr = AsrSchool.HANAFI),
    TURKEY_DIYANET(18.0, 17.0, offsets = mapOf(Prayer.SUNRISE to -7.0, Prayer.DHUHR to 5.0, Prayer.ASR to 4.0, Prayer.MAGHRIB to 7.0), defaultAsr = AsrSchool.STANDARD),
    /** Kemenag (Indonesia), fitted to its Jakarta timetable: 2–4 minutes of ihtiyat on every time. */
    INDONESIA_KEMENAG(
        20.0, 18.0,
        offsets = mapOf(Prayer.FAJR to 2.5, Prayer.SUNRISE to -3.5, Prayer.DHUHR to 3.5, Prayer.ASR to 2.5, Prayer.MAGHRIB to 3.5, Prayer.ISHA to 2.5),
    ),
    /** Former generic Russian setting, kept for people who chose it; no board publishes it. */
    RUSSIA_DUM(16.0, 15.0, defaultAsr = AsrSchool.HANAFI),
    /**
     * Spiritual Administration of Muslims of the Russian Federation (dumrf.ru, muslim.ru), fitted
     * to 78 Moscow dates within 1–2 min. Its tables use the majority Asr. In summer Fajr and Isha are
     * capped at a share of the night; see quran-app/research/prayer-times-russia.md.
     */
    RUSSIA_DUM_RF(
        18.0, 15.0,
        offsets = mapOf(Prayer.SUNRISE to -5.0, Prayer.DHUHR to 5.0, Prayer.MAGHRIB to 5.0),
        summerIshaRamp = true,
        authority = "ДУМ РФ",
    ),
    /** ДУМ Республики Татарстан (dumrt.ru): Fajr is their "сухур" column; white nights: 2 h / 1.5 h. */
    TATARSTAN_DUMRT(
        18.0, 15.0,
        offsets = mapOf(Prayer.FAJR to -0.5, Prayer.DHUHR to 0.5, Prayer.ASR to 1.0, Prayer.MAGHRIB to 0.5),
        defaultAsr = AsrSchool.HANAFI,
        fajrFallbackMinutes = 121, ishaFallbackMinutes = 90,
        authority = "ДУМ РТ",
    ),
    /** ДУМ Республики Башкортостан (dumrb.com). Their summer schedule is set by hand; 90 min is its main step. */
    BASHKORTOSTAN_DUMRB(
        16.0, 14.7,
        offsets = mapOf(Prayer.ASR to -1.0),
        defaultAsr = AsrSchool.HANAFI,
        fajrFallbackMinutes = 90, ishaFallbackMinutes = 90,
        authority = "ДУМ РБ",
    ),
    /** Муфтият Республики Дагестан (muftiyatrd.ru), within 1 min on over 99% of days. */
    DAGESTAN_MUFTIYAT(
        17.5, 15.0,
        offsets = mapOf(Prayer.FAJR to 0.0, Prayer.SUNRISE to -2.5, Prayer.DHUHR to 4.5, Prayer.ASR to 1.5, Prayer.MAGHRIB to 4.5),
        authority = "Муфтият Дагестана",
    ),

    // Fitted to each authority's published timetable; sources and errors in quran-app/research/prayer-methods-world.md.
    /** JAKIM (Malaysia) since 2019: Fajr 18°, with its own precaution minutes. */
    MALAYSIA_JAKIM(18.0, 18.0, offsets = mapOf(Prayer.FAJR to 2.0, Prayer.SUNRISE to -1.0, Prayer.DHUHR to 2.0, Prayer.ASR to 2.0, Prayer.MAGHRIB to 2.0, Prayer.ISHA to 2.0), authority = "Malaysia (JAKIM)"),
    /** MUIS (Singapore), also matches Brunei's ministry. */
    SINGAPORE_MUIS(20.0, 18.0, offsets = mapOf(Prayer.FAJR to 0.5, Prayer.SUNRISE to 0.5, Prayer.DHUHR to 1.5, Prayer.ASR to 0.5, Prayer.MAGHRIB to 0.5, Prayer.ISHA to 0.5), authority = "Singapore (MUIS)"),
    /** Council of Ulema of Tajikistan: Isha 100 minutes after sunset. Their Dhuhr is set by hand per month and is not modelled. */
    TAJIKISTAN_ULAMO(18.0, 0.0, ishaMinutesAfterMaghrib = 100, offsets = mapOf(Prayer.SUNRISE to -3.0, Prayer.MAGHRIB to 10.0), defaultAsr = AsrSchool.HANAFI, authority = "Tajikistan (Shuroi Ulamo)"),
    /** Caucasus Muslims Board (Azerbaijan). */
    AZERBAIJAN_QMI(16.0, 15.0, offsets = mapOf(Prayer.ASR to -1.0, Prayer.MAGHRIB to 13.0), defaultAsr = AsrSchool.HANAFI, authority = "Azerbaijan (QMİ)"),
    /** Institute of Geophysics, University of Tehran: Maghrib at 4.5° below the horizon. */
    TEHRAN_IGUT(17.7, 14.0, maghribAngle = 4.5, authority = "Iran (University of Tehran)"),
    MOROCCO_HABOUS(19.0, 17.0, offsets = mapOf(Prayer.SUNRISE to -3.0, Prayer.DHUHR to 5.0, Prayer.MAGHRIB to 3.0), authority = "Morocco (Habous)"),
    JORDAN_AWQAF(18.0, 18.0, offsets = mapOf(Prayer.SUNRISE to -6.0, Prayer.MAGHRIB to 6.0), authority = "Jordan (Awqaf)"),
    UAE_AWQAF(18.2, 18.2, offsets = mapOf(Prayer.SUNRISE to -3.0, Prayer.DHUHR to 2.0, Prayer.ASR to 1.0, Prayer.MAGHRIB to 3.0), authority = "UAE (Awqaf)"),
    KUWAIT(18.0, 17.5, authority = "Kuwait (Awqaf)"),
    OMAN_MARA(18.0, 18.0, offsets = mapOf(Prayer.FAJR to 1.0, Prayer.DHUHR to 6.0, Prayer.ASR to 5.0, Prayer.MAGHRIB to 5.0, Prayer.ISHA to 1.0), authority = "Oman (MARA)"),
    BANGLADESH_IFB(18.0, 18.0, offsets = mapOf(Prayer.FAJR to 2.0, Prayer.SUNRISE to -1.0, Prayer.DHUHR to 3.0, Prayer.MAGHRIB to 3.0, Prayer.ISHA to 1.0), defaultAsr = AsrSchool.HANAFI, authority = "Bangladesh (Islamic Foundation)"),
    /** Islamic Community of Bosnia and Herzegovina (vaktija.ba). */
    BOSNIA_IZ(18.0, 16.0, offsets = mapOf(Prayer.SUNRISE to -6.0, Prayer.DHUHR to 1.0, Prayer.MAGHRIB to 5.5), authority = "Bosnia (Islamska zajednica)"),
    /** Checked only against third-party sites. */
    TUNISIA(18.0, 18.0, offsets = mapOf(Prayer.DHUHR to 7.0, Prayer.ASR to 1.0, Prayer.MAGHRIB to 2.0, Prayer.ISHA to 3.0), authority = "Tunisia"),
    /** Checked only against third-party sites. */
    ALGERIA(18.0, 17.0, offsets = mapOf(Prayer.DHUHR to 1.0, Prayer.MAGHRIB to 3.0), authority = "Algeria"),
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
            maghrib = angleTime(if (method.maghribAngle > 0) method.maghribAngle else SUN_HORIZON, maghrib / 24, beforeNoon = false)
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
        // On a white night (the Fajr angle is never reached) these boards switch both times to fixed intervals.
        val whiteNight = fajr.isNaN()
        if (method.fajrFallbackMinutes > 0) {
            if (whiteNight) fajr = sunrise - method.fajrFallbackMinutes / 60.0
        } else {
            val fajrLimit = portion(method.fajrAngle)
            if (fajr.isNaN() || sunrise - fajr > fajrLimit) fajr = sunrise - fajrLimit
        }
        if (method.ishaMinutesAfterMaghrib == 0) {
            if (method.ishaFallbackMinutes > 0) {
                if (whiteNight || isha.isNaN()) isha = maghrib + method.ishaFallbackMinutes / 60.0
            } else {
                val ishaLimit = portion(if (method.summerIshaRamp) ishaPortionAngle(date) else method.ishaAngle)
                if (isha.isNaN() || isha - maghrib > ishaLimit) isha = maghrib + ishaLimit
            }
        }

        val local = mapOf(
            Prayer.FAJR to fajr, Prayer.SUNRISE to sunrise, Prayer.DHUHR to dhuhr,
            Prayer.ASR to asr, Prayer.MAGHRIB to maghrib, Prayer.ISHA to isha,
        )
        val utcMidnight = date.atStartOfDayIn(TimeZone.UTC)
        val times = local.mapValues { (prayer, solarHours) ->
            // Solar time at this longitude -> hours after UTC midnight.
            val utcHours = solarHours - lng / 15.0
            val offset = (method.offsets[prayer] ?: 0.0) + (config.userOffsets[prayer] ?: 0)
            roundToMinute(utcMidnight + ((utcHours * 60 + offset) * 60).roundToLong().seconds)
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

    /**
     * DUM RF's "smooth transition" (2021 fatwa): the Isha cap is 15/60 of the night until 27 April,
     * rises evenly to 18/60 by 1 June, stays there through June and falls back to 15 by 5 August.
     */
    internal fun ishaPortionAngle(date: LocalDate): Double {
        val d = date.dayOfYear - (if (isLeap(date.year)) 1 else 0).let { leap -> if (date.monthNumber > 2) leap else 0 }
        val apr27 = 117; val jun1 = 152; val jun30 = 181; val aug5 = 217
        return when {
            d <= apr27 || d >= aug5 -> 15.0
            d < jun1 -> 15.0 + 3.0 * (d - apr27) / (jun1 - apr27)
            d <= jun30 -> 18.0
            else -> 18.0 - 3.0 * (d - jun30) / (aug5 - jun30)
        }
    }

    private fun isLeap(y: Int) = (y % 4 == 0 && y % 100 != 0) || y % 400 == 0

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
