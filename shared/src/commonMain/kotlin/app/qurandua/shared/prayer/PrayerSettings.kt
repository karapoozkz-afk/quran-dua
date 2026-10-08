package app.qurandua.shared.prayer

import kotlinx.serialization.Serializable

/** A place prayer times are computed for: a bundled city or the device location. */
@Serializable
data class Place(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    /** IANA zone, e.g. "Asia/Almaty". */
    val timeZone: String,
    /** ISO 3166 country code, or "" when unknown (device location). */
    val country: String = "",
)

/** The sound of a prayer-time reminder. */
@Serializable
enum class AdhanSound { FULL, SHORT, NOTIFICATION, SILENT }

@Serializable
data class PrayerSettings(
    val place: Place? = null,
    /** Follow the phone's location: prayer times move with the person (Almaty, Bishkek, Tashkent…). */
    val autoLocation: Boolean = true,
    /** Location permission is asked once, on first launch; after a refusal the user picks a city. */
    val askedLocation: Boolean = false,
    /** null = the usual convention of the place's country. */
    val method: CalculationMethod? = null,
    /** null = the method's usual school (Hanafi in Kazakhstan, Pakistan, Russia). */
    val asr: AsrSchool? = null,
    val userOffsets: Map<Prayer, Int> = emptyMap(),
    val notificationsOn: Boolean = false,
    val notify: Set<Prayer> = setOf(Prayer.FAJR, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA),
    /** What plays when a prayer time comes. */
    val adhan: AdhanSound = AdhanSound.FULL,
    /** Show the prayer window over the lock screen, like the call from a mosque. */
    val fullScreen: Boolean = true,
) {
    val effectiveMethod: CalculationMethod get() = method ?: defaultMethodFor(place?.country.orEmpty())
    val effectiveAsr: AsrSchool get() = asr ?: effectiveMethod.defaultAsr

    fun config(): PrayerConfig? = place?.let {
        PrayerConfig(it.latitude, it.longitude, effectiveMethod, effectiveAsr, HighLatitudeRule.ANGLE_BASED, userOffsets)
    }
}

/** The convention most mosques of a country follow. */
fun defaultMethodFor(country: String): CalculationMethod = when (country.uppercase()) {
    "KZ" -> CalculationMethod.KAZAKHSTAN_DUMK
    "KG" -> CalculationMethod.KYRGYZSTAN_DUMK
    "UZ" -> CalculationMethod.UZBEKISTAN_MUSLIM_BOARD
    "RU" -> CalculationMethod.RUSSIA_DUM
    "TR" -> CalculationMethod.TURKEY_DIYANET
    "ID" -> CalculationMethod.INDONESIA_KEMENAG
    "PK" -> CalculationMethod.KARACHI
    "SA" -> CalculationMethod.UMM_AL_QURA
    "EG" -> CalculationMethod.EGYPT
    "US", "CA", "MX", "AR" -> CalculationMethod.ISNA
    else -> CalculationMethod.MUSLIM_WORLD_LEAGUE
}

