package app.qurandua.shared.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Text in several languages, keyed by ISO 639-1 code ("ru", "en", ...). */
typealias Localized = Map<String, String>

/** Picks [lang], then English, then any available language. */
fun Localized.pick(lang: String): String =
    this[lang]?.takeIf { it.isNotBlank() }
        ?: this["en"]?.takeIf { it.isNotBlank() }
        ?: values.firstOrNull { it.isNotBlank() }
        ?: ""

fun Map<String, List<String>>.allValues(): List<String> = values.flatten()

/** How reliable the evidence behind an item is. Shown to the user on every card. */
@Serializable
enum class Grade {
    @SerialName("quran") QURAN,
    @SerialName("sahih") SAHIH,
    @SerialName("hasan") HASAN,
    /** Words of Companions or early scholars, not a hadith of the Prophet ﷺ. */
    @SerialName("athar") ATHAR,
    @SerialName("daif") DAIF,
    /** Scholars disagree on the practice or on the hadith's authenticity. */
    @SerialName("disputed") DISPUTED,
    /** Popular advice with no source in the Quran or Sunnah (fixed repetition counts, "tested" formulas). */
    @SerialName("nosource") NOSOURCE,
}

@Serializable
enum class ItemKind {
    /** A supplication or dhikr with Arabic text to recite. */
    @SerialName("dua") DUA,
    /** Guidance with a source but nothing to recite (e.g. a ruling, a disputed practice). */
    @SerialName("info") INFO,
}

@Serializable
data class AyahKey(
    @SerialName("s") val surah: Int,
    @SerialName("a") val ayah: Int,
)

@Serializable
data class Situation(
    val id: String,
    val emoji: String,
    val title: Localized,
    val subtitle: Localized,
    val keywords: Map<String, List<String>> = emptyMap(),
    /** Disclaimer or scholarly note shown on top of the situation screen. */
    val notice: Localized? = null,
)

@Serializable
data class DuaItem(
    val id: String,
    val kind: ItemKind = ItemKind.DUA,
    val grade: Grade,
    val situations: List<String>,
    val title: Localized,
    val arabic: String? = null,
    val translit: Localized? = null,
    val translation: Localized,
    val source: Localized,
    val note: Localized? = null,
    val repeat: Int? = null,
    /** First ayah of a Quranic dua: used for audio and "open in Quran". */
    val quranRef: AyahKey? = null,
    /** Every ayah a Quranic dua spans (whole verses, for audio). */
    val quranAyahs: List<AyahKey> = emptyList(),
    /** A surah this info card points to (e.g. al-Mulk). */
    val quranLink: AyahKey? = null,
) {
    val isQuranic: Boolean get() = quranRef != null
}

data class SurahInfo(
    val number: Int,
    val nameArabic: String,
    val transliteration: String,
    val translatedName: String,
    val revelation: String,
    val ayahCount: Int,
)

data class Ayah(
    val surah: Int,
    val number: Int,
    val arabic: String,
    val transliteration: String,
    val translation: String?,
) {
    val key: AyahKey get() = AyahKey(surah, number)
}

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class AppSettings(
    /** UI language; "" means follow the device. */
    val language: String = "",
    val showTransliteration: Boolean = true,
    val showTranslation: Boolean = true,
    val arabicFontSize: Int = 26,
    val theme: ThemeMode = ThemeMode.SYSTEM,
    /** Which transliteration of duas to show: "auto" follows the content language, or "ru", "en", "es". */
    val translitScript: String = TRANSLIT_AUTO,
)

const val TRANSLIT_AUTO = "auto"

/** Transliteration scripts the duas carry: Cyrillic (ru), Latin (en) and Spanish spelling (es). */
val TRANSLIT_SCRIPTS = listOf(TRANSLIT_AUTO, "ru", "en", "es")

/** The translit map key to show for a chosen script and content language. */
fun translitLanguageFor(script: String, contentLanguage: String): String =
    if (script == TRANSLIT_AUTO || script !in TRANSLIT_SCRIPTS) contentLanguage else script
