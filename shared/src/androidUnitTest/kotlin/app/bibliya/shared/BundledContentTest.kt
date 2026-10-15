package app.bibliya.shared

import app.bibliya.shared.content.ContentParser
import app.bibliya.shared.data.ContentRepository
import app.bibliya.shared.data.TRANSLATIONS
import app.bibliya.shared.data.inTranslation
import app.bibliya.shared.model.VerseRef
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Runs search and references over the real bundled content, as the app ships it. */
class BundledContentTest {

    private fun asset(name: String): String {
        val candidates = listOf("../androidApp/src/main/assets/content/$name", "androidApp/src/main/assets/content/$name")
        return candidates.map(::File).first { it.exists() }.readText()
    }

    private val repo = ContentRepository.fromJson(
        asset("books.json"), asset("situations.json"), asset("passages.json"), asset("votd.json"), asset("plan.json"),
    )
    private val bibles = TRANSLATIONS.associateWith { ContentParser.bible(asset("bible_$it.json")) }

    private fun topSituation(query: String): String? = repo.search.search(query).situations.firstOrNull()?.id

    @Test
    fun bothTranslationsAreComplete() {
        assertEquals(66, bibles.getValue("en").books.size)
        assertTrue(bibles.getValue("ru").books.size >= 77)
        assertEquals(
            "Ибо так возлюбил Бог мир, что отдал Сына Своего Единородного, дабы всякий верующий в Него, не погиб, но имел жизнь вечную.",
            bibles.getValue("ru").books.getValue("jhn")[2][15],
        )
        assertTrue(bibles.getValue("en").books.getValue("jhn")[2][15].startsWith("For God so loved the world"))
    }

    @Test
    fun everySituationHasPassages() {
        for (s in repo.situations) assertTrue(repo.passagesFor(s.id).isNotEmpty(), s.id)
    }

    @Test
    fun passageTextMatchesTheBibleText() {
        // The build script cuts passages out of the Bible; check that the app's copy agrees with it.
        for (p in repo.passages) for (lang in TRANSLATIONS) {
            val ref = assertNotNull(p.refFor(lang), p.id)
            val chapter = bibles.getValue(lang).books.getValue(ref.book)[ref.chapter - 1]
            val verses = p.versesFor(lang)
            assertEquals(ref.verseEnd - ref.verse + 1, verses.size, "${p.id} $lang")
            for ((n, text) in verses) assertEquals(chapter[n - 1], text, "${p.id} $lang $n")
        }
    }

    @Test
    fun everyDayHasAVerse() {
        for (v in repo.dailyVerses) for (lang in TRANSLATIONS) assertTrue(v.plainText(lang).isNotBlank(), v.ref.toString())
    }

    @Test
    fun planCoversTheWholeCanonOnce() {
        assertEquals(365, repo.plan.size)
        val en = bibles.getValue("en").books
        val read = repo.plan.flatten().flatMap { s -> (s.from..s.to).map { s.book to it } }
        assertEquals(en.values.sumOf { it.size }, read.size)
        assertEquals(read.size, read.toSet().size)
        // Psalm numbers map onto the Synodal text without running past its last psalm.
        for (s in repo.plan.flatten().filter { it.book == "psa" }) {
            val ru = s.inTranslation("ru")
            assertTrue(ru.from in 1..151 && ru.to in ru.from..151, s.toString())
        }
    }

    @Test
    fun searchUnderstandsEverydayPhrases() {
        assertEquals("anxiety", topSituation("мне очень тревожно"))
        assertEquals("grief", topSituation("умер отец"))
        assertEquals("money", topSituation("большие долги по кредиту"))
        assertEquals("forgive", topSituation("не могу простить обиду"))
        assertEquals("night", topSituation("не могу уснуть"))
        assertEquals("travel", topSituation("перед дорогой"))
        assertEquals("illness", topSituation("мама заболела"))
        assertEquals("grief", topSituation("my father died"))
        assertEquals("anxiety", topSituation("I feel anxious"))
        assertEquals("temptation", topSituation("addiction to alcohol"))
    }

    @Test
    fun referencesAreUnderstood() {
        val refs = repo.references
        assertEquals(VerseRef("jhn", 3, 16), refs.parse("Ин 3:16"))
        assertEquals(VerseRef("jhn", 3, 16), refs.parse("иоанна 3 16"))
        assertEquals(VerseRef("jhn", 3, 16), refs.parse("John 3:16"))
        assertEquals(VerseRef("1co", 13, 1), refs.parse("1 Кор 13"))
        assertEquals(VerseRef("1co", 13, 4, 7), refs.parse("1Кор 13:4-7"))
        assertEquals(VerseRef("psa", 22, 1), refs.parse("Пс 22"))
        assertEquals(VerseRef("mat", 5, 3), refs.parse("Мф 5:3"))
        assertEquals(VerseRef("1jn", 4, 8), refs.parse("1 Ин 4:8"))
        assertEquals(VerseRef("gen", 1, 1), refs.parse("Бытие 1"))
        assertEquals(VerseRef("rev", 21, 4), refs.parse("Откровение 21:4"))
        assertEquals(null, refs.parse("мне тревожно"))
        assertEquals(null, refs.parse("долги"))
    }

    @Test
    fun booksAreOrderedPerTranslation() {
        val ru = repo.booksFor("ru", showDeuterocanon = true)
        val en = repo.booksFor("en", showDeuterocanon = true)
        assertEquals("gen", ru.first().id)
        assertEquals("rev", ru.last().id)
        assertEquals(66, en.size)
        // English Bibles put Paul's letters before James; the Synodal edition after.
        assertTrue(en.indexOfFirst { it.id == "rom" } < en.indexOfFirst { it.id == "jas" })
        assertTrue(ru.indexOfFirst { it.id == "rom" } > ru.indexOfFirst { it.id == "jas" })
        assertEquals(66, repo.booksFor("ru", showDeuterocanon = false).size)
    }
}
