package app.qurandua.shared.i18n

import app.qurandua.shared.model.Grade
import app.qurandua.shared.zakat.Madhhab

/**
 * UI strings live in shared code so Android and the future iOS app use one source.
 * Adding a language = adding one more instance and registering it in [stringsFor].
 */
data class Strings(
    val appName: String,
    val navHome: String,
    val navQuran: String,
    val navDuas: String,
    val navSaved: String,
    val navMore: String,
    val whatToRead: String,
    val searchHint: String,
    val searchExamples: List<String>,
    val situations: String,
    val continueReading: String,
    val surahAyah: (Int, Int) -> String,
    val allDuas: String,
    val noResults: String,
    val resultsSituations: String,
    val resultsDuas: String,
    val resultsAyahs: String,
    val source: String,
    val repeatTimes: (Int) -> String,
    val openInQuran: String,
    val share: String,
    val copy: String,
    val copied: String,
    val listen: String,
    val listenSurah: String,
    val ttsNote: String,
    val noArabicVoice: String,
    val voiceSettings: String,
    val stop: String,
    val addFavorite: String,
    val removeFavorite: String,
    val bookmark: String,
    val ayahsCount: (Int) -> String,
    val meccan: String,
    val medinan: String,
    val loadingQuran: String,
    val savedDuas: String,
    val savedAyahs: String,
    val savedEmpty: String,
    val settings: String,
    val language: String,
    val showTranslit: String,
    val showTranslation: String,
    val arabicSize: String,
    val theme: String,
    val themeSystem: String,
    val themeLight: String,
    val themeDark: String,
    val donate: String,
    val donateTitle: String,
    val donateBody: String,
    val donateCharity: String,
    val donateSoon: String,
    val about: String,
    val aboutBody: String,
    val sourcesTitle: String,
    val sourcesBody: String,
    val back: String,
    val gradeLabel: (Grade) -> String,
    val gradeExplain: (Grade) -> String,
    // Transliteration script
    val translitScript: String = "Transliteration letters",
    val translitScriptName: (String) -> String = { code ->
        when (code) {
            "ru" -> "Кириллица"
            "en" -> "Latin"
            "es" -> "Español"
            else -> "Auto"
        }
    },
    val translitQuranNote: String = "Quran verses are transliterated in Latin letters only (Tanzil.net). No alphabet renders Arabic sounds exactly, so learn the recitation with the audio.",
    // Zakat
    val zakat: String = "Zakat calculator",
    val zakatIntro: String = "Zakat is due once a lunar year has passed while your wealth stayed at or above the nisab. Enter amounts in one currency.",
    val zakatMadhhab: String = "School of law (madhhab)",
    val madhhabName: (Madhhab) -> String = { m ->
        when (m) {
            Madhhab.HANAFI -> "Hanafi"
            Madhhab.MALIKI -> "Maliki"
            Madhhab.SHAFII -> "Shafi‘i"
            Madhhab.HANBALI -> "Hanbali"
        }
    },
    val zakatNisabBasis: String = "Nisab by",
    val nisabGold: String = "Gold (85 g)",
    val nisabSilver: String = "Silver (595 g)",
    val zakatNisabNote: String = "The silver nisab is lower, so more people pay and the poor benefit more; the gold nisab is used by many contemporary scholars for cash. If your local religious board announces a nisab, follow it.",
    val zakatJewelryRule: String = "Count jewellery you wear",
    val zakatDebtRule: String = "Subtract debts due now",
    val zakatRulesHint: String = "Set to your school’s position; you can change each one.",
    val zakatCash: String = "Cash and bank balances",
    val zakatGold: String = "Gold, grams (bars, coins, savings)",
    val zakatGoldJewelry: String = "Gold jewellery worn, grams",
    val zakatSilver: String = "Silver, grams",
    val zakatSilverJewelry: String = "Silver jewellery worn, grams",
    val zakatTrade: String = "Goods held for sale (market value)",
    val zakatReceivables: String = "Money owed to you that you expect back",
    val zakatDebts: String = "Debts you must pay now",
    val zakatGoldPrice: String = "Price of 1 g of gold",
    val zakatSilverPrice: String = "Price of 1 g of silver",
    val zakatAssets: String = "Zakatable wealth",
    val zakatDeductions: String = "Deducted debts",
    val zakatNet: String = "Net",
    val zakatNisab: String = "Nisab",
    val zakatDue: String = "Zakat to pay (2.5%)",
    val zakatNotDue: String = "Below the nisab: no zakat is due on this wealth.",
    val zakatNeedPrice: String = "Enter the price per gram for the chosen nisab metal.",
    val zakatRecipientsTitle: String = "Who may receive zakat",
    val zakatRecipients: String = "The poor, the needy, those who administer it, those whose hearts are to be reconciled, freeing captives, those in debt, in the cause of Allah, and the stranded traveller (Quran 9:60).",
    val zakatSources: String = "Sources: Quran 9:60, 9:103; al-Bukhari 1447 (no zakat below five awaq of silver); Abu Dawud 1573 (nisab of gold, and a full year).",
    val zakatDisclaimer: String = "This is an estimate. Livestock, crops, shares and business specifics are not covered — ask a knowledgeable imam for your case.",
    // Charities
    val charitiesTitle: String = "Verified charities",
    val charitiesEmpty: String = "No verified charities have been added yet. Each charity appears here only with its registration number and the date it was checked.",
    val charityReg: String = "Registration",
    val charityVerified: (String, String) -> String = { date, who -> "Checked $date by $who" },
    val charityPurpose: (String) -> String = { p ->
        when (p) {
            "zakat" -> "Zakat"
            "sadaqah" -> "Sadaqah"
            "mosque" -> "Mosques"
            "orphans" -> "Orphans"
            "water" -> "Water wells"
            "food" -> "Food"
            "education" -> "Education"
            else -> "General"
        }
    },
    val charityDonate: String = "Donate on the charity’s website",
    val charityWebsite: String = "Website",
    val supportApp: String = "Supporting the app itself will be possible through Google Play once it is published.",
)

