package app.qurandua.shared.content

import app.qurandua.shared.model.Charity
import app.qurandua.shared.model.DuaItem
import app.qurandua.shared.model.Situation
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Parses the JSON assets produced by tools/build_content.py. */
object ContentParser {
    private val json = Json { ignoreUnknownKeys = true }

    fun situations(text: String): List<Situation> = json.decodeFromString(text)

    fun duas(text: String): List<DuaItem> = json.decodeFromString(text)

    fun quran(text: String): QuranAsset = json.decodeFromString(text)

    fun charities(text: String): List<Charity> = json.decodeFromString(text)
}

/**
 * quran.json: compact layout to keep the asset small.
 * Each ayah is [arabic, transliteration, translation for langs[0], langs[1], ...].
 */
@Serializable
data class QuranAsset(
    val langs: List<String>,
    val credits: Map<String, String>,
    val meta: Map<String, String>,
    val surahs: List<SurahAsset>,
) {
    /** Column of each language's translation inside an ayah row. */
    val translationColumns: Map<String, Int>
        get() = langs.withIndex().associate { (i, lang) -> lang to i + 2 }
}

@Serializable
data class SurahAsset(
    val n: Int,
    val ar: String,
    val tr: String,
    val type: String,
    /** Translated surah names; languages without one show the transliteration. */
    val names: Map<String, String>,
    val ayahs: List<List<String>>,
) {
    companion object {
        const val ARABIC = 0
        const val TRANSLIT = 1
    }
}
