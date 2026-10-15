package app.qurandua.shared

import app.qurandua.shared.data.UI_LANGUAGES
import app.qurandua.shared.journal.isValidPin
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
}