fun stringsFor(language: String): Strings = when (language) {
    "ru" -> RuStrings
    "kk" -> KkStrings
    "ar" -> ArStrings
    "tr" -> TrStrings
    "id" -> IdStrings
    "ur" -> UrStrings
    "es" -> EsStrings
    else -> EnStrings
}

private fun ruAyahs(n: Int): String {
    val mod10 = n % 10
    val mod100 = n % 100
    val word = when {
        mod10 == 1 && mod100 != 11 -> "аят"
        mod10 in 2..4 && mod100 !in 12..14 -> "аята"
        else -> "аятов"
    }
    return "$n $word"
}

internal val RuBase = Strings(
    appName = "Коран и Дуа",
    navHome = "Главная",
    navQuran = "Коран",
    navDuas = "Дуа",
    navSaved = "Избранное",
    navMore = "Ещё",
    whatToRead = "Что читать?",
    searchHint = "Опишите ситуацию: «у меня долги», «умер отец»…",
    searchExamples = listOf("что читать от долгов", "за умершего отца", "не могу уснуть", "перед дорогой", "боюсь будущего", "муж заболел"),
    situations = "Жизненные ситуации",
    continueReading = "Продолжить чтение",
    surahAyah = { s, a -> "Сура $s, аят $a" },
    allDuas = "Все дуа",
    noResults = "Ничего не нашлось. Попробуйте описать ситуацию другими словами или выберите её в списке.",
    resultsSituations = "Ситуации",
    resultsDuas = "Дуа",
    resultsAyahs = "Аяты Корана",
    source = "Источник",
    repeatTimes = { n -> "Повторить $n раз" },
    openInQuran = "Открыть в Коране",
    share = "Поделиться",
    copy = "Копировать",
    copied = "Скопировано",
    listen = "Слушать",
    listenSurah = "Слушать всю суру",
    ttsNote = "Голос телефона (синтез речи), не запись чтеца. Только для ориентира в произношении: сверяйтесь с арабским текстом.",
    noArabicVoice = "На телефоне нет арабского голоса. Его можно установить в настройках синтеза речи.",
    voiceSettings = "Настройки голоса",
    stop = "Стоп",
    addFavorite = "В избранное",
    removeFavorite = "Убрать из избранного",
    bookmark = "Закладка",
    ayahsCount = ::ruAyahs,
    meccan = "Мекканская",
    medinan = "Мединская",
    loadingQuran = "Подготавливаем текст Корана (только при первом запуске)…",
    savedDuas = "Сохранённые дуа",
    savedAyahs = "Закладки в Коране",
    savedEmpty = "Здесь появятся дуа и аяты, которые вы сохраните.",
    settings = "Настройки",
    language = "Язык",
    showTranslit = "Показывать транслитерацию",
    showTranslation = "Показывать перевод",
    arabicSize = "Размер арабского текста",
    theme = "Тема",
    themeSystem = "Как в системе",
    themeLight = "Светлая",
    themeDark = "Тёмная",
    donate = "Садака и поддержка",
    donateTitle = "Садака джария",
    donateBody = "Приложение бесплатное и без рекламы. Пожертвования на благотворительные проекты принимаются на сайтах проверенных фондов — кнопка ниже откроет страницу в браузере.",
    donateCharity = "Открыть страницу пожертвований",
    donateSoon = "Страница пожертвований скоро появится.",
    about = "О приложении",
    aboutBody = "У каждого материала указан источник и степень достоверности. Если учёные расходятся во мнениях, это отмечено. Приложение не выносит фетв: по личным вопросам обращайтесь к знающему имаму.",
    sourcesTitle = "Источники текста",
    sourcesBody = "Арабский текст Корана: The Noble Qur'an Encyclopedia (quranenc.com). Перевод смыслов: Эльмир Кулиев; английский: Saheeh International; транслитерация: Tanzil.net. Набор данных quran-json (CC BY-SA 4.0). Аудио: EveryAyah.com, чтец Мишари Рашид аль-Афаси.",
    back = "Назад",
    gradeLabel = { g ->
        when (g) {
            Grade.QURAN -> "Коран"
            Grade.SAHIH -> "Достоверный хадис"
            Grade.HASAN -> "Хороший хадис (хасан)"
            Grade.ATHAR -> "Слова сподвижников / учёных"
            Grade.DAIF -> "Слабый хадис"
            Grade.DISPUTED -> "Есть разногласия"
            Grade.NOSOURCE -> "Без источника"
        }
    },
    gradeExplain = { g ->
        when (g) {
            Grade.QURAN -> "Текст из Корана."
            Grade.SAHIH -> "Хадис признан достоверным (сахих)."
            Grade.HASAN -> "Хадис признан хорошим (хасан) — применяется как довод."
            Grade.ATHAR -> "Это не хадис Пророка ﷺ, а слова сподвижников или учёных."
            Grade.DAIF -> "Хадис слабый: его нельзя приписывать Сунне."
            Grade.DISPUTED -> "Учёные расходятся в оценке. Не выдавайте это за обязательную Сунну."
            Grade.NOSOURCE -> "Популярный совет, у которого нет источника в Коране и достоверной Сунне. Читать Коран и делать дуа можно всегда, но обещанный результат и число повторов не установлены."
        }
    },
)

