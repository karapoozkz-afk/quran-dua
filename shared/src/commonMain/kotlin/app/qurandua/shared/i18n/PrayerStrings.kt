package app.qurandua.shared.i18n

import app.qurandua.shared.prayer.CalculationMethod
import app.qurandua.shared.prayer.Prayer

/** Strings of the prayer-times screen and its notifications. */
data class PrayerStrings(
    val title: String,
    val next: (prayer: String, time: String) -> String,
    val prayerName: (Prayer) -> String,
    val place: String,
    val chooseCity: String,
    val useLocation: String,
    val noPlace: String,
    val method: String,
    val methodName: (CalculationMethod) -> String,
    val asr: String,
    val asrStandard: String,
    val asrHanafi: String,
    val offsets: String,
    val notifications: String,
    val notificationText: (Prayer) -> String,
    val notificationsDenied: String,
    val locationDenied: String,
    val dumkNote: String,
)

private fun methodNames(dumk: String, mwl: String, isna: String, egypt: String, ummAlQura: String, karachi: String, turkey: String, indonesia: String, russia: String): (CalculationMethod) -> String = { m ->
    when (m) {
        CalculationMethod.KAZAKHSTAN_DUMK -> dumk
        CalculationMethod.MUSLIM_WORLD_LEAGUE -> mwl
        CalculationMethod.ISNA -> isna
        CalculationMethod.EGYPT -> egypt
        CalculationMethod.UMM_AL_QURA -> ummAlQura
        CalculationMethod.KARACHI -> karachi
        CalculationMethod.TURKEY_DIYANET -> turkey
        CalculationMethod.INDONESIA_KEMENAG -> indonesia
        CalculationMethod.RUSSIA_DUM -> russia
    }
}

private fun prayerNames(fajr: String, sunrise: String, dhuhr: String, asr: String, maghrib: String, isha: String): (Prayer) -> String = { p ->
    when (p) {
        Prayer.FAJR -> fajr
        Prayer.SUNRISE -> sunrise
        Prayer.DHUHR -> dhuhr
        Prayer.ASR -> asr
        Prayer.MAGHRIB -> maghrib
        Prayer.ISHA -> isha
    }
}

val EnPrayer = PrayerStrings(
    title = "Prayer times",
    next = { p, t -> "Next: $p at $t" },
    prayerName = prayerNames("Fajr", "Sunrise", "Dhuhr", "Asr", "Maghrib", "Isha"),
    place = "City",
    chooseCity = "Choose a city",
    useLocation = "My location",
    noPlace = "Choose a city to see prayer times. They are calculated on the phone and work offline.",
    method = "Calculation method",
    methodName = methodNames("Kazakhstan (DUMK)", "Muslim World League", "ISNA (North America)", "Egyptian Authority", "Umm al-Qura (Makkah)", "Karachi University", "Diyanet (Turkey)", "Kemenag (Indonesia)", "Russia (DUM)"),
    asr = "Asr",
    asrStandard = "Standard (Shafi‘i, Maliki, Hanbali)",
    asrHanafi = "Hanafi",
    offsets = "Adjust, minutes",
    notifications = "Prayer notifications",
    notificationText = { p -> "It is time for ${prayerNames("Fajr", "Sunrise", "Dhuhr", "Asr", "Maghrib", "Isha")(p)} prayer" },
    notificationsDenied = "Without notification permission there will be no reminders.",
    locationDenied = "Location access was not granted. Choose a city from the list.",
    dumkNote = "Times for Kazakhstan follow the official DUMK (muftyat.kz) timetable, checked against it for Almaty. Small differences of a minute or two are possible in other cities; adjust the minutes if your mosque differs.",
)

