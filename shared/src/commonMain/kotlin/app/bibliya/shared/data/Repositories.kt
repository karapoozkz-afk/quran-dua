package app.bibliya.shared.data

import app.bibliya.shared.content.ContentParser
import app.bibliya.shared.model.AppSettings
import app.bibliya.shared.model.Book
import app.bibliya.shared.model.Bookmark
import app.bibliya.shared.model.DailyVerse
import app.bibliya.shared.model.Passage
import app.bibliya.shared.model.PlanSegment
import app.bibliya.shared.model.ReadingPosition
import app.bibliya.shared.model.Situation
import app.bibliya.shared.model.Verse
import app.bibliya.shared.search.ReferenceParser
import app.bibliya.shared.search.SmartSearch
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/** Interface languages. */
val UI_LANGUAGES = listOf("ru", "en")

/** Bible translations bundled with the app, by language: Russian Synodal and the King James Version. */
val TRANSLATIONS = listOf("ru", "en")

/** Maps a device language code to a supported UI language, or "" when unsupported. */
fun supportedUiLanguage(deviceLanguage: String): String {
    val code = deviceLanguage.lowercase().substringBefore('-').substringBefore('_')
    // Russian-speaking countries of the region often run phones in Kazakh, Ukrainian or Belarusian.
    return when (code) {
        in UI_LANGUAGES -> code
        "kk", "ky", "uk", "be", "uz", "tg", "tt", "az", "hy", "ka" -> "ru"
        else -> ""
    }
}

/** The translation shown when the person has not chosen one. */
fun translationFor(settings: AppSettings, uiLanguage: String): String =
    settings.translation.takeIf { it in TRANSLATIONS } ?: if (uiLanguage in TRANSLATIONS) uiLanguage else "ru"

interface BibleRepository {
    /** Imports a bundled translation into the database once; later calls return immediately. */
    suspend fun ensureImported(lang: String, readAsset: suspend (String) -> String)
    suspend fun chapter(lang: String, book: String, chapter: Int): List<Verse>
    suspend fun verse(lang: String, book: String, chapter: Int, verse: Int): Verse?
    /** Verses whose text contains every one of [terms] (stems), in Bible order. */
    suspend fun search(lang: String, terms: List<String>, limit: Int = 60): List<Verse>
}

interface UserDataRepository {
    val settings: Flow<AppSettings>
    suspend fun updateSettings(transform: (AppSettings) -> AppSettings)

    val favoritePassages: Flow<Set<String>>
    suspend fun toggleFavorite(passageId: String)

    val bookmarks: Flow<List<Bookmark>>
    suspend fun toggleBookmark(lang: String, book: String, chapter: Int, verse: Int)

    val lastRead: Flow<ReadingPosition?>
    suspend fun setLastRead(position: ReadingPosition)

    /** Days of the yearly plan marked as read (1-based). */
    val planDone: Flow<Set<Int>>
    suspend fun setPlanDay(day: Int, done: Boolean)
    /** The date the plan was started, or null when it has not been. */
    val planStart: Flow<LocalDate?>
    suspend fun setPlanStart(date: LocalDate?)
}

/** Books, situations, passages, verses of the day and the plan: small, read-only, kept in memory. */
class ContentRepository(
    val books: List<Book>,
    val situations: List<Situation>,
    val passages: List<Passage>,
    val dailyVerses: List<DailyVerse>,
    val plan: List<List<PlanSegment>>,
) {
    private val bookById = books.associateBy { it.id }
    private val situationById = situations.associateBy { it.id }
    private val passageById = passages.associateBy { it.id }

    val search = SmartSearch(situations, passages)
    val references = ReferenceParser(books)

    fun book(id: String): Book? = bookById[id]

    fun situation(id: String): Situation? = situationById[id]

    fun passage(id: String): Passage? = passageById[id]

    fun passagesFor(situationId: String): List<Passage> = passages.filter { situationId in it.sit }

    /** Books of a translation, in that translation's usual order. */
    fun booksFor(lang: String, showDeuterocanon: Boolean): List<Book> =
        books.filter { it.chapterCount(lang) > 0 && (showDeuterocanon || !it.dc) }
            .sortedBy { if (lang == "en") it.orderEn ?: it.order else it.order }

    /** The same verse every day for everyone, a different one each day of the year. */
    fun verseOfDay(date: LocalDate): DailyVerse? =
        dailyVerses.takeIf { it.isNotEmpty() }?.let { it[(date.toEpochDays() % it.size + it.size) % it.size] }

    companion object {
        fun fromJson(books: String, situations: String, passages: String, votd: String, plan: String) =
            ContentRepository(
                ContentParser.books(books),
                ContentParser.situations(situations),
                ContentParser.passages(passages),
                ContentParser.dailyVerses(votd),
                ContentParser.plan(plan),
            )
    }
}

/**
 * The plan and the English text number the Psalms as the Hebrew Bible does; the Synodal text
 * follows the Greek Psalter. Maps an English psalm number to its Synodal one.
 */
fun synodalPsalm(hebrew: Int): Int = when (hebrew) {
    in 1..8 -> hebrew
    9, 10 -> 9
    in 11..113 -> hebrew - 1
    114, 115 -> 113
    116 -> 114
    in 117..146 -> hebrew - 1
    147 -> 146
    else -> hebrew
}

/** A plan reading in the numbering of [lang]'s text. */
fun PlanSegment.inTranslation(lang: String): PlanSegment =
    if (lang == "ru" && book == "psa") {
        // Hebrew 147 is Synodal 146 and 147; read to the end of the pair.
        val end = if (to == 147) 147 else if (to == 116) 115 else synodalPsalm(to)
        PlanSegment(book, synodalPsalm(from), end)
    } else {
        this
    }
