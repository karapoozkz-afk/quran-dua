package app.qurandua.shared

import app.qurandua.shared.prayer.CalculationMethod
import app.qurandua.shared.prayer.Prayer
import app.qurandua.shared.prayer.PrayerConfig
import app.qurandua.shared.prayer.PrayerTimesCalculator
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Official timetables of Kyrgyzstan and Uzbekistan, local time: fajr, sunrise, dhuhr, asr, maghrib, isha.
 * Sources and the fit are in quran-app/research/prayer-times-kg-uz.md.
 */
class KgUzTimetableTest {

    /** muftiyat.kg calendar API, Bishkek (id 1) and Osh (id 2), UTC+6. */
    private val bishkek = mapOf(
        "2026-02-01" to "06:39 08:16 13:15 16:33 18:20 19:39",
        "2026-03-20" to "05:30 07:05 13:09 17:25 19:19 20:36",
        "2026-06-21" to "03:05 05:23 13:03 18:23 20:50 22:40",
        "2026-08-10" to "04:10 06:02 13:07 18:07 20:19 21:49",
        "2026-10-08" to "05:32 07:06 12:49 16:48 18:39 19:55",
        "2026-12-21" to "06:46 08:29 12:59 15:50 17:36 19:01",
    )
    private val osh = mapOf(
        "2026-06-21" to "03:32 05:38 13:11 18:26 20:49 22:30",
        "2026-10-08" to "05:41 07:12 12:56 16:58 18:47 20:00",
        "2026-12-21" to "06:49 08:28 13:07 16:06 17:51 19:12",
    )

    /** Muslim Board of Uzbekistan daily times (muslim.uz / praytime.uz), UTC+5. */
    private val tashkentBoard = mapOf(
        "2026-10-07" to "05:07 06:26 12:11 16:10 17:59 19:14",
        "2026-10-08" to "05:08 06:27 12:11 16:09 17:58 19:12",
    )
    private val samarkandBoard = mapOf("2026-10-08" to "05:18 06:35 12:20 16:20 18:08 19:20")

    /** namoz-vaqti.uz yearly Tashkent table: not the board itself, so a season check with 2 minutes' slack. */
    private val tashkentYear = mapOf(
        "2026-12-21" to "06:19 07:45 12:21 15:16 17:01 18:23",
        "2027-03-21" to "05:06 06:25 12:30 16:47 18:39 19:55",
        "2027-06-21" to "03:04 04:50 12:25 17:40 20:03 21:46",
        "2027-08-05" to "03:50 05:22 12:29 17:29 19:39 21:08",
    )

    private fun misses(table: Map<String, String>, lat: Double, lng: Double, method: CalculationMethod, utcOffset: String, slack: Int): List<String> {
        val config = PrayerConfig(lat, lng, method, method.defaultAsr)
        val zone = TimeZone.of(utcOffset)
        return table.flatMap { (date, line) ->
            val day = PrayerTimesCalculator.compute(LocalDate.parse(date), config)
            Prayer.entries.zip(line.split(' ')).mapNotNull { (prayer, hm) ->
                val (h, m) = hm.split(':').map(String::toInt)
                val expected = LocalDateTime(LocalDate.parse(date), LocalTime(h, m)).toInstant(zone)
                val diff = (day[prayer] - expected).inWholeMinutes
                if (abs(diff) <= slack) null else "$date $prayer: $diff min"
            }
        }
    }

    @Test
    fun kyrgyzstanMatchesMuftiyat() {
        val m = CalculationMethod.KYRGYZSTAN_DUMK
        assertEquals(emptyList(), misses(bishkek, 42.8746, 74.5698, m, "UTC+06:00", 1) + misses(osh, 40.5140, 72.8161, m, "UTC+06:00", 1))
    }

    @Test
    fun uzbekistanMatchesTheMuslimBoard() {
        val m = CalculationMethod.UZBEKISTAN_MUSLIM_BOARD
        assertEquals(
            emptyList(),
            misses(tashkentBoard, 41.2995, 69.2401, m, "UTC+05:00", 1) +
                misses(samarkandBoard, 39.6542, 66.9597, m, "UTC+05:00", 1) +
                misses(tashkentYear, 41.2995, 69.2401, m, "UTC+05:00", 2),
        )
    }
}