val RuPrayer = PrayerStrings(
    title = "Время намаза",
    next = { p, t -> "Следующий: $p в $t" },
    prayerName = prayerNames("Фаджр", "Восход", "Зухр", "Аср", "Магриб", "Иша"),
    place = "Город",
    chooseCity = "Выбрать город",
    useLocation = "Моё местоположение",
    noPlace = "Выберите город, чтобы увидеть время намаза. Оно считается прямо на телефоне и работает без интернета.",
    method = "Метод расчёта",
    methodName = methodNames("Казахстан (ДУМК)", "Всемирная исламская лига", "ISNA (Северная Америка)", "Египет", "Умм аль-Кура (Мекка)", "Университет Карачи", "Диянет (Турция)", "Кеменаг (Индонезия)", "Россия (ДУМ)"),
    asr = "Аср",
    asrStandard = "Стандартный (шафииты, маликиты, ханбалиты)",
    asrHanafi = "Ханафитский",
    offsets = "Поправка, минут",
    notifications = "Уведомления о намазе",
    notificationText = { p -> "Наступило время намаза: ${prayerNames("Фаджр", "Восход", "Зухр", "Аср", "Магриб", "Иша")(p)}" },
    notificationsDenied = "Без разрешения на уведомления напоминаний не будет.",
    locationDenied = "Доступ к местоположению не дан. Выберите город из списка.",
    dumkNote = "Время для Казахстана считается по правилам официального расписания ДУМК (muftyat.kz) и сверено с ним для Алматы. В других городах возможна разница в минуту-две; если в вашей мечети время другое, поправьте минуты.",
)

val KkPrayer = PrayerStrings(
    title = "Намаз уақыты",
    next = { p, t -> "Келесі: $p, $t" },
    prayerName = prayerNames("Таң", "Күн шығуы", "Бесін", "Екінті", "Ақшам", "Құптан"),
    place = "Қала",
    chooseCity = "Қаланы таңдау",
    useLocation = "Менің орным",
    noPlace = "Намаз уақытын көру үшін қаланы таңдаңыз. Уақыт телефонның өзінде есептеледі және интернетсіз жұмыс істейді.",
    method = "Есептеу әдісі",
    methodName = methodNames("Қазақстан (ҚМДБ)", "Дүниежүзілік ислам лигасы", "ISNA (Солтүстік Америка)", "Мысыр", "Умм әл-Құра (Мекке)", "Карачи университеті", "Диянет (Түркия)", "Кеменаг (Индонезия)", "Ресей (МДБ)"),
    asr = "Екінті",
    asrStandard = "Стандартты (шафиғи, мәлики, ханбали)",
    asrHanafi = "Ханафи",
    offsets = "Түзету, минут",
    notifications = "Намаз туралы хабарламалар",
    notificationText = { p -> "Намаз уақыты кірді: ${prayerNames("Таң", "Күн шығуы", "Бесін", "Екінті", "Ақшам", "Құптан")(p)}" },
    notificationsDenied = "Хабарламаға рұқсат болмаса, еске салу болмайды.",
    locationDenied = "Орналасқан жерге рұқсат берілмеді. Тізімнен қаланы таңдаңыз.",
    dumkNote = "Қазақстан үшін уақыт ҚМДБ (muftyat.kz) ресми кестесінің ережесімен есептеледі және Алматы бойынша онымен салыстырылды. Басқа қалаларда бір-екі минут айырмашылық болуы мүмкін; мешітіңіздің уақыты басқа болса, минуттарды түзетіңіз.",
)

val TrPrayer = PrayerStrings(
    title = "Namaz vakitleri",
    next = { p, t -> "Sıradaki: $p, $t" },
    prayerName = prayerNames("İmsak", "Güneş", "Öğle", "İkindi", "Akşam", "Yatsı"),
    place = "Şehir",
    chooseCity = "Şehir seç",
    useLocation = "Konumum",
    noPlace = "Namaz vakitlerini görmek için bir şehir seçin. Vakitler telefonda hesaplanır ve internetsiz çalışır.",
    method = "Hesaplama yöntemi",
    methodName = methodNames("Kazakistan (DUMK)", "Dünya Müslüman Birliği", "ISNA (Kuzey Amerika)", "Mısır", "Ümmü'l-Kurâ (Mekke)", "Karaçi Üniversitesi", "Diyanet (Türkiye)", "Kemenag (Endonezya)", "Rusya (DUM)"),
    asr = "İkindi",
    asrStandard = "Standart (Şâfiî, Mâlikî, Hanbelî)",
    asrHanafi = "Hanefî",
    offsets = "Düzeltme, dakika",
    notifications = "Namaz bildirimleri",
    notificationText = { p -> "Namaz vakti girdi: ${prayerNames("İmsak", "Güneş", "Öğle", "İkindi", "Akşam", "Yatsı")(p)}" },
    notificationsDenied = "Bildirim izni olmadan hatırlatma gelmez.",
    locationDenied = "Konum izni verilmedi. Listeden bir şehir seçin.",
    dumkNote = "Kazakistan vakitleri resmî DUMK (muftyat.kz) takvimine göre hesaplanır ve Almatı için onunla karşılaştırılmıştır. Diğer şehirlerde bir iki dakikalık fark olabilir; camiinizin vakti farklıysa dakikaları düzeltin.",
)

