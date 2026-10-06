package app.qurandua.shared

import app.qurandua.shared.prayer.AsrSchool
import app.qurandua.shared.prayer.CalculationMethod
import app.qurandua.shared.prayer.HighLatitudeRule
import app.qurandua.shared.prayer.Prayer
import app.qurandua.shared.prayer.PrayerConfig
import app.qurandua.shared.prayer.PrayerTimesCalculator
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Reference values come from adhanpy (a port of the Adhan library, which uses more precise
 * astronomy), Muslim World League angles 18°/17°, middle-of-the-night high-latitude rule.
 * The on-device calculation must stay within two minutes of it.
 */
class PrayerTimesTest {

    private fun check(lat: Double, lng: Double, date: LocalDate, asr: AsrSchool, expected: List<String>) {
        val config = PrayerConfig(lat, lng, CalculationMethod.MUSLIM_WORLD_LEAGUE, asr, HighLatitudeRule.MIDDLE_OF_NIGHT)
        val times = PrayerTimesCalculator.compute(date, config)
        Prayer.entries.zip(expected).forEach { (prayer, iso) ->
            val diffMinutes = abs((times[prayer] - Instant.parse(iso)).inWholeSeconds) / 60.0
            assertTrue(diffMinutes <= 2, "$prayer at $lat,$lng on $date: ${times[prayer]} vs $iso")
        }
    }

    @Test
    fun almatySummerHanafi() = check(
        43.238, 76.945, LocalDate(2026, 6, 21), AsrSchool.HANAFI,
        listOf("2026-06-20T20:52:00Z", "2026-06-20T23:12:00Z", "2026-06-21T06:54:00Z", "2026-06-21T12:13:00Z", "2026-06-21T14:36:00Z", "2026-06-21T16:45:00Z"),
    )

    @Test
    fun petropavlHighLatitudeSummer() = check(
        54.87, 69.15, LocalDate(2026, 6, 21), AsrSchool.HANAFI,
        listOf("2026-06-20T19:25:00Z", "2026-06-20T22:45:00Z", "2026-06-21T07:25:00Z", "2026-06-21T13:11:00Z", "2026-06-21T16:06:00Z", "2026-06-21T19:25:00Z"),
    )

    @Test
    fun makkahAutumn() = check(
        21.4225, 39.8262, LocalDate(2026, 10, 6), AsrSchool.STANDARD,
        listOf("2026-10-06T01:59:00Z", "2026-10-06T03:13:00Z", "2026-10-06T09:09:00Z", "2026-10-06T12:31:00Z", "2026-10-06T15:04:00Z", "2026-10-06T16:14:00Z"),
    )

    @Test
    fun madridWinterWestOfGreenwich() = check(
        40.4168, -3.7038, LocalDate(2026, 1, 15), AsrSchool.STANDARD,
        listOf("2026-01-15T05:59:00Z", "2026-01-15T07:36:00Z", "2026-01-15T12:24:00Z", "2026-01-15T14:54:00Z", "2026-01-15T17:13:00Z", "2026-01-15T18:44:00Z"),
    )

    @Test
    fun userOffsetsShiftOnlyTheirPrayer() {
        val base = PrayerConfig(43.238, 76.945, CalculationMethod.KAZAKHSTAN_DUMK)
        val date = LocalDate(2026, 10, 6)
        val plain = PrayerTimesCalculator.compute(date, base)
        val shifted = PrayerTimesCalculator.compute(date, base.copy(userOffsets = mapOf(Prayer.FAJR to 3, Prayer.ISHA to -2)))
        assertEquals(3, (shifted[Prayer.FAJR] - plain[Prayer.FAJR]).inWholeMinutes)
        assertEquals(-2, (shifted[Prayer.ISHA] - plain[Prayer.ISHA]).inWholeMinutes)
        assertEquals(plain[Prayer.DHUHR], shifted[Prayer.DHUHR])
    }

    @Test
    fun timesAreInOrderEverywhere() {
        for (lat in listOf(-35.0, 0.0, 25.0, 43.0, 51.0, 55.0)) for (month in 1..12) {
            val t = PrayerTimesCalculator.compute(LocalDate(2026, month, 10), PrayerConfig(lat, 70.0, CalculationMethod.KAZAKHSTAN_DUMK))
            val list = Prayer.entries.map { t[it] }
            assertEquals(list.sorted(), list, "order at lat $lat month $month")
        }
    }
}
