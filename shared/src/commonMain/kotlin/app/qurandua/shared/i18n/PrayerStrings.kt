package app.qurandua.shared.i18n

import app.qurandua.shared.prayer.AdhanSound
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
    val adhan: String,
    val adhanName: (AdhanSound) -> String,
    val listen: String,
    val stop: String,
    val fullScreen: String,
    val fullScreenDenied: String,
    val exactDenied: String,
    val openSettings: String,
    val fajrNote: String,
    val adhanCredit: String,
)

private fun methodNames(kyrgyzstan: String, uzbekistan: String, dumk: String, mwl: String, isna: String, egypt: String, ummAlQura: String, karachi: String, turkey: String, indonesia: String, russia: String): (CalculationMethod) -> String = { m ->
    when (m) {
        CalculationMethod.KAZAKHSTAN_DUMK -> dumk
        CalculationMethod.KYRGYZSTAN_DUMK -> kyrgyzstan
        CalculationMethod.UZBEKISTAN_MUSLIM_BOARD -> uzbekistan
        CalculationMethod.MUSLIM_WORLD_LEAGUE -> mwl
        CalculationMethod.ISNA -> isna
        CalculationMethod.EGYPT -> egypt
        CalculationMethod.UMM_AL_QURA -> ummAlQura
        CalculationMethod.KARACHI -> karachi
        CalculationMethod.TURKEY_DIYANET -> turkey
        CalculationMethod.INDONESIA_KEMENAG -> indonesia
        CalculationMethod.RUSSIA_DUM -> russia
        else -> m.authority
    }
}

private fun adhanNames(full: String, short: String, notification: String, silent: String): (AdhanSound) -> String = { a ->
    when (a) {
        AdhanSound.FULL -> full
        AdhanSound.SHORT -> short
        AdhanSound.NOTIFICATION -> notification
        AdhanSound.SILENT -> silent
    }
}