val IdPrayer = PrayerStrings(
    title = "Jadwal salat",
    next = { p, t -> "Berikutnya: $p pukul $t" },
    prayerName = prayerNames("Subuh", "Terbit", "Zuhur", "Asar", "Magrib", "Isya"),
    place = "Kota",
    chooseCity = "Pilih kota",
    useLocation = "Lokasi saya",
    noPlace = "Pilih kota untuk melihat jadwal salat. Jadwal dihitung di ponsel dan berfungsi tanpa internet.",
    method = "Metode perhitungan",
    methodName = methodNames("Kazakhstan (DUMK)", "Liga Muslim Dunia", "ISNA (Amerika Utara)", "Mesir", "Umm al-Qura (Makkah)", "Universitas Karachi", "Diyanet (Turki)", "Kemenag (Indonesia)", "Rusia (DUM)"),
    asr = "Asar",
    asrStandard = "Standar (Syafi'i, Maliki, Hanbali)",
    asrHanafi = "Hanafi",
    offsets = "Koreksi, menit",
    notifications = "Notifikasi salat",
    notificationText = { p -> "Waktu salat ${prayerNames("Subuh", "Terbit", "Zuhur", "Asar", "Magrib", "Isya")(p)} telah tiba" },
    notificationsDenied = "Tanpa izin notifikasi, pengingat tidak akan muncul.",
    locationDenied = "Izin lokasi tidak diberikan. Pilih kota dari daftar.",
    dumkNote = "Jadwal untuk Kazakhstan mengikuti jadwal resmi DUMK (muftyat.kz) dan sudah dicocokkan untuk Almaty. Di kota lain bisa berbeda satu atau dua menit; koreksi menitnya bila masjid Anda berbeda.",
)

val UrPrayer = PrayerStrings(
    title = "نماز کے اوقات",
    next = { p, t -> "اگلی: $p، $t" },
    prayerName = prayerNames("فجر", "طلوع آفتاب", "ظہر", "عصر", "مغرب", "عشاء"),
    place = "شہر",
    chooseCity = "شہر منتخب کریں",
    useLocation = "میرا مقام",
    noPlace = "نماز کے اوقات دیکھنے کے لیے شہر منتخب کریں۔ اوقات فون پر ہی حساب ہوتے ہیں اور انٹرنیٹ کے بغیر کام کرتے ہیں۔",
    method = "حساب کا طریقہ",
    methodName = methodNames("قازقستان (DUMK)", "مسلم ورلڈ لیگ", "ISNA (شمالی امریکہ)", "مصر", "ام القریٰ (مکہ)", "جامعہ کراچی", "دیانت (ترکی)", "کیمیناگ (انڈونیشیا)", "روس (DUM)"),
    asr = "عصر",
    asrStandard = "عام (شافعی، مالکی، حنبلی)",
    asrHanafi = "حنفی",
    offsets = "تصحیح، منٹ",
    notifications = "نماز کی اطلاعات",
    notificationText = { p -> "${prayerNames("فجر", "طلوع آفتاب", "ظہر", "عصر", "مغرب", "عشاء")(p)} کی نماز کا وقت ہو گیا" },
    notificationsDenied = "اطلاعات کی اجازت کے بغیر یاد دہانی نہیں آئے گی۔",
    locationDenied = "مقام کی اجازت نہیں ملی۔ فہرست سے شہر منتخب کریں۔",
    dumkNote = "قازقستان کے اوقات DUMK (muftyat.kz) کے سرکاری نظام الاوقات کے مطابق ہیں اور الماتی کے لیے اس سے ملائے گئے ہیں۔ دوسرے شہروں میں ایک دو منٹ کا فرق ہو سکتا ہے؛ اپنی مسجد کے مطابق منٹ درست کر لیں۔",
)

