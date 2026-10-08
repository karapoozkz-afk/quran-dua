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
    /** Region (federal subject, province) as the geocoder names it; picks the board inside Russia. */
    val region: String = "",
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
    val effectiveMethod: CalculationMethod get() = method ?: defaultMethodFor(place?.country.orEmpty(), place?.region.orEmpty())
    val effectiveAsr: AsrSchool get() = asr ?: effectiveMethod.defaultAsr

    fun config(): PrayerConfig? = place?.let {
        PrayerConfig(it.latitude, it.longitude, effectiveMethod, effectiveAsr, HighLatitudeRule.ANGLE_BASED, userOffsets)
    }
}

/** The convention most mosques of a country follow. */
fun defaultMethodFor(country: String, region: String = ""): CalculationMethod = when (country.uppercase()) {
    "RU" -> russianBoardFor(region)
    "KZ" -> CalculationMethod.KAZAKHSTAN_DUMK
    "KG" -> CalculationMethod.KYRGYZSTAN_DUMK
    "UZ" -> CalculationMethod.UZBEKISTAN_MUSLIM_BOARD
    "TJ" -> CalculationMethod.TAJIKISTAN_ULAMO
    "AZ" -> CalculationMethod.AZERBAIJAN_QMI
    // Turkish mosques in Western Europe follow Diyanet's timetable (low confidence).
    "TR", "DE", "AT", "NL", "BE" -> CalculationMethod.TURKEY_DIYANET
    "MY" -> CalculationMethod.MALAYSIA_JAKIM
    "SG", "BN" -> CalculationMethod.SINGAPORE_MUIS
    "ID" -> CalculationMethod.INDONESIA_KEMENAG
    "BD" -> CalculationMethod.BANGLADESH_IFB
    // No official tables found: the regional Hanafi convention.
    "PK", "IN", "AF", "TM" -> CalculationMethod.KARACHI
    "IR" -> CalculationMethod.TEHRAN_IGUT
    "SA", "QA" -> CalculationMethod.UMM_AL_QURA
    "AE" -> CalculationMethod.UAE_AWQAF
    "KW" -> CalculationMethod.KUWAIT
    "OM" -> CalculationMethod.OMAN_MARA
    "JO" -> CalculationMethod.JORDAN_AWQAF
    "EG" -> CalculationMethod.EGYPT
    "MA" -> CalculationMethod.MOROCCO_HABOUS
    "DZ" -> CalculationMethod.ALGERIA
    "TN" -> CalculationMethod.TUNISIA
    "BA" -> CalculationMethod.BOSNIA_IZ
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
    Place("Душанбе", 38.5598, 68.7870, "Asia/Dushanbe", "TJ"),
    Place("Худжанд", 40.2826, 69.6222, "Asia/Dushanbe", "TJ"),
    Place("Bakı", 40.4093, 49.8671, "Asia/Baku", "AZ"),
    Place("Ашхабад", 37.9601, 58.3261, "Asia/Ashgabat", "TM"),
    Place("Конаев", 43.8667, 77.0667, "Asia/Almaty", "KZ"),
    Place("Москва", 55.7558, 37.6173, "Europe/Moscow", "RU", "Москва"),
    Place("Санкт-Петербург", 59.9343, 30.3351, "Europe/Moscow", "RU", "Санкт-Петербург"),
    Place("Казань", 55.7963, 49.1088, "Europe/Moscow", "RU", "Татарстан"),
    Place("Набережные Челны", 55.7436, 52.3958, "Europe/Moscow", "RU", "Татарстан"),
    Place("Альметьевск", 54.9014, 52.2973, "Europe/Moscow", "RU", "Татарстан"),
    Place("Уфа", 54.7388, 55.9721, "Asia/Yekaterinburg", "RU", "Башкортостан"),
    Place("Стерлитамак", 53.6305, 55.9306, "Asia/Yekaterinburg", "RU", "Башкортостан"),
    Place("Махачкала", 42.9849, 47.5047, "Europe/Moscow", "RU", "Дагестан"),
    Place("Дербент", 42.0578, 48.2885, "Europe/Moscow", "RU", "Дагестан"),
    Place("Грозный", 43.3178, 45.6982, "Europe/Moscow", "RU", "Чечня"),
    Place("Нальчик", 43.4853, 43.6071, "Europe/Moscow", "RU", "Кабардино-Балкария"),
    Place("Магас", 43.1715, 44.8100, "Europe/Moscow", "RU", "Ингушетия"),
    Place("Черкесск", 44.2269, 42.0578, "Europe/Moscow", "RU", "Карачаево-Черкесия"),
    Place("Астрахань", 46.3497, 48.0408, "Europe/Astrakhan", "RU", "Астраханская область"),
    Place("Оренбург", 51.7682, 55.0970, "Asia/Yekaterinburg", "RU", "Оренбургская область"),
    Place("Екатеринбург", 56.8389, 60.6057, "Asia/Yekaterinburg", "RU", "Свердловская область"),
    Place("Челябинск", 55.1644, 61.4368, "Asia/Yekaterinburg", "RU", "Челябинская область"),
    Place("Тюмень", 57.1530, 65.5343, "Asia/Yekaterinburg", "RU", "Тюменская область"),
    Place("Новосибирск", 55.0084, 82.9357, "Asia/Novosibirsk", "RU", "Новосибирская область"),
    Place("Самара", 53.1959, 50.1002, "Europe/Samara", "RU", "Самарская область"),
    Place("Нижний Новгород", 56.2965, 43.9361, "Europe/Moscow", "RU", "Нижегородская область"),
    Place("Пенза", 53.1959, 45.0183, "Europe/Moscow", "RU", "Пензенская область"),
    Place("Ульяновск", 54.3142, 48.4031, "Europe/Ulyanovsk", "RU", "Ульяновская область"),
    Place("Симферополь", 44.9521, 34.1024, "Europe/Simferopol", "RU", "Крым"),
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
    Place("أبو ظبي Abu Dhabi", 24.4539, 54.3773, "Asia/Dubai", "AE"),
    Place("الدوحة Doha", 25.2854, 51.5310, "Asia/Qatar", "QA"),
    Place("الكويت Kuwait", 29.3759, 47.9774, "Asia/Kuwait", "KW"),
    Place("مسقط Muscat", 23.5880, 58.3829, "Asia/Muscat", "OM"),
    Place("عمّان Amman", 31.9539, 35.9106, "Asia/Amman", "JO"),
    Place("تهران Tehran", 35.6892, 51.3890, "Asia/Tehran", "IR"),
    Place("Kuala Lumpur", 3.1390, 101.6869, "Asia/Kuala_Lumpur", "MY"),
    Place("Singapore", 1.3521, 103.8198, "Asia/Singapore", "SG"),
    Place("ঢাকা Dhaka", 23.8103, 90.4125, "Asia/Dhaka", "BD"),
    Place("الرباط Rabat", 34.0209, -6.8416, "Africa/Casablanca", "MA"),
    Place("Casablanca", 33.5731, -7.5898, "Africa/Casablanca", "MA"),
    Place("الجزائر Alger", 36.7538, 3.0588, "Africa/Algiers", "DZ"),
    Place("تونس Tunis", 36.8065, 10.1815, "Africa/Tunis", "TN"),
    Place("Sarajevo", 43.8563, 18.4131, "Europe/Sarajevo", "BA"),
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

/**
 * Russia has several muftiates with their own timetables. Tatarstan, Bashkortostan and Dagestan
 * follow their republic's board; elsewhere the DUM RF timetable. The region name may come in
 * Russian or English, depending on the phone.
 */
fun russianBoardFor(region: String): CalculationMethod {
    val r = region.lowercase()
    return when {
        "татарстан" in r || "tatarstan" in r -> CalculationMethod.TATARSTAN_DUMRT
        "башкортостан" in r || "bashkortostan" in r || "башкирия" in r -> CalculationMethod.BASHKORTOSTAN_DUMRB
        "дагестан" in r || "dagestan" in r -> CalculationMethod.DAGESTAN_MUFTIYAT
        else -> CalculationMethod.RUSSIA_DUM_RF
    }
}