/** Licence attribution for the bundled adhan; shown with the other licences on the More screen. */
const val ADHAN_CREDIT = "Aaqib Azeez, Wikimedia Commons, CC BY-SA 4.0"

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
    methodName = methodNames("Kyrgyzstan (Muftiyat)", "Uzbekistan (Muslim Board)", "Kazakhstan (DUMK)", "Muslim World League", "ISNA (North America)", "Egyptian Authority", "Umm al-Qura (Makkah)", "Karachi University", "Diyanet (Turkey)", "Kemenag (Indonesia)", "Russia (DUM)"),
    asr = "Asr",
    asrStandard = "Standard (Shafi‘i, Maliki, Hanbali)",
    asrHanafi = "Hanafi",
    offsets = "Adjust, minutes",
    notifications = "Prayer notifications",
    notificationText = { p -> "It is time for ${prayerNames("Fajr", "Sunrise", "Dhuhr", "Asr", "Maghrib", "Isha")(p)} prayer" },
    notificationsDenied = "Without notification permission there will be no reminders.",
    locationDenied = "Location access was not granted. Choose a city from the list.",
    dumkNote = "Times for Kazakhstan follow the official DUMK (muftyat.kz) timetable, checked against it for Almaty. Small differences of a minute or two are possible in other cities; adjust the minutes if your mosque differs.",
    adhan = "Adhan at prayer time",
    adhanName = adhanNames("Full adhan", "Short (20 s)", "Notification sound", "Silent"),
    listen = "Listen",
    stop = "Stop",
    fullScreen = "Show prayer window on the lock screen",
    fullScreenDenied = "Android has not allowed this app to open full-screen. The adhan will still play, with a regular notification.",
    exactDenied = "Android is not allowing exact alarms for this app, so the adhan may come a few minutes late or only as a notification.",
    openSettings = "Allow",
    fajrNote = "Fajr uses the same adhan: there is no freely licensed recording of the Fajr adhan (with “as-salatu khayrun min an-nawm”) yet.",
    adhanCredit = ADHAN_CREDIT,
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
    methodName = methodNames("Кыргызстан (ДУМК)", "Узбекистан (Управление мусульман)", "Казахстан (ДУМК)", "Всемирная исламская лига", "ISNA (Северная Америка)", "Египет", "Умм аль-Кура (Мекка)", "Университет Карачи", "Диянет (Турция)", "Кеменаг (Индонезия)", "Россия (ДУМ)"),
    asr = "Аср",
    asrStandard = "Стандартный (шафииты, маликиты, ханбалиты)",
    asrHanafi = "Ханафитский",
    offsets = "Поправка, минут",
    notifications = "Уведомления о намазе",
    notificationText = { p -> "Наступило время намаза: ${prayerNames("Фаджр", "Восход", "Зухр", "Аср", "Магриб", "Иша")(p)}" },
    notificationsDenied = "Без разрешения на уведомления напоминаний не будет.",
    locationDenied = "Доступ к местоположению не дан. Выберите город из списка.",
    dumkNote = "Время для Казахстана считается по правилам официального расписания ДУМК (muftyat.kz) и сверено с ним для Алматы. В других городах возможна разница в минуту-две; если в вашей мечети время другое, поправьте минуты.",
    adhan = "Азан при наступлении намаза",
    adhanName = adhanNames("Полный азан", "Короткий (20 с)", "Звук уведомления", "Без звука"),
    listen = "Прослушать",
    stop = "Остановить",
    fullScreen = "Показывать окно намаза на заблокированном экране",
    fullScreenDenied = "Android не разрешил приложению открываться во весь экран. Азан всё равно прозвучит, с обычным уведомлением.",
    exactDenied = "Android не разрешил приложению точные будильники: азан может прозвучать с опозданием на несколько минут или прийти только уведомлением.",
    openSettings = "Разрешить",
    fajrNote = "Для фаджра звучит тот же азан: свободной записи фаджр-азана (со словами «ас-саляту хайрун мин ан-наум») пока нет.",
    adhanCredit = ADHAN_CREDIT,
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
    methodName = methodNames("Қырғызстан (ДМБ)", "Өзбекстан (Мұсылмандар басқармасы)", "Қазақстан (ҚМДБ)", "Дүниежүзілік ислам лигасы", "ISNA (Солтүстік Америка)", "Мысыр", "Умм әл-Құра (Мекке)", "Карачи университеті", "Диянет (Түркия)", "Кеменаг (Индонезия)", "Ресей (МДБ)"),
    asr = "Екінті",
    asrStandard = "Стандартты (шафиғи, мәлики, ханбали)",
    asrHanafi = "Ханафи",
    offsets = "Түзету, минут",
    notifications = "Намаз туралы хабарламалар",
    notificationText = { p -> "Намаз уақыты кірді: ${prayerNames("Таң", "Күн шығуы", "Бесін", "Екінті", "Ақшам", "Құптан")(p)}" },
    notificationsDenied = "Хабарламаға рұқсат болмаса, еске салу болмайды.",
    locationDenied = "Орналасқан жерге рұқсат берілмеді. Тізімнен қаланы таңдаңыз.",
    dumkNote = "Қазақстан үшін уақыт ҚМДБ (muftyat.kz) ресми кестесінің ережесімен есептеледі және Алматы бойынша онымен салыстырылды. Басқа қалаларда бір-екі минут айырмашылық болуы мүмкін; мешітіңіздің уақыты басқа болса, минуттарды түзетіңіз.",
    adhan = "Намаз уақыты кіргенде азан",
    adhanName = adhanNames("Толық азан", "Қысқа (20 с)", "Хабарлама дыбысы", "Дыбыссыз"),
    listen = "Тыңдау",
    stop = "Тоқтату",
    fullScreen = "Намаз терезесін құлыпталған экранда көрсету",
    fullScreenDenied = "Android қосымшаға толық экранда ашылуға рұқсат бермеді. Азан бәрібір қарапайым хабарламамен естіледі.",
    exactDenied = "Android қосымшаға дәл оятқышқа рұқсат бермеді: азан бірнеше минут кешігуі немесе тек хабарлама болып келуі мүмкін.",
    openSettings = "Рұқсат беру",
    fajrNote = "Таң намазына да осы азан естіледі: «әс-салату хайрун минән-науым» сөздері бар таң азанының еркін лицензиялы жазбасы әзірге жоқ.",
    adhanCredit = ADHAN_CREDIT,
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
    methodName = methodNames("Kırgızistan (Müftülük)", "Özbekistan (Müslümanlar İdaresi)", "Kazakistan (DUMK)", "Dünya Müslüman Birliği", "ISNA (Kuzey Amerika)", "Mısır", "Ümmü'l-Kurâ (Mekke)", "Karaçi Üniversitesi", "Diyanet (Türkiye)", "Kemenag (Endonezya)", "Rusya (DUM)"),
    asr = "İkindi",
    asrStandard = "Standart (Şâfiî, Mâlikî, Hanbelî)",
    asrHanafi = "Hanefî",
    offsets = "Düzeltme, dakika",
    notifications = "Namaz bildirimleri",
    notificationText = { p -> "Namaz vakti girdi: ${prayerNames("İmsak", "Güneş", "Öğle", "İkindi", "Akşam", "Yatsı")(p)}" },
    notificationsDenied = "Bildirim izni olmadan hatırlatma gelmez.",
    locationDenied = "Konum izni verilmedi. Listeden bir şehir seçin.",
    dumkNote = "Kazakistan vakitleri resmî DUMK (muftyat.kz) takvimine göre hesaplanır ve Almatı için onunla karşılaştırılmıştır. Diğer şehirlerde bir iki dakikalık fark olabilir; camiinizin vakti farklıysa dakikaları düzeltin.",
    adhan = "Vakit girince ezan",
    adhanName = adhanNames("Tam ezan", "Kısa (20 sn)", "Bildirim sesi", "Sessiz"),
    listen = "Dinle",
    stop = "Durdur",
    fullScreen = "Namaz penceresini kilit ekranında göster",
    fullScreenDenied = "Android bu uygulamanın tam ekran açılmasına izin vermedi. Ezan yine normal bir bildirimle okunur.",
    exactDenied = "Android bu uygulamaya tam zamanlı alarm izni vermedi: ezan birkaç dakika gecikebilir ya da yalnızca bildirim olarak gelebilir.",
    openSettings = "İzin ver",
    fajrNote = "Sabah için de aynı ezan okunur: “es-salâtu hayrun mine’n-nevm” içeren serbest lisanslı bir sabah ezanı kaydı henüz yok.",
    adhanCredit = ADHAN_CREDIT,
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
    methodName = methodNames("Kirgizstan (Muftiyat)", "Uzbekistan (Dewan Muslim)", "Kazakhstan (DUMK)", "Liga Muslim Dunia", "ISNA (Amerika Utara)", "Mesir", "Umm al-Qura (Makkah)", "Universitas Karachi", "Diyanet (Turki)", "Kemenag (Indonesia)", "Rusia (DUM)"),
    asr = "Asar",
    asrStandard = "Standar (Syafi'i, Maliki, Hanbali)",
    asrHanafi = "Hanafi",
    offsets = "Koreksi, menit",
    notifications = "Notifikasi salat",
    notificationText = { p -> "Waktu salat ${prayerNames("Subuh", "Terbit", "Zuhur", "Asar", "Magrib", "Isya")(p)} telah tiba" },
    notificationsDenied = "Tanpa izin notifikasi, pengingat tidak akan muncul.",
    locationDenied = "Izin lokasi tidak diberikan. Pilih kota dari daftar.",
    dumkNote = "Jadwal untuk Kazakhstan mengikuti jadwal resmi DUMK (muftyat.kz) dan sudah dicocokkan untuk Almaty. Di kota lain bisa berbeda satu atau dua menit; koreksi menitnya bila masjid Anda berbeda.",
    adhan = "Azan saat masuk waktu salat",
    adhanName = adhanNames("Azan lengkap", "Pendek (20 dtk)", "Suara notifikasi", "Senyap"),
    listen = "Dengarkan",
    stop = "Hentikan",
    fullScreen = "Tampilkan jendela salat di layar kunci",
    fullScreenDenied = "Android belum mengizinkan aplikasi ini tampil layar penuh. Azan tetap berbunyi dengan notifikasi biasa.",
    exactDenied = "Android belum mengizinkan alarm tepat waktu untuk aplikasi ini: azan bisa terlambat beberapa menit atau hanya muncul sebagai notifikasi.",
    openSettings = "Izinkan",
    fajrNote = "Subuh memakai azan yang sama: belum ada rekaman azan Subuh (dengan “ash-shalatu khairun minan-naum”) berlisensi bebas.",
    adhanCredit = ADHAN_CREDIT,
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
    methodName = methodNames("کرغزستان (مفتیات)", "ازبکستان (مسلم بورڈ)", "قازقستان (DUMK)", "مسلم ورلڈ لیگ", "ISNA (شمالی امریکہ)", "مصر", "ام القریٰ (مکہ)", "جامعہ کراچی", "دیانت (ترکی)", "کیمیناگ (انڈونیشیا)", "روس (DUM)"),
    asr = "عصر",
    asrStandard = "عام (شافعی، مالکی، حنبلی)",
    asrHanafi = "حنفی",
    offsets = "تصحیح، منٹ",
    notifications = "نماز کی اطلاعات",
    notificationText = { p -> "${prayerNames("فجر", "طلوع آفتاب", "ظہر", "عصر", "مغرب", "عشاء")(p)} کی نماز کا وقت ہو گیا" },
    notificationsDenied = "اطلاعات کی اجازت کے بغیر یاد دہانی نہیں آئے گی۔",
    locationDenied = "مقام کی اجازت نہیں ملی۔ فہرست سے شہر منتخب کریں۔",
    dumkNote = "قازقستان کے اوقات DUMK (muftyat.kz) کے سرکاری نظام الاوقات کے مطابق ہیں اور الماتی کے لیے اس سے ملائے گئے ہیں۔ دوسرے شہروں میں ایک دو منٹ کا فرق ہو سکتا ہے؛ اپنی مسجد کے مطابق منٹ درست کر لیں۔",
    adhan = "نماز کے وقت اذان",
    adhanName = adhanNames("مکمل اذان", "مختصر (20 سیکنڈ)", "نوٹیفکیشن کی آواز", "خاموش"),
    listen = "سنیں",
    stop = "روکیں",
    fullScreen = "لاک اسکرین پر نماز کی ونڈو دکھائیں",
    fullScreenDenied = "اینڈرائیڈ نے اس ایپ کو پوری اسکرین پر کھلنے کی اجازت نہیں دی۔ اذان پھر بھی عام نوٹیفکیشن کے ساتھ بجے گی۔",
    exactDenied = "اینڈرائیڈ نے اس ایپ کو درست الارم کی اجازت نہیں دی: اذان چند منٹ دیر سے بج سکتی ہے یا صرف نوٹیفکیشن آ سکتا ہے۔",
    openSettings = "اجازت دیں",
    fajrNote = "فجر میں بھی یہی اذان بجے گی: فجر کی اذان («الصلاۃ خیر من النوم» کے ساتھ) کی آزاد لائسنس والی ریکارڈنگ ابھی دستیاب نہیں۔",
    adhanCredit = ADHAN_CREDIT,
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
    methodName = methodNames("قيرغيزستان (الإفتاء)", "أوزبكستان (إدارة المسلمين)", "كازاخستان (الإدارة الدينية)", "رابطة العالم الإسلامي", "ISNA (أمريكا الشمالية)", "الهيئة المصرية", "أم القرى (مكة)", "جامعة كراتشي", "ديانت (تركيا)", "وزارة الشؤون الدينية (إندونيسيا)", "روسيا"),
    asr = "العصر",
    asrStandard = "الجمهور (الشافعي والمالكي والحنبلي)",
    asrHanafi = "الحنفي",
    offsets = "تعديل بالدقائق",
    notifications = "تنبيهات الصلاة",
    notificationText = { p -> "حان وقت صلاة ${prayerNames("الفجر", "الشروق", "الظهر", "العصر", "المغرب", "العشاء")(p)}" },
    notificationsDenied = "دون إذن التنبيهات لن تصل التذكيرات.",
    locationDenied = "لم يُمنح إذن الموقع. اختر مدينة من القائمة.",
    dumkNote = "مواقيت كازاخستان تتبع الجدول الرسمي للإدارة الدينية (muftyat.kz) وقد طوبقت معه في ألماتي. قد يختلف الوقت دقيقة أو دقيقتين في مدن أخرى؛ عدّل الدقائق إن اختلف مسجدك.",
    adhan = "الأذان عند دخول الوقت",
    adhanName = adhanNames("الأذان كاملًا", "مختصر (20 ث)", "صوت الإشعار", "صامت"),
    listen = "استماع",
    stop = "إيقاف",
    fullScreen = "إظهار نافذة الصلاة على شاشة القفل",
    fullScreenDenied = "لم يسمح أندرويد للتطبيق بالظهور بملء الشاشة. سيُرفع الأذان مع إشعار عادي.",
    exactDenied = "لم يسمح أندرويد للتطبيق بالمنبهات الدقيقة: قد يتأخر الأذان بضع دقائق أو يصل كإشعار فقط.",
    openSettings = "السماح",
    fajrNote = "يُرفع الأذان نفسه للفجر: لا يوجد بعد تسجيل حر الترخيص لأذان الفجر (مع «الصلاة خير من النوم»).",
    adhanCredit = ADHAN_CREDIT,
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
    methodName = methodNames("Kirguistán (Muftiyat)", "Uzbekistán (Junta Musulmana)", "Kazajistán (DUMK)", "Liga Musulmana Mundial", "ISNA (Norteamérica)", "Autoridad egipcia", "Umm al-Qura (La Meca)", "Universidad de Karachi", "Diyanet (Turquía)", "Kemenag (Indonesia)", "Rusia (DUM)"),
    asr = "Asr",
    asrStandard = "Estándar (shafi‘í, malikí, hanbalí)",
    asrHanafi = "Hanafí",
    offsets = "Ajuste, minutos",
    notifications = "Avisos de oración",
    notificationText = { p -> "Es la hora de la oración: ${prayerNames("Fayr", "Salida del sol", "Duhr", "Asr", "Magrib", "Isha")(p)}" },
    notificationsDenied = "Sin permiso de notificaciones no habrá recordatorios.",
    locationDenied = "No se concedió acceso a la ubicación. Elige una ciudad de la lista.",
    dumkNote = "Los horarios de Kazajistán siguen el calendario oficial de la DUMK (muftyat.kz) y se han comprobado con él para Almaty. En otras ciudades puede haber uno o dos minutos de diferencia; ajusta los minutos si tu mezquita difiere.",
    adhan = "Adhan a la hora de la oración",
    adhanName = adhanNames("Adhan completo", "Corto (20 s)", "Sonido de notificación", "Silencio"),
    listen = "Escuchar",
    stop = "Detener",
    fullScreen = "Mostrar la ventana de oración en la pantalla de bloqueo",
    fullScreenDenied = "Android no ha permitido que la app se abra a pantalla completa. El adhan sonará igualmente, con una notificación normal.",
    exactDenied = "Android no permite alarmas exactas a esta app: el adhan puede sonar unos minutos tarde o llegar solo como notificación.",
    openSettings = "Permitir",
    fajrNote = "En el Fajr suena el mismo adhan: aún no hay una grabación con licencia libre del adhan del Fajr (con «as-salatu jairun min an-naum»).",
    adhanCredit = ADHAN_CREDIT,
)

