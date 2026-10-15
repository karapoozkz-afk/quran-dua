package app.bibliya.shared.content

import app.bibliya.shared.model.Book
import app.bibliya.shared.model.DailyVerse
import app.bibliya.shared.model.Passage
import app.bibliya.shared.model.PlanSegment
import app.bibliya.shared.model.Situation
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

/** Parses the JSON assets produced by tools/build_content.py. */
object ContentParser {
    private val json = Json { ignoreUnknownKeys = true }

    fun books(text: String): List<Book> = json.decodeFromString(text)

    fun situations(text: String): List<Situation> = json.decodeFromString(text)

    fun passages(text: String): List<Passage> = json.decodeFromString(text)

    fun dailyVerses(text: String): List<DailyVerse> = json.decodeFromString(text)

    fun bible(text: String): BibleAsset = json.decodeFromString(text)

    /** plan.json: 365 days, each a list of [book, from, to]. */
    fun plan(text: String): List<List<PlanSegment>> =
        json.parseToJsonElement(text).jsonArray.map { day ->
            day.jsonArray.map { seg ->
                val parts = seg.jsonArray
                PlanSegment(parts[0].jsonPrimitive.content, parts[1].jsonPrimitive.int, parts[2].jsonPrimitive.int)
            }
        }
}

/** bible_<lang>.json: the whole translation, verses as plain strings per chapter. */
@Serializable
data class BibleAsset(
    val lang: String,
    val name: String,
    val credit: String,
    /** Book id to chapters to verses. An empty string is a verse printed joined to the previous one. */
    val books: Map<String, List<List<String>>>,
    /** Translator's footnotes, keyed "book chapter:verse". */
    val notes: Map<String, String> = emptyMap(),
)
