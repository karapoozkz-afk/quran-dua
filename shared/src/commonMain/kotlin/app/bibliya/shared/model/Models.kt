package app.bibliya.shared.model

import kotlinx.serialization.Serializable

/** Text in several languages, keyed by ISO 639-1 code ("ru", "en"). */
typealias Localized = Map<String, String>

/** Picks [lang], then Russian, then any available language. */
fun Localized.pick(lang: String): String =
    this[lang]?.takeIf { it.isNotBlank() }
        ?: this["ru"]?.takeIf { it.isNotBlank() }
        ?: values.firstOrNull { it.isNotBlank() }
        ?: ""

/** A book of the Bible (assets/content/books.json). */
@Serializable
data class Book(
    val id: String,
    /** "ot" or "nt". */
    val t: String,
    /** Deuterocanonical: printed in the Synodal Bible as "неканонические", absent from the KJV. */
    val dc: Boolean,
    /** Position in the Synodal edition. */
    val order: Int,
    /** Position in English Bibles; null for books English Bibles do not have. */
    val orderEn: Int? = null,
    val names: Map<String, String>,
    val short: Map<String, String>,
    /** Extra spellings for reference search ("иоанна", "jn"). */
    val aliases: List<String> = emptyList(),
    /** Chapter count per translation language; 0 when the translation lacks the book. */
    val chapters: Map<String, Int>,
) {
    val isNewTestament: Boolean get() = t == "nt"
    fun name(lang: String): String = names.pick(lang)
    fun shortName(lang: String): String = short.pick(lang)
    fun chapterCount(lang: String): Int = chapters[lang] ?: 0
}

/** One verse, or a range of verses in one chapter: "jhn 3:16", "psa 22:1-6". */
@Serializable
data class VerseRef(
    val book: String,
    val chapter: Int,
    val verse: Int = 1,
    val verseEnd: Int = verse,
) {
    override fun toString(): String =
        if (verseEnd != verse) "$book $chapter:$verse-$verseEnd" else "$book $chapter:$verse"

    companion object {
        private val PATTERN = Regex("""^([0-9a-z]{3}) (\d+):(\d+)(?:-(\d+))?$""")

        fun parse(text: String): VerseRef? {
            val m = PATTERN.matchEntire(text.trim()) ?: return null
            val v = m.groupValues[3].toInt()
            return VerseRef(m.groupValues[1], m.groupValues[2].toInt(), v, m.groupValues[4].toIntOrNull() ?: v)
        }
    }
}

data class Verse(
    val lang: String,
    val book: String,
    val chapter: Int,
    val number: Int,
    val text: String,
)

/** A bookmarked verse, kept with the translation it was marked in. */
data class Bookmark(val lang: String, val book: String, val chapter: Int, val verse: Int, val text: String?)

/** Where the reader stopped: opened again from "Continue reading". */
data class ReadingPosition(val lang: String, val book: String, val chapter: Int, val verse: Int)

@Serializable
data class Situation(
    val id: String,
    val emoji: String,
    val title: Localized,
    val subtitle: Localized,
    val keywords: Map<String, List<String>> = emptyMap(),
    /** Shown on top of the situation screen (e.g. "prayer does not replace a doctor"). */
    val notice: Localized? = null,
)

/**
 * A Bible passage for a situation. Its text is cut out of the translation by the content
 * build script (tools/build_content.py), never typed in by hand.
 */
@Serializable
data class Passage(
    val id: String,
    val sit: List<String>,
    val title: Localized,
    /** Reference in each translation's own numbering (Psalms differ between Synodal and KJV). */
    val ref: Map<String, String>,
    /** Verses as [number, text] pairs, per translation language. */
    val text: Map<String, List<List<String>>>,
    val note: Localized? = null,
) {
    fun refFor(lang: String): VerseRef? = VerseRef.parse(ref[lang] ?: ref["ru"] ?: "")
    fun versesFor(lang: String): List<Pair<Int, String>> =
        (text[lang] ?: text["ru"].orEmpty()).map { it[0].toInt() to it[1] }
}

/** Verse of the day: same layout as a passage, without a title. */
@Serializable
data class DailyVerse(
    val ref: Map<String, String>,
    val text: Map<String, List<List<String>>>,
) {
    fun refFor(lang: String): VerseRef? = VerseRef.parse(ref[lang] ?: ref["ru"] ?: "")
    fun plainText(lang: String): String = (text[lang] ?: text["ru"].orEmpty()).joinToString(" ") { it[1] }
}

/** One reading of the yearly plan: chapters [from]..[to] of [book] (English chapter numbers). */
data class PlanSegment(val book: String, val from: Int, val to: Int)

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * The reader's church tradition. The text is the Synodal translation for everyone; the tradition only decides
 * whether the 11 deuterocanonical books are listed and what they are called:
 * Orthodox "неканонические", Catholic "второканонические", Protestant "апокрифы" (hidden by default).
 */
enum class Tradition {
    /** Not asked yet: the first launch shows the question. */
    UNSET,
    /** "Не важно": the Synodal edition as printed. */
    ANY,
    ORTHODOX,
    CATHOLIC,
    PROTESTANT;

    /** Whether the deuterocanonical books are listed right after choosing this tradition. */
    val showsDeuterocanon: Boolean get() = this != PROTESTANT
}

data class AppSettings(
    /** UI language; "" means follow the device. */
    val language: String = "",
    /** Translation shown in the reader; "" means the one matching the UI language. */
    val translation: String = "",
    val fontSize: Int = 18,
    val theme: ThemeMode = ThemeMode.SYSTEM,
    /** Whether the deuterocanonical books are listed (they are part of the Synodal edition). */
    val showDeuterocanon: Boolean = true,
    /** Daily verse notification. */
    val dailyReminder: Boolean = true,
    /** Minute of the day the reminder comes at (default 08:00). */
    val reminderMinute: Int = 8 * 60,
    /** Whether the notification permission was already asked for once. */
    val askedNotifications: Boolean = false,
    val tradition: Tradition = Tradition.UNSET,
)
