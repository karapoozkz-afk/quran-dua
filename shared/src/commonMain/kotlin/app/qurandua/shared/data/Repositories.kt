package app.qurandua.shared.data

import app.qurandua.shared.content.ContentParser
import app.qurandua.shared.model.AppSettings
import app.qurandua.shared.model.Ayah
import app.qurandua.shared.model.AyahKey
import app.qurandua.shared.model.Charity
import app.qurandua.shared.model.DuaItem
import app.qurandua.shared.model.Situation
import app.qurandua.shared.model.SurahInfo
import app.qurandua.shared.search.SmartSearch
import kotlinx.coroutines.flow.Flow

/** Languages that have bundled translations of the Quran and the duas. */
val CONTENT_LANGUAGES = listOf("ru", "kk", "ky", "uz", "en", "es", "tr", "id", "ur")

/** Quran translations inside quran.json, in its column order (see tools/build_content.py). */
val QURAN_TRANSLATION_LANGUAGES = listOf("ru", "en", "es", "id", "kk", "ky", "tr", "ur", "uz")

/** UI languages. Arabic and Urdu switch the layout to right-to-left. */
val UI_LANGUAGES = listOf("ru", "kk", "ky", "uz", "en", "es", "ar", "tr", "id", "ur")

/** Right-to-left UI languages. */
val RTL_LANGUAGES = setOf("ar", "ur", "fa", "he", "ps")

/**
 * Maps a device language code to a supported UI language, or "" when unsupported.
 * Java and older Android report Indonesian as "in" and Hebrew as "iw".
 */
fun supportedUiLanguage(deviceLanguage: String): String {
    val code = when (val lower = deviceLanguage.lowercase().substringBefore('-').substringBefore('_')) {
        "in" -> "id"
        "iw" -> "he"
        else -> lower
    }
    return if (code in UI_LANGUAGES) code else ""
}

/** The language translations are shown in for a given UI language. */
fun contentLanguageFor(uiLanguage: String): String =
    if (uiLanguage in CONTENT_LANGUAGES) uiLanguage else "en"

interface QuranRepository {
    /** Imports the bundled Quran into the database once; later calls return immediately. */
    suspend fun ensureImported(readAsset: suspend () -> String)
    fun surahs(lang: String): Flow<List<SurahInfo>>
    suspend fun surah(number: Int, lang: String): SurahInfo?
    suspend fun ayahs(surah: Int, lang: String): List<Ayah>
    /** Finds ayahs whose translation or Arabic text contains any of [terms]. */
    suspend fun search(terms: List<String>, lang: String, limit: Int = 50): List<Ayah>
}

interface UserDataRepository {
    val settings: Flow<AppSettings>
    suspend fun updateSettings(transform: (AppSettings) -> AppSettings)

    val favoriteIds: Flow<Set<String>>
    suspend fun toggleFavorite(itemId: String)

    val bookmarks: Flow<List<AyahKey>>
    suspend fun toggleBookmark(key: AyahKey)

    val lastRead: Flow<AyahKey?>
    suspend fun setLastRead(key: AyahKey)
}

/** Situations and duas: small, read-only, kept in memory. */
class ContentRepository(
    val situations: List<Situation>,
    val duas: List<DuaItem>,
    val charities: List<Charity> = emptyList(),
) {
    private val byId = duas.associateBy { it.id }
    private val situationById = situations.associateBy { it.id }

    val search = SmartSearch(situations, duas)

    fun situation(id: String): Situation? = situationById[id]

    fun dua(id: String): DuaItem? = byId[id]

    /** Recitable duas first, then guidance cards; disputed items last. */
    fun duasFor(situationId: String): List<DuaItem> =
        duas.filter { situationId in it.situations }
            .sortedWith(compareBy({ it.grade.ordinal >= 4 }, { it.kind.ordinal }))

    companion object {
        fun fromJson(situationsJson: String, duasJson: String, charitiesJson: String = "[]") = ContentRepository(
            ContentParser.situations(situationsJson),
            ContentParser.duas(duasJson),
            ContentParser.charities(charitiesJson),
        )
    }
}