val KyPrayer = PrayerStrings(
    title = "Намаз убактысы",
    next = { p, t -> "Кийинки: $p, $t" },
    prayerName = prayerNames("Багымдат", "Күн чыгуу", "Бешим", "Аср", "Шам", "Куптан"),
    place = "Шаар",
    chooseCity = "Шаарды тандоо",
    useLocation = "Менин жайгашкан жерим",
    noPlace = "Намаз убактысын көрүү үчүн шаарды тандаңыз. Убакыт телефондун өзүндө эсептелет жана интернетсиз иштейт.",
    method = "Эсептөө ыкмасы",
    methodName = methodNames("Кыргызстан (КМДБ)", "Өзбекстан (Мусулмандар башкармалыгы)", "Казакстан (КМДБ)", "Дүйнөлүк ислам лигасы", "ISNA (Түндүк Америка)", "Египет", "Умм аль-Кура (Мекке)", "Карачи университети", "Диянет (Түркия)", "Кеменаг (Индонезия)", "Россия (МДБ)"),
    asr = "Аср",
    asrStandard = "Стандарттуу (шафиий, маликий, ханбалий)",
    asrHanafi = "Ханафий",
    offsets = "Тууралоо, мүнөт",
    notifications = "Намаз тууралуу эскертмелер",
    notificationText = { p -> "Намаз убактысы кирди: ${prayerNames("Багымдат", "Күн чыгуу", "Бешим", "Аср", "Шам", "Куптан")(p)}" },
    notificationsDenied = "Эскертмелерге уруксат болбосо, эскертүү келбейт.",
    locationDenied = "Жайгашкан жерге уруксат берилген жок. Тизмеден шаарды тандаңыз.",
    dumkNote = "Казакстан үчүн убакыт КМДБнын (muftyat.kz) расмий жадыбалынын эрежеси менен эсептелет жана Алматы боюнча аны менен салыштырылган. Башка шаарларда бир-эки мүнөт айырма болушу мүмкүн; мечитиңиздин убактысы башкача болсо, мүнөттөрдү тууралаңыз.",
    adhan = "Намаз убактысы киргенде азан",
    adhanName = adhanNames("Толук азан", "Кыска (20 сек)", "Эскертме үнү", "Үнсүз"),
    listen = "Угуу",
    stop = "Токтотуу",
    fullScreen = "Намаз терезесин кулпуланган экранда көрсөтүү",
    fullScreenDenied = "Android тиркемеге толук экранда ачылууга уруксат берген жок. Азан баары бир жөнөкөй эскертме менен угулат.",
    exactDenied = "Android тиркемеге так ойготкучтарга уруксат берген жок: азан бир нече мүнөт кечигиши же эскертме катары гана келиши мүмкүн.",
    openSettings = "Уруксат берүү",
    fajrNote = "Багымдатта да ушул эле азан угулат: «ас-салату хайрун мина-н-наум» сөздөрү бар багымдат азанынын эркин лицензиядагы жазмасы азырынча жок.",
    adhanCredit = ADHAN_CREDIT,
)

