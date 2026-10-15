package app.bibliya.shared

import app.bibliya.shared.i18n.EnStrings
import app.bibliya.shared.i18n.RuStrings
import app.bibliya.shared.model.AppSettings
import app.bibliya.shared.model.Tradition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TraditionTest {

    @Test
    fun firstLaunchAsksTheQuestion() {
        assertEquals(Tradition.UNSET, AppSettings().tradition)
    }

    @Test
    fun onlyProtestantsHideTheDeuterocanonicalBooks() {
        assertFalse(Tradition.PROTESTANT.showsDeuterocanon)
        for (t in listOf(Tradition.ANY, Tradition.ORTHODOX, Tradition.CATHOLIC)) assertTrue(t.showsDeuterocanon, t.name)
    }

    @Test
    fun eachTraditionUsesItsOwnWordForTheBooks() {
        assertEquals("неканоническая", RuStrings.deuterocanonical(Tradition.ORTHODOX))
        assertEquals("второканоническая", RuStrings.deuterocanonical(Tradition.CATHOLIC))
        assertEquals("апокриф", RuStrings.deuterocanonical(Tradition.PROTESTANT))
        assertEquals("apocrypha", EnStrings.deuterocanonical(Tradition.PROTESTANT))
    }
}
