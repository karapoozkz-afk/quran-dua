package app.bibliya.shared.i18n

import app.bibliya.shared.model.Tradition

/** Every piece of interface text. Content (situations, passages, Bible text) carries its own languages. */
data class Strings(
    val appName: String,
    val navHome: String,
    val navBible: String,
    val navSituations: String,
    val navSaved: String,
    val navMore: String,
    val back: String,

    // Home
    val homeQuestion: String,
    val searchHint: String,
    val searchExamples: List<String>,
    val verseOfDay: String,
    val continueReading: String,
    val allSituations: String,
    val planTitle: String,
    val planSubtitle: String,
    val planStart: String,
    val planToday: (day: Int) -> String,
    val planProgress: (done: Int, total: Int) -> String,
    val planDone: String,

    // Bible
    val oldTestament: String,
    val newTestament: String,
    /** The deuterocanonical books are named the way the reader's tradition names them. */
    val deuterocanonical: (Tradition) -> String,
    val deuterocanonicalNote: (Tradition) -> String,
    val chapter: String,
    val chapters: String,
    val loadingBible: String,
    val previousChapter: String,
    val nextChapter: String,
    val listen: String,
    val stopListening: String,
    val noVoice: String,
    val voiceSettings: String,
    val textSize: String,
    val translation: String,
    val notInTranslation: String,

    // Verse and passage actions
    val bookmark: String,
    val removeBookmark: String,
    val copy: String,
    val share: String,
    val favorite: String,
    val unfavorite: String,
    val openInBible: String,
    val copied: String,

    // Search
    val searchTitle: String,
    val goToReference: String,
    val foundSituations: String,
    val foundPassages: String,
    val foundVerses: String,
    val searchingBible: String,
    val nothingFound: String,

    // Saved
    val savedPassages: String,
    val savedVerses: String,
    val savedEmpty: String,

    // Plan
    val planDay: (day: Int) -> String,
    val planMarkRead: String,
    val planRestart: String,
    val planRestartConfirm: String,
    val planNote: String,
    val cancel: String,

    // More
    val settings: String,
    val language: String,
    val languageAuto: String,
    val theme: String,
    val themeSystem: String,
    val themeLight: String,
    val themeDark: String,
    val showDeuterocanon: (Tradition) -> String,
    val showDeuterocanonNote: String,
    val reminder: String,
    val reminderNote: String,
    val reminderTime: String,
    val notificationsDenied: String,
    val about: String,
    val aboutText: String,
    val sources: String,
    val sourcesText: String,
    val privacy: String,
    val privacyText: String,
    val feedback: String,
    val version: String,
    val tradition: String,
    val traditionQuestion: String,
    val traditionNote: String,
    val traditionName: (Tradition) -> String,
    val traditionHint: (Tradition) -> String,
    val notificationChannel: String,
    val notificationTitle: String,
)

fun stringsFor(language: String): Strings = when (language) {
    "en" -> EnStrings
    else -> RuStrings
}

