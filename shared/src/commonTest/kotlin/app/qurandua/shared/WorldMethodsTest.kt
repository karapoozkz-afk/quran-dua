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
 * Published timetables of national authorities (fajr, sunrise, dhuhr, asr, maghrib, isha), as collected
 * in quran-app/research/prayer-methods-world.md and prayer-times-russia.md. Each method must reproduce them within 2 minutes.
 */
class WorldMethodsTest {

    private class Sample(val method: CalculationMethod, val lat: Double, val lng: Double, val utc: String, val date: String, val times: String)

    private val samples = listOf(
        Sample(CalculationMethod.MALAYSIA_JAKIM, 3.139, 101.687, "UTC+08:00", "2026-01-01", "06:06 07:18 13:19 16:42 19:17 20:31"),
        Sample(CalculationMethod.MALAYSIA_JAKIM, 3.139, 101.687, "UTC+08:00", "2026-06-21", "05:52 07:05 13:18 16:44 19:27 20:43"),
        Sample(CalculationMethod.SINGAPORE_MUIS, 1.3521, 103.8198, "UTC+08:00", "2026-01-01", "05:44 07:08 13:10 16:34 19:11 20:25"),
        Sample(CalculationMethod.INDONESIA_KEMENAG, -6.2088, 106.8456, "UTC+07:00", "2026-02-24", "04:42 05:55 12:09 15:16 18:17 19:26"),
        Sample(CalculationMethod.AZERBAIJAN_QMI, 40.4093, 49.8671, "UTC+04:00", "2026-10-08", "05:25 06:44 12:28 16:27 18:25 19:25"),
        Sample(CalculationMethod.AZERBAIJAN_QMI, 40.4093, 49.8671, "UTC+04:00", "2026-10-22", "05:39 06:59 12:25 16:08 18:04 19:05"),
        Sample(CalculationMethod.TEHRAN_IGUT, 35.6892, 51.3890, "UTC+03:30", "2026-10-07", "04:41 06:04 11:52 15:09 17:58 18:45"),
        Sample(CalculationMethod.MOROCCO_HABOUS, 34.0209, -6.8416, "UTC", "2026-10-07", "04:58 06:23 12:20 15:33 18:07 19:22"),
        Sample(CalculationMethod.MOROCCO_HABOUS, 34.0209, -6.8416, "UTC", "2026-06-15", "03:24 05:12 12:33 16:13 19:44 21:18"),
        Sample(CalculationMethod.BOSNIA_IZ, 43.8563, 18.4131, "UTC+02:00", "2026-06-21", "02:40 04:57 12:49 16:55 20:38 22:34"),
        Sample(CalculationMethod.BOSNIA_IZ, 43.8563, 18.4131, "UTC+01:00", "2026-12-21", "05:33 07:11 11:45 13:55 16:17 17:44"),
        Sample(CalculationMethod.BANGLADESH_IFB, 23.8103, 90.4125, "UTC+06:00", "2026-10-08", "04:40 05:52 11:49 16:00 17:42 18:55"),
        // Russian muftiates (quran-app/research/prayer-times-russia.md), white nights included.
        Sample(CalculationMethod.RUSSIA_DUM_RF, 55.7558, 37.6173, "UTC+03:00", "2025-12-04", "06:21 08:35 12:25 13:44 16:04 17:56"),
        Sample(CalculationMethod.RUSSIA_DUM_RF, 55.7558, 37.6173, "UTC+03:00", "2026-03-18", "04:31 06:32 12:43 15:47 18:44 20:23"),
        Sample(CalculationMethod.RUSSIA_DUM_RF, 55.7558, 37.6173, "UTC+03:00", "2026-05-17", "01:57 04:09 12:31 16:46 20:44 22:46"),
        Sample(CalculationMethod.RUSSIA_DUM_RF, 55.7558, 37.6173, "UTC+03:00", "2026-06-21", "01:49 03:40 12:36 17:03 21:23 23:14"),
        Sample(CalculationMethod.RUSSIA_DUM_RF, 55.7558, 37.6173, "UTC+03:00", "2026-07-15", "02:00 04:01 12:41 17:02 21:09 23:02"),
        Sample(CalculationMethod.RUSSIA_DUM_RF, 55.7558, 37.6173, "UTC+03:00", "2026-08-02", "02:12 04:32 12:41 16:50 20:39 22:37"),
        Sample(CalculationMethod.RUSSIA_DUM_RF, 55.7558, 37.6173, "UTC+03:00", "2026-10-08", "04:43 06:41 12:22 15:04 17:52 19:28"),
        Sample(CalculationMethod.TATARSTAN_DUMRT, 55.7963, 49.1088, "UTC+03:00", "2026-01-15", "05:48 08:04 11:53 13:54 15:43 17:37"),
        Sample(CalculationMethod.TATARSTAN_DUMRT, 55.7963, 49.1088, "UTC+03:00", "2026-03-20", "03:39 05:47 11:51 15:54 17:57 19:41"),
        Sample(CalculationMethod.TATARSTAN_DUMRT, 55.7963, 49.1088, "UTC+03:00", "2026-04-20", "01:46 04:27 11:43 16:39 19:00 21:06"),
        Sample(CalculationMethod.TATARSTAN_DUMRT, 55.7963, 49.1088, "UTC+03:00", "2026-06-21", "00:58 02:59 11:46 17:35 20:33 22:03"),
        Sample(CalculationMethod.TATARSTAN_DUMRT, 55.7963, 49.1088, "UTC+03:00", "2026-07-20", "01:27 03:28 11:50 17:26 20:12 21:42"),
        Sample(CalculationMethod.TATARSTAN_DUMRT, 55.7963, 49.1088, "UTC+03:00", "2026-09-15", "03:02 05:15 11:39 15:55 18:02 19:48"),
        Sample(CalculationMethod.TATARSTAN_DUMRT, 55.7963, 49.1088, "UTC+03:00", "2026-12-21", "05:50 08:12 11:42 13:25 15:12 17:11"),
        Sample(CalculationMethod.DAGESTAN_MUFTIYAT, 42.9849, 47.5047, "UTC+03:00", "2026-01-15", "05:41 07:16 12:04 14:22 16:45 18:04"),
        Sample(CalculationMethod.DAGESTAN_MUFTIYAT, 42.9849, 47.5047, "UTC+03:00", "2026-03-20", "04:21 05:52 12:02 15:22 18:06 19:20"),
        Sample(CalculationMethod.DAGESTAN_MUFTIYAT, 42.9849, 47.5047, "UTC+03:00", "2026-06-21", "01:58 04:09 11:56 15:58 19:37 21:20"),
        Sample(CalculationMethod.DAGESTAN_MUFTIYAT, 42.9849, 47.5047, "UTC+03:00", "2026-09-15", "03:56 05:27 11:50 15:18 18:05 19:20"),
        Sample(CalculationMethod.DAGESTAN_MUFTIYAT, 42.9849, 47.5047, "UTC+03:00", "2026-12-21", "05:38 07:15 11:52 14:02 16:23 17:44"),
        Sample(CalculationMethod.BASHKORTOSTAN_DUMRB, 54.7388, 55.9721, "UTC+05:00", "2026-01-15", "07:33 09:31 13:25 15:30 17:19 19:09"),
        Sample(CalculationMethod.BASHKORTOSTAN_DUMRB, 54.7388, 55.9721, "UTC+05:00", "2026-02-15", "06:51 08:38 13:30 16:28 18:23 20:01"),
        Sample(CalculationMethod.BASHKORTOSTAN_DUMRB, 54.7388, 55.9721, "UTC+05:00", "2026-03-10", "05:58 07:44 13:25 17:09 19:09 20:46"),
        Sample(CalculationMethod.BASHKORTOSTAN_DUMRB, 54.7388, 55.9721, "UTC+05:00", "2026-12-21", "07:34 09:37 13:13 15:02 16:50 18:45"),
    )

    @Test
    fun methodsMatchTheirAuthorities() {
        val misses = samples.flatMap { s ->
            val config = PrayerConfig(s.lat, s.lng, s.method, s.method.defaultAsr)
            val day = PrayerTimesCalculator.compute(LocalDate.parse(s.date), config)
            Prayer.entries.zip(s.times.split(' ')).mapNotNull { (prayer, hm) ->
                val (h, m) = hm.split(':').map(String::toInt)
                val expected = LocalDateTime(LocalDate.parse(s.date), LocalTime(h, m)).toInstant(TimeZone.of(s.utc))
                val diff = (day[prayer] - expected).inWholeMinutes
                if (abs(diff) <= 2) null else "${s.method} ${s.date} $prayer: $diff min"
            }
        }
        assertEquals(emptyList(), misses)
    }
}
