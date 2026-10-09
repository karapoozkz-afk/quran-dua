package app.qurandua.android.ui.zakat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.qurandua.android.ui.LocalStrings
import app.qurandua.android.ui.components.BackTopBar
import app.qurandua.android.ui.components.MoreInfo
import app.qurandua.android.ui.components.NoticeCard
import app.qurandua.shared.zakat.Madhhab
import app.qurandua.shared.zakat.NisabBasis
import app.qurandua.shared.zakat.ZakatCalculator
import app.qurandua.shared.zakat.ZakatInput
import app.qurandua.shared.zakat.rulesFor
import kotlin.math.roundToLong

private enum class Field {
    CASH, GOLD, GOLD_JEWELRY, SILVER, SILVER_JEWELRY, TRADE, RECEIVABLES, DEBTS, GOLD_PRICE, SILVER_PRICE
}

/** Zakat on money, gold, silver and trade goods, with the schools' differences made explicit. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ZakatScreen(onBack: () -> Unit) {
    val strings = LocalStrings.current
    var madhhab by rememberSaveable { mutableStateOf(Madhhab.HANAFI) }
    var basis by rememberSaveable { mutableStateOf(NisabBasis.SILVER) }
    var includeJewelry by rememberSaveable { mutableStateOf(rulesFor(Madhhab.HANAFI).includeWornJewelry) }
    var deductDebts by rememberSaveable { mutableStateOf(rulesFor(Madhhab.HANAFI).deductDebts) }
    val values = remember { mutableStateMapOf<Field, String>() }

    fun num(field: Field) = values[field]?.replace(',', '.')?.replace(" ", "")?.toDoubleOrNull() ?: 0.0

    val result = ZakatCalculator.calculate(
        ZakatInput(
            cash = num(Field.CASH),
            goldGrams = num(Field.GOLD),
            goldJewelryGrams = num(Field.GOLD_JEWELRY),
            silverGrams = num(Field.SILVER),
            silverJewelryGrams = num(Field.SILVER_JEWELRY),
            tradeGoods = num(Field.TRADE),
            receivables = num(Field.RECEIVABLES),
            debtsDue = num(Field.DEBTS),
            goldPricePerGram = num(Field.GOLD_PRICE),
            silverPricePerGram = num(Field.SILVER_PRICE),
        ),
        rulesFor(madhhab, basis).copy(includeWornJewelry = includeJewelry, deductDebts = deductDebts),
    )

    val labels = mapOf(
        Field.CASH to strings.zakatCash,
        Field.GOLD to strings.zakatGold,
        Field.GOLD_JEWELRY to strings.zakatGoldJewelry,
        Field.SILVER to strings.zakatSilver,
        Field.SILVER_JEWELRY to strings.zakatSilverJewelry,
        Field.TRADE to strings.zakatTrade,
        Field.RECEIVABLES to strings.zakatReceivables,
        Field.DEBTS to strings.zakatDebts,
        Field.GOLD_PRICE to strings.zakatGoldPrice,
        Field.SILVER_PRICE to strings.zakatSilverPrice,
    )

    Scaffold(topBar = { BackTopBar(strings.zakat, onBack) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(strings.zakatIntro, style = MaterialTheme.typography.bodyMedium)

            Text(strings.zakatMadhhab, style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Madhhab.entries.forEach { m ->
                    FilterChip(
                        selected = madhhab == m,
                        onClick = {
                            madhhab = m
                            val rules = rulesFor(m)
                            includeJewelry = rules.includeWornJewelry
                            deductDebts = rules.deductDebts
                        },
                        label = { Text(strings.madhhabName(m)) },
                    )
                }
            }
            Text(
                strings.zakatRulesHint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SwitchRow(strings.zakatJewelryRule, includeJewelry) { includeJewelry = it }
            SwitchRow(strings.zakatDebtRule, deductDebts) { deductDebts = it }

            Text(strings.zakatNisabBasis, style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = basis == NisabBasis.SILVER, onClick = { basis = NisabBasis.SILVER }, label = { Text(strings.nisabSilver) })
                FilterChip(selected = basis == NisabBasis.GOLD, onClick = { basis = NisabBasis.GOLD }, label = { Text(strings.nisabGold) })
            }
            Text(
                strings.zakatNisabNote,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Field.entries.forEach { field ->
                OutlinedTextField(
                    value = values[field] ?: "",
                    onValueChange = { text -> values[field] = text.filter { it.isDigit() || it == '.' || it == ',' } },
                    label = { Text(labels.getValue(field)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ResultRow(strings.zakatAssets, result.assets)
                    if (result.deductions > 0) ResultRow(strings.zakatDeductions, -result.deductions)
                    ResultRow(strings.zakatNet, result.net)
                    if (result.nisabKnown) ResultRow(strings.zakatNisab, result.nisab)
                    HorizontalDivider()
                    when {
                        !result.nisabKnown -> Text(strings.zakatNeedPrice, style = MaterialTheme.typography.bodyMedium)
                        result.isDue -> ResultRow(strings.zakatDue, result.zakat, emphasized = true)
                        else -> Text(strings.zakatNotDue, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            NoticeCard(strings.zakatDisclaimer)
            Text(strings.zakatRecipientsTitle, style = MaterialTheme.typography.titleSmall)
            Text(strings.zakatRecipients, style = MaterialTheme.typography.bodyMedium)
            MoreInfo(strings.zakatSources, strings.source)
        }
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun ResultRow(label: String, value: Double, emphasized: Boolean = false) {
    val style = if (emphasized) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = style, modifier = Modifier.weight(1f))
        Text(formatAmount(value), style = style)
    }
}

/** Whole units with thin-space grouping: currencies differ, so no symbol. */
private fun formatAmount(value: Double): String {
    val rounded = value.roundToLong()
    val digits = kotlin.math.abs(rounded).toString().reversed().chunked(3).joinToString(" ").reversed()
    return if (rounded < 0) "−$digits" else digits
}