val RuStrings = Strings(
    appName = "Библия",
    navHome = "Главная",
    navBible = "Библия",
    navSituations = "Что читать",
    navSaved = "Закладки",
    navMore = "Ещё",
    back = "Назад",

    homeQuestion = "Что почитать сегодня?",
    searchHint = "Опишите, что у вас на душе, или «Ин 3:16»",
    searchExamples = listOf("мне тревожно", "умер близкий человек", "не могу простить", "долги", "перед дорогой"),
    verseOfDay = "Стих дня",
    continueReading = "Продолжить чтение",
    allSituations = "Все темы",
    planTitle = "Библия за год",
    planSubtitle = "Около 15 минут в день",
    planStart = "Начать план",
    planToday = { day -> "День $day" },
    planProgress = { done, total -> "Прочитано $done из $total" },
    planDone = "Сегодняшнее чтение отмечено",

    oldTestament = "Ветхий Завет",
    newTestament = "Новый Завет",
    deuterocanonical = { t ->
        when (t) {
            Tradition.CATHOLIC -> "второканоническая"
            Tradition.PROTESTANT -> "апокриф"
            else -> "неканоническая"
        }
    },
    deuterocanonicalNote = { t ->
        when (t) {
            Tradition.CATHOLIC -> "Второканонические книги входят в Синодальное издание, там они названы неканоническими. Их можно скрыть в настройках."
            Tradition.PROTESTANT -> "Апокрифы входят в Синодальное издание, но не в протестантский канон. Их можно скрыть в настройках."
            else -> "Неканонические книги входят в Синодальное издание. Их можно скрыть в настройках."
        }
    },
    chapter = "Глава",
    chapters = "Главы",
    loadingBible = "Готовим текст Библии на телефоне. Это нужно сделать один раз.",
    previousChapter = "Предыдущая глава",
    nextChapter = "Следующая глава",
    listen = "Слушать главу",
    stopListening = "Остановить",
    noVoice = "На телефоне нет голоса для этого языка. Его можно установить в настройках синтеза речи.",
    voiceSettings = "Настройки голоса",
    textSize = "Размер текста",
    translation = "Перевод",
    notInTranslation = "Этой книги нет в выбранном переводе.",

    bookmark = "В закладки",
    removeBookmark = "Убрать закладку",
    copy = "Копировать",
    share = "Поделиться",
    favorite = "Сохранить",
    unfavorite = "Убрать из закладок",
    openInBible = "Открыть главу",
    copied = "Скопировано",

    searchTitle = "Поиск",
    goToReference = "Перейти",
    foundSituations = "Темы",
    foundPassages = "Подходящие отрывки",
    foundVerses = "В тексте Библии",
    searchingBible = "Ищем в тексте…",
    nothingFound = "Ничего не нашлось. Попробуйте сказать иначе: «тревога», «болезнь мамы», «Пс 22».",

    savedPassages = "Отрывки",
    savedVerses = "Закладки",
    savedEmpty = "Здесь появятся сохранённые отрывки и стихи с закладками.",

    planDay = { day -> "День $day" },
    planMarkRead = "Прочитано",
    planRestart = "Начать заново",
    planRestartConfirm = "Отметки о прочитанном будут удалены.",
    planNote = "Канонические книги от Бытия до Откровения за 365 дней. Пропустили день — просто продолжайте с того места, где остановились.",
    cancel = "Отмена",

    settings = "Настройки",
    language = "Язык интерфейса",
    languageAuto = "Как на телефоне",
    theme = "Тема",
    themeSystem = "Как на телефоне",
    themeLight = "Светлая",
    themeDark = "Тёмная",
    showDeuterocanon = { t ->
        when (t) {
            Tradition.CATHOLIC -> "Показывать второканонические книги"
            Tradition.PROTESTANT -> "Показывать апокрифы"
            else -> "Показывать неканонические книги"
        }
    },
    showDeuterocanonNote = "Товит, Иудифь, Премудрость, Сирах, Маккавейские и другие. Есть только в Синодальном переводе.",
    reminder = "Стих дня в уведомлении",
    reminderNote = "Одно уведомление в день, без рекламы.",
    reminderTime = "Время",
    notificationsDenied = "Уведомления выключены в настройках телефона.",
    about = "О приложении",
    aboutText = "Библия на русском и английском без интернета, рекламы и регистрации. " +
        "Раздел «Что читать» подбирает отрывки Писания к жизненным ситуациям: тревога, утрата, болезнь, долги, семья. " +
        "Отрывки подобраны составителями приложения и не заменяют совета священника или пастора, а в болезни — врача. " +
        "Приложение не принадлежит ни одной церкви или организации.",
    sources = "Тексты и лицензии",
    sourcesText = "Синодальный перевод (1876) и King James Version (1769) находятся в общественном достоянии. " +
        "Электронный текст: CrossWire Bible Society через проект scrollmapper/bible_databases. " +
        "Отрывки в разделе «Что читать» берутся из этого же текста автоматически, вручную текст Писания не набирался.",
    privacy = "Конфиденциальность",
    privacyText = "Приложение не собирает и не отправляет никаких данных. Закладки, настройки и отметки плана хранятся только на вашем телефоне. " +
        "Приложению не нужен интернет, геолокация или доступ к контактам.",
    feedback = "Написать разработчику",
    version = "Версия",
    tradition = "Традиция",
    traditionQuestion = "К какой традиции вы ближе?",
    traditionNote = "Текст Библии для всех один: Синодальный перевод. От выбора зависит только, показывать ли 11 книг Ветхого Завета, которых нет в протестантской Библии, и как их называть. Поменять можно в настройках.",
    traditionName = { t ->
        when (t) {
            Tradition.ORTHODOX -> "Православная"
            Tradition.CATHOLIC -> "Католическая"
            Tradition.PROTESTANT -> "Протестантская"
            else -> "Не важно"
        }
    },
    traditionHint = { t ->
        when (t) {
            Tradition.ORTHODOX -> "Неканонические книги показаны"
            Tradition.CATHOLIC -> "Второканонические книги показаны"
            Tradition.PROTESTANT -> "Евангельские христиане, баптисты, пятидесятники, лютеране. Апокрифы скрыты"
            else -> "Синодальное издание целиком"
        }
    },
    notificationChannel = "Стих дня",
    notificationTitle = "Стих дня",
)

