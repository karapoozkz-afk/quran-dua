package app.qurandua.android.ui.more

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.qurandua.android.ui.LocalContentLang
import app.qurandua.android.ui.LocalDeps
import app.qurandua.android.ui.LocalStrings
import app.qurandua.android.ui.LocalUiLanguage
import app.qurandua.android.ui.SupportController
import app.qurandua.android.ui.SupportResult
import app.qurandua.android.ui.components.BackTopBar
import app.qurandua.shared.daily.HADITH_CREDIT
import app.qurandua.shared.i18n.ADHAN_CREDIT
import app.qurandua.shared.i18n.supportStringsFor
import app.qurandua.shared.model.Charity
import app.qurandua.shared.model.pick

/**
 * Sadaqah and support: zakat, verified funds (each links to its own payment page; the app never
 * holds money), supporting the project through Google Play, and the sources and licences at the bottom.
 */
@Composable
fun DonateScreen(onOpenZakat: () -> Unit, onBack: () -> Unit) {
    val strings = LocalStrings.current
    val deps = LocalDeps.current
    Scaffold(topBar = { BackTopBar(strings.donate, onBack) }) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
        item {
            Card(onClick = onOpenZakat, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(strings.zakat, style = MaterialTheme.typography.titleMedium)
                    Text(strings.zakatIntro, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        // Funds appear only once verified ones are added (content-src/charities.json); until then
        // the section is not shown at all rather than promising something empty.
        if (deps.content.charities.isNotEmpty()) {
            item {
                Text(
                    strings.charitiesTitle,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
            items(deps.content.charities, key = { it.id }) { charity ->
                CharityCard(charity, Modifier.padding(horizontal = 16.dp))
            }
        }
        deps.support?.let { support ->
            item { SupportCard(support, Modifier.padding(horizontal = 16.dp)) }
        }

        item {
            // Licences (CC BY-SA) require the attribution somewhere in the app: it closes this screen.
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                HorizontalDivider()
                Text(strings.sourcesTitle, style = MaterialTheme.typography.titleSmall)
                Text(
                    strings.sourcesBody + "\n\nAdhan: " + ADHAN_CREDIT + "\n\n" + HADITH_CREDIT,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SupportCard(support: SupportController, modifier: Modifier = Modifier) {
    val texts = supportStringsFor(LocalUiLanguage.current)
    val options by support.options.collectAsState()
    val result by support.result.collectAsState()
    LaunchedEffect(support) { support.connect() }
    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(texts.title, style = MaterialTheme.typography.titleMedium)
            Text(texts.body, style = MaterialTheme.typography.bodySmall)
            if (options.isEmpty()) {
                Text(
                    texts.unavailable,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    options.forEach { option ->
                        Button(onClick = { support.clearResult(); support.buy(option.id) }) { Text(option.price) }
                    }
                }
            }
            result?.let {
                Text(
                    when (it) {
                        SupportResult.Thanks -> texts.thanks
                        SupportResult.Pending -> texts.pending
                        SupportResult.Failed -> texts.failed
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (it == SupportResult.Failed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun CharityCard(charity: Charity, modifier: Modifier = Modifier) {
    val strings = LocalStrings.current
    val lang = LocalContentLang.current
    val deps = LocalDeps.current
    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(charity.name.pick(lang), style = MaterialTheme.typography.titleMedium)
            Text(
                charity.purposes.joinToString(" · ") { strings.charityPurpose(it) },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text("${strings.charityReg}: ${charity.registrationNumber} (${charity.country})", style = MaterialTheme.typography.bodySmall)
            Text(
                strings.charityVerified(charity.verifiedOn, charity.verifiedBy),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { deps.platform.openUrl(charity.donateUrl) }) { Text(strings.charityDonate) }
                TextButton(onClick = { deps.platform.openUrl(charity.website) }) { Text(strings.charityWebsite) }
            }
        }
    }
}
