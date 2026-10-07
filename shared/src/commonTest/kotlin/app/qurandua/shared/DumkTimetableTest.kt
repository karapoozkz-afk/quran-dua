package app.qurandua.shared

import app.qurandua.shared.prayer.CalculationMethod
import app.qurandua.shared.prayer.Prayer
import app.qurandua.shared.prayer.PrayerConfig
import app.qurandua.shared.prayer.PrayerTimesCalculator
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The official DUMK timetable for Almaty (api.muftyat.kz/prayer-times/2026/43.238293/76.945465,
 * fetched 2026-10-07), local time UTC+5: fajr, sunrise, dhuhr, asr, maghrib, isha.
 * KAZAKHSTAN_DUMK reproduces it within a minute on most days; in March and April the
 * official Fajr and sunrise run two minutes earlier, so the test allows two.
 */
class DumkTimetableTest {

    private val official = mapOf(
        "2026-01-01" to "05:59 07:21 11:59 14:48 16:30 17:53",
        "2026-02-15" to "05:31 06:48 12:09 15:42 17:26 18:42",
        "2026-03-20" to "04:36 05:51 12:02 16:18 18:08 19:24",
        "2026-04-15" to "03:45 05:06 11:55 16:40 18:39 20:00",
        "2026-05-15" to "02:52 04:26 11:52 16:59 19:12 20:46",
        "2026-06-21" to "02:23 04:09 11:57 17:16 19:39 21:25",
        "2026-08-15" to "03:29 04:54 12:00 16:55 18:59 20:24",
        "2026-09-15" to "04:12 05:28 11:50 16:15 18:06 19:23",
        "2026-10-07" to "04:38 05:53 11:43 15:41 17:26 18:41",
        "2026-11-15" to "05:23 06:42 11:40 14:49 16:31 17:50",
        "2026-12-21" to "05:55 07:18 11:53 14:41 16:23 17:46",
    )

    @Test
    fun almatyMatchesTheOfficialTimetable() {
        val method = CalculationMethod.KAZAKHSTAN_DUMK
        val config = PrayerConfig(43.238293, 76.945465, method, method.defaultAsr)
        val zone = TimeZone.of("UTC+05:00")
        val misses = official.flatMap { (date, line) ->
            val day = PrayerTimesCalculator.compute(LocalDate.parse(date), config)
            Prayer.entries.zip(line.split(' ')).mapNotNull { (prayer, hm) ->
                val (h, m) = hm.split(':').map(String::toInt)
                val expected = LocalDateTime(LocalDate.parse(date), kotlinx.datetime.LocalTime(h, m)).toInstant(zone)
                val diff = (day[prayer] - expected).inWholeMinutes
                if (abs(diff) <= 2) null else "$date $prayer: ${if (diff > 0) "+" else ""}$diff min"
            }
        }
        assertEquals(emptyList(), misses)
    }
}
