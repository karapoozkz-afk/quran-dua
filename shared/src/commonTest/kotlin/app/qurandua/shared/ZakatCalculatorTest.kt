package app.qurandua.shared

import app.qurandua.shared.zakat.Madhhab
import app.qurandua.shared.zakat.NisabBasis
import app.qurandua.shared.zakat.ZakatCalculator
import app.qurandua.shared.zakat.ZakatInput
import app.qurandua.shared.zakat.rulesFor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ZakatCalculatorTest {
    private val prices = ZakatInput(goldPricePerGram = 100.0, silverPricePerGram = 1.0)

    @Test
    fun cashAboveSilverNisabIsZakatable() {
        val r = ZakatCalculator.calculate(prices.copy(cash = 1000.0), rulesFor(Madhhab.HANAFI, NisabBasis.SILVER))
        assertEquals(595.0, r.nisab)
        assertTrue(r.isDue)
        assertEquals(25.0, r.zakat, 1e-9)
    }

    @Test
    fun sameCashBelowGoldNisabIsNot() {
        val r = ZakatCalculator.calculate(prices.copy(cash = 1000.0), rulesFor(Madhhab.SHAFII, NisabBasis.GOLD))
        assertEquals(8500.0, r.nisab)
        assertFalse(r.isDue)
        assertEquals(0.0, r.zakat)
    }

    @Test
    fun wornJewelryCountsOnlyForHanafi() {
        val input = prices.copy(goldJewelryGrams = 100.0)
        assertTrue(ZakatCalculator.calculate(input, rulesFor(Madhhab.HANAFI, NisabBasis.GOLD)).isDue)
        assertFalse(ZakatCalculator.calculate(input, rulesFor(Madhhab.MALIKI, NisabBasis.GOLD)).isDue)
    }

    @Test
    fun debtsReduceZakatExceptShafii() {
        val input = prices.copy(cash = 2000.0, debtsDue = 1500.0)
        assertFalse(ZakatCalculator.calculate(input, rulesFor(Madhhab.HANBALI)).isDue)
        val shafii = ZakatCalculator.calculate(input, rulesFor(Madhhab.SHAFII))
        assertTrue(shafii.isDue)
        assertEquals(50.0, shafii.zakat, 1e-9)
    }

    @Test
    fun missingPriceMeansNisabUnknown() {
        val r = ZakatCalculator.calculate(ZakatInput(cash = 1_000_000.0), rulesFor(Madhhab.HANAFI))
        assertFalse(r.nisabKnown)
        assertFalse(r.isDue)
    }

    @Test
    fun negativeInputsAreIgnored() {
        val r = ZakatCalculator.calculate(prices.copy(cash = -500.0, debtsDue = -10.0), rulesFor(Madhhab.HANAFI))
        assertEquals(0.0, r.assets)
        assertEquals(0.0, r.deductions)
    }
}
