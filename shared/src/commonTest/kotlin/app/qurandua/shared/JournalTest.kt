package app.qurandua.shared

import app.qurandua.shared.data.UI_LANGUAGES
import app.qurandua.shared.journal.JournalKind
import app.qurandua.shared.journal.WALL_DUA_COUNT
import app.qurandua.shared.journal.WallFirestore
import app.qurandua.shared.journal.isValidPin
import app.qurandua.shared.journal.wallStringsFor
import app.qurandua.shared.journal.journalStringsFor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class JournalTest {
    @Test
    fun everyUiLanguageHasItsOwnJournalTexts() {
        val english = journalStringsFor("en")
        for (lang in UI_LANGUAGES - "en") {
            val s = journalStringsFor(lang)
            assertNotEquals(english.title, s.title, "journal texts missing for $lang")
            assertTrue("%1" in s.tooManyTries, "seconds placeholder missing for $lang")
        }
    }

    @Test
    fun pinIsFourToEightDigits() {
        assertTrue(isValidPin("1234"))
        assertTrue(isValidPin("12345678"))
        assertFalse(isValidPin("123"))
        assertFalse(isValidPin("123456789"))
        assertFalse(isValidPin("12a4"))
        assertEquals(false, isValidPin(""))
    }

    @Test
    fun everyUiLanguageHasWallTexts() {
        val english = wallStringsFor("en")
        for (lang in UI_LANGUAGES) {
            val s = wallStringsFor(lang)
            assertEquals(WALL_DUA_COUNT, s.duas.size, "dua replies for $lang")
            if (lang != "en") assertNotEquals(english.notice, s.notice, "wall texts missing for $lang")
        }
    }

    @Test
    fun parsesApprovedPostsAndSkipsEmptyRows() {
        val body = """
            [{"document":{"name":"projects/p/databases/(default)/documents/posts/abc",
              "fields":{"text":{"stringValue":"I regret it"},"kind":{"stringValue":"GRUDGE"},
                "createdAt":{"timestampValue":"2026-10-15T10:00:00.123456Z"},
                "d0":{"integerValue":"3"},"d2":{"integerValue":"1"}}},"readTime":"x"},
             {"readTime":"x"}]
        """.trimIndent()
        val posts = WallFirestore.parsePosts(body)
        assertEquals(1, posts.size)
        val post = posts.single()
        assertEquals("abc", post.id)
        assertEquals(JournalKind.GRUDGE, post.kind)
        assertEquals(listOf(3, 0, 1), post.duas)
        assertEquals(1792058400123L, post.createdAt)
    }

    @Test
    fun newPostIsPendingWithZeroCountersAndServerTime() {
        val body = WallFirestore.newPost(WallFirestore.root("p"), "id1", "text", JournalKind.REGRET, "ru")
        assertTrue("\"pending\"" in body)
        assertTrue("REQUEST_TIME" in body)
        assertTrue("\"d2\":{\"integerValue\":\"0\"}" in body)
        assertFalse("author" in body)
    }
}
