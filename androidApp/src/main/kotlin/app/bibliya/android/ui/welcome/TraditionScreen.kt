package app.bibliya.android.ui.welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.bibliya.android.ui.LocalStrings
import app.bibliya.shared.model.Tradition

/** The traditions offered, in the order shown on the first launch and in settings. */
val TRADITION_CHOICES = listOf(Tradition.ORTHODOX, Tradition.CATHOLIC, Tradition.PROTESTANT, Tradition.ANY)

/** First launch: the one question the app asks before opening. */
@Composable
fun TraditionScreen(onChoose: (Tradition) -> Unit) {
    val strings = LocalStrings.current
    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(strings.appName, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Text(strings.traditionQuestion, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Text(strings.traditionNote, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TRADITION_CHOICES.forEach { tradition ->
                OutlinedCard(onClick = { onChoose(tradition) }, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                        Text(strings.traditionName(tradition), style = MaterialTheme.typography.titleMedium)
                        Text(
                            strings.traditionHint(tradition),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
