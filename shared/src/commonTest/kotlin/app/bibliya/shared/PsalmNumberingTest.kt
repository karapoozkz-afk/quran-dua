package app.bibliya.shared

import app.bibliya.shared.data.synodalPsalm
import app.bibliya.shared.search.TextNormalizer
import kotlin.test.Test
import kotlin.test.assertEquals

class PsalmNumberingTest {
    @Test
    fun hebrewPsalmsMapToSynodal() {
        assertEquals(1, synodalPsalm(1))
        assertEquals(9, synodalPsalm(10))
        assertEquals(22, synodalPsalm(23))
        assertEquals(50, synodalPsalm(51))
        assertEquals(90, synodalPsalm(91))
        assertEquals(113, synodalPsalm(115))
        assertEquals(114, synodalPsalm(116))
        assertEquals(118, synodalPsalm(119))
        assertEquals(146, synodalPsalm(147))
        assertEquals(150, synodalPsalm(150))
    }

    @Test
    fun normalizerFoldsCaseAndYo() {
        assertEquals("еще раз", TextNormalizer.normalize("Ещё, раз!"))
        assertEquals("gods word", TextNormalizer.normalize("God’s Word"))
    }
}
