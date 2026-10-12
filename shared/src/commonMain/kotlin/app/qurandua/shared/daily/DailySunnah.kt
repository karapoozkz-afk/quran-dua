package app.qurandua.shared.daily

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * A hadith for the morning or evening reminder. Only Sahih al-Bukhari and Sahih Muslim are used, so every
 * one is sahih. [title] and [summary] are our own words (shown in the notification); [text] is the published
 * translation of the hadith, cut from the dataset by number and never typed by hand (tools/build_content.py).
 */
@Serializable
data class DailyHadith(
    /** "bukhari" or "muslim". */
    val collection: String,
    /** Hadith number in the dataset: the usual numbering for al-Bukhari. */
    val number: Int,
    /** Book and hadith inside the book: how Sahih Muslim is cited. */
    val book: Int = 0,
    val hadith: Int = 0,
    val title: Map<String, String>,
    val summary: Map<String, String>,
    val text: Map<String, String>,
)

@Serializable
data class DailyContent(
    val morning: List<DailyHadith> = emptyList(),
    val evening: List<DailyHadith> = emptyList(),
) {
    /** The same hadith all day, a new one each day; the list repeats each year. */
    fun forDay(date: LocalDate, slot: DailySlot): DailyHadith? {
        val list = if (slot == DailySlot.MORNING) morning else evening
        if (list.isEmpty()) return null
        return list[(date.dayOfYear - 1) % list.size]
    }
}

enum class DailySlot { MORNING, EVENING }

/** When the two reminders come; minutes after local midnight. */
@Serializable
data class DailySettings(
    val on: Boolean = true,
    val morningMinute: Int = 7 * 60,
    val eveningMinute: Int = 22 * 60,
    /** Notification permission is asked once, on first launch. */
    val askedPermission: Boolean = false,
)

object DailyParser {
    private val json = Json { ignoreUnknownKeys = true }
    fun parse(text: String): DailyContent = json.decodeFromString(text)
}

/**
 * Text in [lang], else the language its readers most likely also read: Russian across the former USSR,
 * English elsewhere.
 */
fun Map<String, String>.forLang(lang: String): String =
    this[lang] ?: (if (lang in RUSSIAN_READERS) this["ru"] else this["en"]) ?: this["ru"] ?: this["en"] ?: values.firstOrNull().orEmpty()

/** True when [map] has no text of its own in [lang] and a fallback language is shown instead. */
fun Map<String, String>.isFallbackFor(lang: String): Boolean = lang !in this

private val RUSSIAN_READERS = setOf("ru", "kk", "ky", "uz", "tg", "tt", "az")

fun formatMinute(minute: Int): String {
    val h = (minute / 60) % 24
    val m = minute % 60
    return h.toString().padStart(2, '0') + ":" + m.toString().padStart(2, '0')
}

/** Shown with the other sources; the texts come from the published translations in this repository. */
const val HADITH_CREDIT = "Hadith: Sahih al-Bukhari and Sahih Muslim, translations as published in " +
    "fawazahmed0/hadith-api (github.com/fawazahmed0/hadith-api)."