val EnStrings = Strings(
    appName = "Quran & Dua",
    navHome = "Home",
    navQuran = "Quran",
    navDuas = "Duas",
    navSaved = "Saved",
    navMore = "More",
    whatToRead = "What should I read?",
    searchHint = "Describe your situation: “I have debts”, “my father died”…",
    searchExamples = listOf("dua for debt", "for my late father", "can't sleep", "before travel", "I feel anxious", "my husband is sick"),
    situations = "Life situations",
    continueReading = "Continue reading",
    surahAyah = { s, a -> "Surah $s, ayah $a" },
    allDuas = "All duas",
    noResults = "Nothing found. Try describing it in other words or pick a situation from the list.",
    resultsSituations = "Situations",
    resultsDuas = "Duas",
    resultsAyahs = "Quran verses",
    source = "Source",
    repeatTimes = { n -> "Repeat $n times" },
    openInQuran = "Open in Quran",
    share = "Share",
    copy = "Copy",
    copied = "Copied",
    listen = "Listen",
    listenSurah = "Listen to the whole surah",
    ttsNote = "Phone voice (text-to-speech), not a reciter's recording. A pronunciation guide only: check it against the Arabic text.",
    noArabicVoice = "This phone has no Arabic voice. You can install one in the text-to-speech settings.",
    voiceSettings = "Voice settings",
    stop = "Stop",
    addFavorite = "Save",
    removeFavorite = "Remove from saved",
    bookmark = "Bookmark",
    ayahsCount = { n -> if (n == 1) "1 ayah" else "$n ayahs" },
    meccan = "Meccan",
    medinan = "Medinan",
    loadingQuran = "Preparing the Quran text (first launch only)…",
    savedDuas = "Saved duas",
    savedAyahs = "Quran bookmarks",
    savedEmpty = "Duas and verses you save will appear here.",
    settings = "Settings",
    language = "Language",
    showTranslit = "Show transliteration",
    showTranslation = "Show translation",
    arabicSize = "Arabic text size",
    theme = "Theme",
    themeSystem = "System",
    themeLight = "Light",
    themeDark = "Dark",
    donate = "Sadaqah & support",
    donateTitle = "Sadaqah jariyah",
    donateBody = "This app is free and ad-free. Donations to charity projects are accepted on verified charities’ websites — the button below opens the page in your browser.",
    donateCharity = "Open donation page",
    donateSoon = "The donation page is coming soon.",
    about = "About",
    aboutBody = "Every item shows its source and grade of authenticity, and scholarly disagreement is marked. The app does not issue fatwas: for personal questions, ask a knowledgeable imam.",
    sourcesTitle = "Text sources",
    sourcesBody = "Arabic Quran text: The Noble Qur'an Encyclopedia (quranenc.com). Translations: Saheeh International (English), Elmir Kuliev (Russian); transliteration: Tanzil.net. Dataset: quran-json (CC BY-SA 4.0). Audio: EveryAyah.com, reciter Mishary Rashid Alafasy.",
    back = "Back",
    gradeLabel = { g ->
        when (g) {
            Grade.QURAN -> "Quran"
            Grade.SAHIH -> "Authentic hadith"
            Grade.HASAN -> "Good hadith (hasan)"
            Grade.ATHAR -> "Companions / scholars"
            Grade.DAIF -> "Weak hadith"
            Grade.DISPUTED -> "Scholars differ"
            Grade.NOSOURCE -> "No source"
        }
    },
    gradeExplain = { g ->
        when (g) {
            Grade.QURAN -> "Text from the Quran."
            Grade.SAHIH -> "The hadith is graded authentic (sahih)."
            Grade.HASAN -> "The hadith is graded good (hasan) and is used as evidence."
            Grade.ATHAR -> "Not a hadith of the Prophet ﷺ: words of Companions or scholars."
            Grade.DAIF -> "Weak hadith: it must not be attributed to the Sunnah."
            Grade.DISPUTED -> "Scholars differ on this. Do not present it as established Sunnah."
            Grade.NOSOURCE -> "Popular advice with no source in the Quran or authentic Sunnah. You may always recite the Quran and make dua, but the promised result and repetition count are not established."
        }
    },
)