val UzPrayer = PrayerStrings(
    title = "Namoz vaqtlari",
    next = { p, t -> "Keyingisi: $p, $t" },
    prayerName = prayerNames("Bomdod", "Quyosh chiqishi", "Peshin", "Asr", "Shom", "Xufton"),
    place = "Shahar",
    chooseCity = "Shaharni tanlash",
    useLocation = "Mening joylashuvim",
    noPlace = "Namoz vaqtlarini ko‘rish uchun shaharni tanlang. Vaqtlar telefonning o‘zida hisoblanadi va internetsiz ishlaydi.",
    method = "Hisoblash usuli",
    methodName = methodNames("Qirg‘iziston (Muftiyat)", "O‘zbekiston (Musulmonlari idorasi)", "Qozog‘iston (QMDB)", "Butunjahon islom ligasi", "ISNA (Shimoliy Amerika)", "Misr", "Ummul Quro (Makka)", "Karachi universiteti", "Diyonat (Turkiya)", "Kemenag (Indoneziya)", "Rossiya (MDB)"),
    asr = "Asr",
    asrStandard = "Standart (shofe’iy, molikiy, hanbaliy)",
    asrHanafi = "Hanafiy",
    offsets = "Tuzatish, daqiqa",
    notifications = "Namoz haqida bildirishnomalar",
    notificationText = { p -> "Namoz vaqti kirdi: ${prayerNames("Bomdod", "Quyosh chiqishi", "Peshin", "Asr", "Shom", "Xufton")(p)}" },
    notificationsDenied = "Bildirishnomalarga ruxsat bo‘lmasa, eslatmalar kelmaydi.",
    locationDenied = "Joylashuvga ruxsat berilmadi. Ro‘yxatdan shaharni tanlang.",
    dumkNote = "Qozog‘iston uchun vaqtlar QMDB (muftyat.kz) rasmiy jadvali qoidasi bilan hisoblanadi va Olmaota bo‘yicha u bilan solishtirilgan. Boshqa shaharlarda bir-ikki daqiqa farq bo‘lishi mumkin; masjidingiz vaqti boshqacha bo‘lsa, daqiqalarni tuzating.",
    adhan = "Namoz vaqti kirganda azon",
    adhanName = adhanNames("To‘liq azon", "Qisqa (20 s)", "Bildirishnoma ovozi", "Ovozsiz"),
    listen = "Tinglash",
    stop = "To‘xtatish",
    fullScreen = "Namoz oynasini qulflangan ekranda ko‘rsatish",
    fullScreenDenied = "Android ilovaga to‘liq ekranda ochilishga ruxsat bermadi. Azon baribir oddiy bildirishnoma bilan eshitiladi.",
    exactDenied = "Android ilovaga aniq budilniklarga ruxsat bermadi: azon bir necha daqiqa kechikishi yoki faqat bildirishnoma bo‘lib kelishi mumkin.",
    openSettings = "Ruxsat berish",
    fajrNote = "Bomdodda ham shu azon eshitiladi: «as-salotu xoyrun minan-navm» so‘zlari bor bomdod azonining erkin litsenziyali yozuvi hozircha yo‘q.",
    adhanCredit = ADHAN_CREDIT,
)

fun prayerStringsFor(language: String): PrayerStrings = when (language) {
    "ru" -> RuPrayer
    "kk" -> KkPrayer
    "ky" -> KyPrayer
    "uz" -> UzPrayer
    "tr" -> TrPrayer
    "id" -> IdPrayer
    "ur" -> UrPrayer
    "ar" -> ArPrayer
    "es" -> EsPrayer
    else -> EnPrayer
}