/** Cities offered without location access: every regional centre of Kazakhstan plus large cities of the app's languages. */
val CITIES: List<Place> = listOf(
    Place("Астана", 51.1282, 71.4304, "Asia/Almaty", "KZ"),
    Place("Алматы", 43.2380, 76.9452, "Asia/Almaty", "KZ"),
    Place("Шымкент", 42.3417, 69.5901, "Asia/Almaty", "KZ"),
    Place("Актобе", 50.2839, 57.1670, "Asia/Aqtobe", "KZ"),
    Place("Караганда", 49.8047, 73.1094, "Asia/Almaty", "KZ"),
    Place("Тараз", 42.9000, 71.3667, "Asia/Almaty", "KZ"),
    Place("Павлодар", 52.2873, 76.9674, "Asia/Almaty", "KZ"),
    Place("Усть-Каменогорск", 49.9483, 82.6279, "Asia/Almaty", "KZ"),
    Place("Семей", 50.4111, 80.2275, "Asia/Almaty", "KZ"),
    Place("Атырау", 47.1167, 51.8833, "Asia/Atyrau", "KZ"),
    Place("Костанай", 53.2144, 63.6246, "Asia/Qostanay", "KZ"),
    Place("Кызылорда", 44.8528, 65.5092, "Asia/Qyzylorda", "KZ"),
    Place("Уральск", 51.2333, 51.3667, "Asia/Oral", "KZ"),
    Place("Петропавловск", 54.8667, 69.1500, "Asia/Almaty", "KZ"),
    Place("Актау", 43.6500, 51.1667, "Asia/Aqtau", "KZ"),
    Place("Туркестан", 43.3000, 68.2500, "Asia/Almaty", "KZ"),
    Place("Талдыкорган", 45.0156, 78.3739, "Asia/Almaty", "KZ"),
    Place("Кокшетау", 53.2833, 69.3833, "Asia/Almaty", "KZ"),
    Place("Жезказган", 47.7833, 67.7667, "Asia/Almaty", "KZ"),
    Place("Бишкек", 42.8746, 74.5698, "Asia/Bishkek", "KG"),
    Place("Ош", 40.5140, 72.8161, "Asia/Bishkek", "KG"),
    Place("Джалал-Абад", 40.9333, 73.0000, "Asia/Bishkek", "KG"),
    Place("Каракол", 42.4907, 78.3936, "Asia/Bishkek", "KG"),
    Place("Нарын", 41.4287, 75.9911, "Asia/Bishkek", "KG"),
    Place("Ташкент", 41.2995, 69.2401, "Asia/Tashkent", "UZ"),
    Place("Самарканд", 39.6542, 66.9597, "Asia/Samarkand", "UZ"),
    Place("Бухара", 39.7681, 64.4556, "Asia/Samarkand", "UZ"),
    Place("Наманган", 40.9983, 71.6726, "Asia/Tashkent", "UZ"),
    Place("Андижан", 40.7821, 72.3442, "Asia/Tashkent", "UZ"),
    Place("Фергана", 40.3864, 71.7864, "Asia/Tashkent", "UZ"),
    Place("Нукус", 42.4619, 59.6166, "Asia/Samarkand", "UZ"),
    Place("Конаев", 43.8667, 77.0667, "Asia/Almaty", "KZ"),
    Place("Москва", 55.7558, 37.6173, "Europe/Moscow", "RU"),
    Place("Казань", 55.7963, 49.1088, "Europe/Moscow", "RU"),
    Place("İstanbul", 41.0082, 28.9784, "Europe/Istanbul", "TR"),
    Place("Ankara", 39.9334, 32.8597, "Europe/Istanbul", "TR"),
    Place("Jakarta", -6.2088, 106.8456, "Asia/Jakarta", "ID"),
    Place("Surabaya", -7.2575, 112.7521, "Asia/Jakarta", "ID"),
    Place("کراچی Karachi", 24.8607, 67.0011, "Asia/Karachi", "PK"),
    Place("لاہور Lahore", 31.5204, 74.3587, "Asia/Karachi", "PK"),
    Place("اسلام آباد Islamabad", 33.6844, 73.0479, "Asia/Karachi", "PK"),
    Place("مكة Makkah", 21.4225, 39.8262, "Asia/Riyadh", "SA"),
    Place("المدينة Madinah", 24.4672, 39.6111, "Asia/Riyadh", "SA"),
    Place("دبي Dubai", 25.2048, 55.2708, "Asia/Dubai", "AE"),
    Place("القاهرة Cairo", 30.0444, 31.2357, "Africa/Cairo", "EG"),
    Place("London", 51.5072, -0.1276, "Europe/London", "GB"),
    Place("Berlin", 52.5200, 13.4050, "Europe/Berlin", "DE"),
    Place("Paris", 48.8566, 2.3522, "Europe/Paris", "FR"),
    Place("Madrid", 40.4168, -3.7038, "Europe/Madrid", "ES"),
    Place("Barcelona", 41.3874, 2.1686, "Europe/Madrid", "ES"),
    Place("Ciudad de México", 19.4326, -99.1332, "America/Mexico_City", "MX"),
    Place("Buenos Aires", -34.6037, -58.3816, "America/Argentina/Buenos_Aires", "AR"),
    Place("New York", 40.7128, -74.0060, "America/New_York", "US"),
)