val EnStrings = Strings(
    appName = "Bible",
    navHome = "Home",
    navBible = "Bible",
    navSituations = "Topics",
    navSaved = "Saved",
    navMore = "More",
    back = "Back",

    homeQuestion = "What should I read today?",
    searchHint = "Describe how you feel, or type “John 3:16”",
    searchExamples = listOf("I feel anxious", "my father died", "I can't forgive", "debts", "before a trip"),
    verseOfDay = "Verse of the day",
    continueReading = "Continue reading",
    allSituations = "All topics",
    planTitle = "Bible in a year",
    planSubtitle = "About 15 minutes a day",
    planStart = "Start the plan",
    planToday = { day -> "Day $day" },
    planProgress = { done, total -> "$done of $total read" },
    planDone = "Today's reading is marked",

    oldTestament = "Old Testament",
    newTestament = "New Testament",
    deuterocanonical = { t -> if (t == Tradition.PROTESTANT) "apocrypha" else "deuterocanonical" },
    deuterocanonicalNote = { t ->
        if (t == Tradition.PROTESTANT) "The Apocrypha are part of the Russian Synodal edition but not of the Protestant canon. You can hide them in settings."
        else "The deuterocanonical books are part of the Russian Synodal edition. You can hide them in settings."
    },
    chapter = "Chapter",
    chapters = "Chapters",
    loadingBible = "Preparing the Bible text on your phone. This happens only once.",
    previousChapter = "Previous chapter",
    nextChapter = "Next chapter",
    listen = "Listen to the chapter",
    stopListening = "Stop",
    noVoice = "Your phone has no voice for this language. You can install one in text-to-speech settings.",
    voiceSettings = "Voice settings",
    textSize = "Text size",
    translation = "Translation",
    notInTranslation = "This book is not in the chosen translation.",

    bookmark = "Bookmark",
    removeBookmark = "Remove bookmark",
    copy = "Copy",
    share = "Share",
    favorite = "Save",
    unfavorite = "Remove from saved",
    openInBible = "Open chapter",
    copied = "Copied",

    searchTitle = "Search",
    goToReference = "Go to",
    foundSituations = "Topics",
    foundPassages = "Matching passages",
    foundVerses = "In the Bible text",
    searchingBible = "Searching the text…",
    nothingFound = "Nothing found. Try other words: “anxiety”, “my mother is ill”, “Ps 23”.",

    savedPassages = "Passages",
    savedVerses = "Bookmarks",
    savedEmpty = "Saved passages and bookmarked verses will appear here.",

    planDay = { day -> "Day $day" },
    planMarkRead = "Read",
    planRestart = "Start over",
    planRestartConfirm = "Your progress marks will be removed.",
    planNote = "The canonical books from Genesis to Revelation in 365 days. Missed a day? Just carry on from where you stopped.",
    cancel = "Cancel",

    settings = "Settings",
    language = "App language",
    languageAuto = "Same as phone",
    theme = "Theme",
    themeSystem = "Same as phone",
    themeLight = "Light",
    themeDark = "Dark",
    showDeuterocanon = { t -> if (t == Tradition.PROTESTANT) "Show the Apocrypha" else "Show deuterocanonical books" },
    showDeuterocanonNote = "Tobit, Judith, Wisdom, Sirach, Maccabees and others. Only in the Russian Synodal text.",
    reminder = "Verse of the day notification",
    reminderNote = "One notification a day, no ads.",
    reminderTime = "Time",
    notificationsDenied = "Notifications are turned off in your phone's settings.",
    about = "About",
    aboutText = "The Bible in Russian and English, offline, with no ads and no sign-up. " +
        "“What to read” suggests Scripture passages for life situations: anxiety, loss, illness, debt, family. " +
        "The passages were chosen by the app's authors and do not replace the advice of a priest or pastor, or of a doctor in illness. " +
        "The app is not affiliated with any church or organization.",
    sources = "Texts and licenses",
    sourcesText = "The Russian Synodal translation (1876) and the King James Version (1769) are in the public domain. " +
        "Electronic text: CrossWire Bible Society via the scrollmapper/bible_databases project. " +
        "Passages in “What to read” are cut from the same text automatically; no Scripture was typed by hand.",
    privacy = "Privacy",
    privacyText = "The app collects and sends no data. Bookmarks, settings and plan progress stay on your phone only. " +
        "The app needs no internet, location or contacts.",
    feedback = "Contact the developer",
    version = "Version",
    tradition = "Tradition",
    traditionQuestion = "Which tradition are you closest to?",
    traditionNote = "The Bible text is the same for everyone. Your choice only decides whether the deuterocanonical books, which Protestant Bibles leave out, are listed and what they are called. You can change it in settings.",
    traditionName = { t ->
        when (t) {
            Tradition.ORTHODOX -> "Orthodox"
            Tradition.CATHOLIC -> "Catholic"
            Tradition.PROTESTANT -> "Protestant"
            else -> "Doesn't matter"
        }
    },
    traditionHint = { t ->
        when (t) {
            Tradition.ORTHODOX -> "Deuterocanonical books listed"
            Tradition.CATHOLIC -> "Deuterocanonical books listed"
            Tradition.PROTESTANT -> "Evangelical, Baptist, Pentecostal, Lutheran. Apocrypha hidden"
            else -> "Every book of the edition"
        }
    },
    notificationChannel = "Verse of the day",
    notificationTitle = "Verse of the day",
)
