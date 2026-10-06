package app.qurandua.shared.zakat

import kotlin.math.max

enum class Madhhab { HANAFI, MALIKI, SHAFII, HANBALI }

enum class NisabBasis { GOLD, SILVER }

/**
 * Where the schools differ in what this calculator asks about.
 * Defaults per school come from [rulesFor]; the user can override each one.
 */
data class ZakatRules(
    val nisabBasis: NisabBasis,
    /** Hanafi: jewellery is zakatable. Maliki, Shafi'i, Hanbali: permissible jewellery in personal use is not. */
    val includeWornJewelry: Boolean,
    /** Hanafi, Maliki, Hanbali: debts due reduce the zakatable amount. Shafi'i (the relied-upon view): they do not. */
    val deductDebts: Boolean,
)

fun rulesFor(madhhab: Madhhab, nisabBasis: NisabBasis = NisabBasis.SILVER) = when (madhhab) {
    Madhhab.HANAFI -> ZakatRules(nisabBasis, includeWornJewelry = true, deductDebts = true)
    Madhhab.MALIKI -> ZakatRules(nisabBasis, includeWornJewelry = false, deductDebts = true)
    Madhhab.SHAFII -> ZakatRules(nisabBasis, includeWornJewelry = false, deductDebts = false)
    Madhhab.HANBALI -> ZakatRules(nisabBasis, includeWornJewelry = false, deductDebts = true)
}

/** Everything in one currency, metals in grams. */
data class ZakatInput(
    val cash: Double = 0.0,
    val goldGrams: Double = 0.0,
    val goldJewelryGrams: Double = 0.0,
    val silverGrams: Double = 0.0,
    val silverJewelryGrams: Double = 0.0,
    /** Market value of goods held for sale. */
    val tradeGoods: Double = 0.0,
    /** Money owed to you that you expect to get back. */
    val receivables: Double = 0.0,
    /** Debts you must pay now. */
    val debtsDue: Double = 0.0,
    val goldPricePerGram: Double = 0.0,
    val silverPricePerGram: Double = 0.0,
)

data class ZakatResult(
    val assets: Double,
    val deductions: Double,
    val net: Double,
    val nisab: Double,
    val isDue: Boolean,
    val zakat: Double,
    /** False when the price needed for the chosen nisab is missing. */
    val nisabKnown: Boolean,
)

object ZakatCalculator {
    /** 20 mithqal of gold. Some contemporary scholars use 87.48 g. */
    const val GOLD_NISAB_GRAMS = 85.0
    /** 200 dirhams of silver. Some contemporary scholars use 612.36 g. */
    const val SILVER_NISAB_GRAMS = 595.0
    /** One fortieth. */
    const val RATE = 0.025

    fun calculate(input: ZakatInput, rules: ZakatRules): ZakatResult {
        val gold = input.goldGrams + if (rules.includeWornJewelry) input.goldJewelryGrams else 0.0
        val silver = input.silverGrams + if (rules.includeWornJewelry) input.silverJewelryGrams else 0.0
        val assets = input.cash.nonNeg() +
            gold.nonNeg() * input.goldPricePerGram.nonNeg() +
            silver.nonNeg() * input.silverPricePerGram.nonNeg() +
            input.tradeGoods.nonNeg() +
            input.receivables.nonNeg()
        val deductions = if (rules.deductDebts) input.debtsDue.nonNeg() else 0.0
        val net = max(0.0, assets - deductions)
        val price = when (rules.nisabBasis) {
            NisabBasis.GOLD -> input.goldPricePerGram
            NisabBasis.SILVER -> input.silverPricePerGram
        }
        val grams = when (rules.nisabBasis) {
            NisabBasis.GOLD -> GOLD_NISAB_GRAMS
            NisabBasis.SILVER -> SILVER_NISAB_GRAMS
        }
        val nisabKnown = price > 0
        val nisab = grams * price.nonNeg()
        val isDue = nisabKnown && net > 0 && net >= nisab
        return ZakatResult(
            assets = assets,
            deductions = deductions,
            net = net,
            nisab = nisab,
            isDue = isDue,
            zakat = if (isDue) net * RATE else 0.0,
            nisabKnown = nisabKnown,
        )
    }

    private fun Double.nonNeg() = if (isNaN() || this < 0) 0.0 else this
}
