package app.qurandua.shared.daily

/** Texts of the morning and evening reminders. Machine-written outside ru/en; awaiting native review. */
class DailyStrings(
    val section: String,
    val subtitle: String,
    val morning: String,
    val evening: String,
    val notifyMorning: String,
    val notifyEvening: String,
    val channel: String,
    val remind: String,
    val morningTime: String,
    val eveningTime: String,
    val bukhari: String,
    val muslim: String,
    /** "book 48, no. 50" with %1 and %2. */
    val bookHadith: String,
    /** Shown when the hadith has no published translation in the reader's language; %1 = the language shown. */
    val translationIn: String,
    val allowNotifications: String,
    val done: String,
    val cancel: String,
) {
    fun source(h: DailyHadith): String =
        if (h.collection == "muslim") "$muslim, " + bookHadith.replace("%1", h.book.toString()).replace("%2", h.hadith.toString())
        else "$bukhari, ${h.number}"
}

fun dailyStringsFor(lang: String): DailyStrings = when (lang) {
    "ru" -> DailyStrings(
        "Сунна дня", "Хадис утром и вечером, на каждый день года", "Утро", "Вечер",
        "Доброе утро! Сунна дня", "Спокойной ночи. Сунна перед сном", "Сунна утром и вечером",
        "Напоминать утром и вечером", "Утреннее напоминание", "Вечернее напоминание",
        "Сахих аль-Бухари", "Сахих Муслим", "кн. %1, № %2", "Перевод хадиса: %1",
        "Разрешите уведомления, чтобы напоминания приходили", "Готово", "Отмена",
    )
    "kk" -> DailyStrings(
        "Күннің сүннеті", "Таңертең және кешке бір хадис, жылдың әр күніне", "Таң", "Кеш",
        "Қайырлы таң! Күннің сүннеті", "Тыныш түн. Ұйқы алдындағы сүннет", "Таңғы және кешкі сүннет",
        "Таңертең және кешке еске салу", "Таңғы еске салу", "Кешкі еске салу",
        "Сахих әл-Бұхари", "Сахих Муслим", "%1-кітап, № %2", "Хадис аудармасы: %1",
        "Еске салулар келуі үшін хабарландыруларға рұқсат беріңіз", "Дайын", "Болдырмау",
    )
    "ky" -> DailyStrings(
        "Күндүн сүннөтү", "Эртең менен жана кечинде бир хадис, жылдын ар бир күнүнө", "Эртең", "Кеч",
        "Кутман таң! Күндүн сүннөтү", "Бейпил түн. Уйку алдындагы сүннөт", "Эртең мененки жана кечки сүннөт",
        "Эртең менен жана кечинде эскертүү", "Эртең мененки эскертүү", "Кечки эскертүү",
        "Сахих аль-Бухари", "Сахих Муслим", "%1-китеп, № %2", "Хадистин котормосу: %1",
        "Эскертүүлөр келиши үчүн билдирмелерге уруксат бериңиз", "Даяр", "Жокко чыгаруу",
    )
    "uz" -> DailyStrings(
        "Kun sunnati", "Ertalab va kechqurun bitta hadis, yilning har kuni uchun", "Tong", "Kech",
        "Xayrli tong! Kun sunnati", "Xayrli tun. Uyqu oldidan sunnat", "Ertalabki va kechki sunnat",
        "Ertalab va kechqurun eslatish", "Ertalabki eslatma", "Kechki eslatma",
        "Sahih al-Buxoriy", "Sahih Muslim", "%1-kitob, № %2", "Hadis tarjimasi: %1",
        "Eslatmalar kelishi uchun bildirishnomalarga ruxsat bering", "Tayyor", "Bekor qilish",
    )
    "tg" -> DailyStrings(
        "Суннати рӯз", "Субҳу шом як ҳадис, барои ҳар рӯзи сол", "Субҳ", "Шом",
        "Субҳ ба хайр! Суннати рӯз", "Шаб ба хайр. Суннат пеш аз хоб", "Суннати субҳу шом",
        "Субҳу шом ёдовар шавад", "Ёдоварии субҳ", "Ёдоварии шом",
        "Саҳеҳи Бухорӣ", "Саҳеҳи Муслим", "китоби %1, № %2", "Тарҷумаи ҳадис: %1",
        "Барои омадани ёдовариҳо огоҳиномаҳоро иҷозат диҳед", "Тайёр", "Бекор кардан",
    )
    "tt" -> DailyStrings(
        "Көннең сөннәте", "Иртән һәм кичен бер хәдис, елның һәр көненә", "Иртә", "Кич",
        "Хәерле иртә! Көннең сөннәте", "Тыныч төн. Йокы алдыннан сөннәт", "Иртәнге һәм кичке сөннәт",
        "Иртән һәм кичен искә төшерү", "Иртәнге искәртү", "Кичке искәртү",
        "Сахих әл-Бохари", "Сахих Мөслим", "%1 нче китап, № %2", "Хәдис тәрҗемәсе: %1",
        "Искәртүләр килсен өчен хәбәрнамәләргә рөхсәт бирегез", "Әзер", "Баш тарту",
    )
    "az" -> DailyStrings(
        "Günün sünnəsi", "Səhər və axşam bir hədis, ilin hər günü üçün", "Səhər", "Axşam",
        "Sabahınız xeyir! Günün sünnəsi", "Gecəniz xeyrə qalsın. Yuxudan əvvəl sünnə", "Səhər və axşam sünnəsi",
        "Səhər və axşam xatırlat", "Səhər xatırlatması", "Axşam xatırlatması",
        "Səhih əl-Buxari", "Səhih Müslim", "%1-ci kitab, № %2", "Hədisin tərcüməsi: %1",
        "Xatırlatmalar gəlsin deyə bildirişlərə icazə verin", "Hazır", "Ləğv et",
    )
    "tr" -> DailyStrings(
        "Günün sünneti", "Sabah ve akşam bir hadis, yılın her günü için", "Sabah", "Akşam",
        "Günaydın! Günün sünneti", "İyi geceler. Uyumadan önce bir sünnet", "Sabah ve akşam sünneti",
        "Sabah ve akşam hatırlat", "Sabah hatırlatması", "Akşam hatırlatması",
        "Sahih-i Buhari", "Sahih-i Müslim", "%1. kitap, no. %2", "Hadis çevirisi: %1",
        "Hatırlatmaların gelmesi için bildirimlere izin verin", "Tamam", "İptal",
    )
    "fr" -> DailyStrings(
        "Sunna du jour", "Un hadith le matin et le soir, pour chaque jour de l’année", "Matin", "Soir",
        "Bonjour ! La sunna du jour", "Bonne nuit. Une sunna avant de dormir", "Sunna du matin et du soir",
        "Rappel le matin et le soir", "Rappel du matin", "Rappel du soir",
        "Sahih al-Boukhari", "Sahih Mouslim", "livre %1, n° %2", "Traduction du hadith : %1",
        "Autorisez les notifications pour recevoir les rappels", "OK", "Annuler",
    )
    "de" -> DailyStrings(
        "Sunna des Tages", "Morgens und abends ein Hadith, für jeden Tag des Jahres", "Morgen", "Abend",
        "Guten Morgen! Die Sunna des Tages", "Gute Nacht. Eine Sunna vor dem Schlafen", "Sunna am Morgen und Abend",
        "Morgens und abends erinnern", "Erinnerung am Morgen", "Erinnerung am Abend",
        "Sahih al-Buchari", "Sahih Muslim", "Buch %1, Nr. %2", "Übersetzung des Hadith: %1",
        "Erlauben Sie Benachrichtigungen, damit die Erinnerungen kommen", "Fertig", "Abbrechen",
    )
    "es" -> DailyStrings(
        "Sunna del día", "Un hadiz por la mañana y por la noche, para cada día del año", "Mañana", "Noche",
        "¡Buenos días! La sunna del día", "Buenas noches. Una sunna antes de dormir", "Sunna de mañana y noche",
        "Recordar por la mañana y por la noche", "Recordatorio de la mañana", "Recordatorio de la noche",
        "Sahih al-Bujari", "Sahih Muslim", "libro %1, n.º %2", "Traducción del hadiz: %1",
        "Permite las notificaciones para recibir los recordatorios", "Listo", "Cancelar",
    )
    "ar" -> DailyStrings(
        "سنة اليوم", "حديث في الصباح والمساء لكل يوم من أيام السنة", "الصباح", "المساء",
        "صباح الخير! سنة اليوم", "تصبح على خير. سنة قبل النوم", "سنة الصباح والمساء",
        "التذكير صباحاً ومساءً", "تذكير الصباح", "تذكير المساء",
        "صحيح البخاري", "صحيح مسلم", "كتاب %1، رقم %2", "ترجمة الحديث: %1",
        "اسمح بالإشعارات لتصلك التذكيرات", "تم", "إلغاء",
    )
    "id" -> DailyStrings(
        "Sunnah hari ini", "Satu hadis pagi dan malam, untuk setiap hari dalam setahun", "Pagi", "Malam",
        "Selamat pagi! Sunnah hari ini", "Selamat malam. Sunnah sebelum tidur", "Sunnah pagi dan malam",
        "Ingatkan pagi dan malam", "Pengingat pagi", "Pengingat malam",
        "Shahih al-Bukhari", "Shahih Muslim", "kitab %1, no. %2", "Terjemahan hadis: %1",
        "Izinkan notifikasi agar pengingat bisa masuk", "Selesai", "Batal",
    )
    "ur" -> DailyStrings(
        "آج کی سنت", "صبح اور شام ایک حدیث، سال کے ہر دن کے لیے", "صبح", "شام",
        "صبح بخیر! آج کی سنت", "شب بخیر۔ سونے سے پہلے کی سنت", "صبح و شام کی سنت",
        "صبح و شام یاد دلائیں", "صبح کی یاد دہانی", "شام کی یاد دہانی",
        "صحیح بخاری", "صحیح مسلم", "کتاب %1، نمبر %2", "حدیث کا ترجمہ: %1",
        "یاد دہانیاں آنے کے لیے اطلاعات کی اجازت دیں", "ٹھیک ہے", "منسوخ",
    )
    else -> DailyStrings(
        "Sunnah of the day", "A hadith every morning and evening, for each day of the year", "Morning", "Evening",
        "Good morning! The sunnah of the day", "Good night. A sunnah before sleep", "Morning and evening sunnah",
        "Remind me morning and evening", "Morning reminder", "Evening reminder",
        "Sahih al-Bukhari", "Sahih Muslim", "book %1, no. %2", "Hadith translation: %1",
        "Allow notifications so the reminders can arrive", "Done", "Cancel",
    )
}

/** Name of the language a fallback hadith text is shown in, in the reader's language. */
fun fallbackLanguageName(lang: String): String = when (lang) {
    "kk" -> "орыс тілінде"; "ky" -> "орус тилинде"; "uz" -> "rus tilida"; "tg" -> "бо забони русӣ"
    "tt" -> "рус телендә"; "az" -> "rus dilində"; "de" -> "Englisch"; "es" -> "inglés"; "ar" -> "الإنجليزية"
    else -> "English"
}
