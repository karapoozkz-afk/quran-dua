package app.qurandua.shared

import app.qurandua.shared.search.SmartSearch
import app.qurandua.shared.search.TextNormalizer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TextNormalizerTest {

    @Test
    fun lowercasesAndFoldsYo() {
        assertEquals("ее елка", TextNormalizer.normalize("Её, Ёлка!"))
    }

    @Test
    fun stripsArabicDiacriticsAndUnifiesAlef() {
        assertEquals("بسم الله الرحمن الرحيم", TextNormalizer.normalize("بِسۡمِ ٱللَّهِ ٱلرَّحۡمَٰنِ ٱلرَّحِيمِ"))
    }

    @Test
    fun foldsTurkishAndTransliterationDiacritics() {
        assertEquals("borc sabir dua", TextNormalizer.normalize("Borç, sabır — duâ"))
        assertEquals("sabiin", TextNormalizer.normalize("ṣābiīn"))
    }

    @Test
    fun unifiesUrduAndArabicLetters() {
        // Urdu keyboard ی ک ہ against Arabic ي ك ه
        assertEquals(TextNormalizer.normalize("كتاب الله"), TextNormalizer.normalize("کتاب اللہ"))
        assertEquals("في", TextNormalizer.normalize("فی"))
    }

    @Test
    fun similarityMatchesWordForms() {
        assertTrue(SmartSearch.similarity("долгов", "долг") > 0)
        assertTrue(SmartSearch.similarity("умершего", "умерший") > 0)
        assertTrue(SmartSearch.similarity("debts", "debt") > 0)
        assertEquals(0.0, SmartSearch.similarity("муж", "музыка"))
        assertEquals(0.0, SmartSearch.similarity("дорога", "доброта"))
    }
}
