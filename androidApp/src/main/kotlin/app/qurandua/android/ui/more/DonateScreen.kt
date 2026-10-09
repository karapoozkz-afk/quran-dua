package app.qurandua.android.ui.more

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.qurandua.android.ui.LocalContentLang
import app.qurandua.android.ui.LocalDeps
import app.qurandua.android.ui.LocalStrings
import app.qurandua.android.ui.components.BackTopBar
import app.qurandua.shared.i18n.ADHAN_CREDIT
import app.qurandua.shared.model.Charity
import app.qurandua.shared.model.pick

/**
 * Sadaqah and support: zakat, verified funds (each links to its own payment page; the app never
 * holds money), supporting the app, and the text sources and licences at the bottom.
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
        item {
            Text(
                strings.charitiesTitle,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
        if (deps.content.charities.isEmpty()) {
            item {
                Text(
                    strings.charitiesEmpty,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
        items(deps.content.charities, key = { it.id }) { charity ->
            CharityCard(charity, Modifier.padding(horizontal = 16.dp))
        }
        item {
            Text(
                strings.supportApp,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        item {
            // Licences (CC BY-SA) require the attribution somewhere in the app: it closes this screen.
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                HorizontalDivider()
                Text(strings.sourcesTitle, style = MaterialTheme.typography.titleSmall)
                Text(
                    strings.sourcesBody + "\n\nAdhan: " + ADHAN_CREDIT,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
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