val ArPrayer = PrayerStrings(
    title = "مواقيت الصلاة",
    next = { p, t -> "القادمة: $p في $t" },
    prayerName = prayerNames("الفجر", "الشروق", "الظهر", "العصر", "المغرب", "العشاء"),
    place = "المدينة",
    chooseCity = "اختر المدينة",
    useLocation = "موقعي",
    noPlace = "اختر مدينة لعرض مواقيت الصلاة. تُحسب المواقيت على الهاتف وتعمل دون إنترنت.",
    method = "طريقة الحساب",
    methodName = methodNames("كازاخستان (الإدارة الدينية)", "رابطة العالم الإسلامي", "ISNA (أمريكا الشمالية)", "الهيئة المصرية", "أم القرى (مكة)", "جامعة كراتشي", "ديانت (تركيا)", "وزارة الشؤون الدينية (إندونيسيا)", "روسيا"),
    asr = "العصر",
    asrStandard = "الجمهور (الشافعي والمالكي والحنبلي)",
    asrHanafi = "الحنفي",
    offsets = "تعديل بالدقائق",
    notifications = "تنبيهات الصلاة",
    notificationText = { p -> "حان وقت صلاة ${prayerNames("الفجر", "الشروق", "الظهر", "العصر", "المغرب", "العشاء")(p)}" },
    notificationsDenied = "دون إذن التنبيهات لن تصل التذكيرات.",
    locationDenied = "لم يُمنح إذن الموقع. اختر مدينة من القائمة.",
    dumkNote = "مواقيت كازاخستان تتبع الجدول الرسمي للإدارة الدينية (muftyat.kz) وقد طوبقت معه في ألماتي. قد يختلف الوقت دقيقة أو دقيقتين في مدن أخرى؛ عدّل الدقائق إن اختلف مسجدك.",
)

val EsPrayer = PrayerStrings(
    title = "Horarios de oración",
    next = { p, t -> "Siguiente: $p a las $t" },
    prayerName = prayerNames("Fayr", "Salida del sol", "Duhr", "Asr", "Magrib", "Isha"),
    place = "Ciudad",
    chooseCity = "Elegir ciudad",
    useLocation = "Mi ubicación",
    noPlace = "Elige una ciudad para ver los horarios de oración. Se calculan en el teléfono y funcionan sin internet.",
    method = "Método de cálculo",
    methodName = methodNames("Kazajistán (DUMK)", "Liga Musulmana Mundial", "ISNA (Norteamérica)", "Autoridad egipcia", "Umm al-Qura (La Meca)", "Universidad de Karachi", "Diyanet (Turquía)", "Kemenag (Indonesia)", "Rusia (DUM)"),
    asr = "Asr",
    asrStandard = "Estándar (shafi‘í, malikí, hanbalí)",
    asrHanafi = "Hanafí",
    offsets = "Ajuste, minutos",
    notifications = "Avisos de oración",
    notificationText = { p -> "Es la hora de la oración: ${prayerNames("Fayr", "Salida del sol", "Duhr", "Asr", "Magrib", "Isha")(p)}" },
    notificationsDenied = "Sin permiso de notificaciones no habrá recordatorios.",
    locationDenied = "No se concedió acceso a la ubicación. Elige una ciudad de la lista.",
    dumkNote = "Los horarios de Kazajistán siguen el calendario oficial de la DUMK (muftyat.kz) y se han comprobado con él para Almaty. En otras ciudades puede haber uno o dos minutos de diferencia; ajusta los minutos si tu mezquita difiere.",
)

fun prayerStringsFor(language: String): PrayerStrings = when (language) {
    "ru" -> RuPrayer
    "kk" -> KkPrayer
    "tr" -> TrPrayer
    "id" -> IdPrayer
    "ur" -> UrPrayer
    "ar" -> ArPrayer
    "es" -> EsPrayer
    else -> EnPrayer
}