internal val ArBase = EnStrings.copy(
    appName = "القرآن والدعاء",
    navHome = "الرئيسية",
    navQuran = "القرآن",
    navDuas = "الأدعية",
    navSaved = "المحفوظات",
    navMore = "المزيد",
    whatToRead = "ماذا أقرأ؟",
    searchHint = "صِف حالتك: «عليّ دين»، «توفي والدي»…",
    searchExamples = listOf("دعاء الدين", "زيارة القبور", "الأرق", "السفر", "القلق", "المرض"),
    situations = "مواقف الحياة",
    continueReading = "متابعة القراءة",
    surahAyah = { s, a -> "سورة $s، آية $a" },
    allDuas = "كل الأدعية",
    noResults = "لا توجد نتائج. جرّب كلمات أخرى أو اختر موقفًا من القائمة.",
    resultsSituations = "المواقف",
    resultsDuas = "الأدعية",
    resultsAyahs = "آيات القرآن",
    source = "المصدر",
    repeatTimes = { n -> "التكرار: $n مرات" },
    openInQuran = "فتح في المصحف",
    share = "مشاركة",
    copy = "نسخ",
    copied = "تم النسخ",
    listen = "استماع",
    listenSurah = "استماع إلى السورة كاملة",
    ttsNote = "صوت الهاتف (تحويل النص إلى كلام)، وليس تسجيلاً لقارئ. للاسترشاد بالنطق فقط: راجع النص العربي.",
    noArabicVoice = "لا يوجد صوت عربي على الهاتف. يمكن تثبيته من إعدادات تحويل النص إلى كلام.",
    voiceSettings = "إعدادات الصوت",
    stop = "إيقاف",
    addFavorite = "حفظ",
    removeFavorite = "إزالة من المحفوظات",
    bookmark = "علامة",
    ayahsCount = { n -> "$n آية" },
    meccan = "مكية",
    medinan = "مدنية",
    loadingQuran = "جارٍ تجهيز نص القرآن (عند التشغيل الأول فقط)…",
    savedDuas = "الأدعية المحفوظة",
    savedAyahs = "علامات المصحف",
    savedEmpty = "ستظهر هنا الأدعية والآيات التي تحفظها.",
    settings = "الإعدادات",
    language = "اللغة",
    showTranslit = "إظهار النطق اللاتيني",
    showTranslation = "إظهار الترجمة",
    arabicSize = "حجم الخط العربي",
    theme = "المظهر",
    themeSystem = "حسب النظام",
    themeLight = "فاتح",
    themeDark = "داكن",
    donate = "الصدقة والدعم",
    donateTitle = "صدقة جارية",
    donateBody = "التطبيق مجاني وبلا إعلانات. تُقبل التبرعات للمشاريع الخيرية عبر مواقع الجمعيات الموثوقة، والزر أدناه يفتح الصفحة في المتصفح.",
    donateCharity = "فتح صفحة التبرع",
    donateSoon = "صفحة التبرع قريبًا.",
    about = "عن التطبيق",
    aboutBody = "لكل مادة مصدرها ودرجتها، ويُشار إلى مواضع الخلاف بين العلماء. التطبيق لا يُفتي؛ اسأل أهل العلم في مسائلك الخاصة.",
    sourcesTitle = "مصادر النص",
    back = "رجوع",
    gradeLabel = { g ->
        when (g) {
            Grade.QURAN -> "قرآن"
            Grade.SAHIH -> "حديث صحيح"
            Grade.HASAN -> "حديث حسن"
            Grade.ATHAR -> "أثر عن الصحابة / العلماء"
            Grade.DAIF -> "حديث ضعيف"
            Grade.DISPUTED -> "مسألة خلافية"
            Grade.NOSOURCE -> "بلا مصدر"
        }
    },
    gradeExplain = { g ->
        when (g) {
            Grade.QURAN -> "نص من القرآن الكريم."
            Grade.SAHIH -> "الحديث صحيح."
            Grade.HASAN -> "الحديث حسن ويُحتج به."
            Grade.ATHAR -> "ليس حديثًا نبويًا، بل من كلام الصحابة أو العلماء."
            Grade.DAIF -> "حديث ضعيف لا يُنسب إلى السنة."
            Grade.DISPUTED -> "اختلف العلماء في هذه المسألة."
            Grade.NOSOURCE -> "نصيحة شائعة لا أصل لها في القرآن ولا في السنة الصحيحة. قراءة القرآن والدعاء مشروعان دائمًا، لكن النتيجة الموعودة وعدد التكرار غير ثابتين."
        }
    },
)
