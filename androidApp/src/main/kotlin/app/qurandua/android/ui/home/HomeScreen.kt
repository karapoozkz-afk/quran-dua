package app.qurandua.android.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.qurandua.android.ui.LocalContentLang
import app.qurandua.android.ui.LocalStrings
import app.qurandua.shared.model.AyahKey
import app.qurandua.shared.model.Situation
import app.qurandua.shared.model.pick

/**
 * The home screen answers one question: "what do I read in my situation?"
 * The search field and the situation grid are the two ways in; the Quran
 * reader sits right under them.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    situations: List<Situation>,
    lastRead: AyahKey?,
    onSearch: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSituation: (String) -> Unit,
    onOpenQuran: () -> Unit,
    onContinueReading: (AyahKey) -> Unit,
    prayerTitle: String,
    prayerLine: String?,
    onOpenPrayer: () -> Unit,
    learnTitle: String = "",
    learnSubtitle: String = "",
    onOpenLearn: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    val lang = LocalContentLang.current

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(strings.whatToRead, style = MaterialTheme.typography.headlineSmall)
        }
        item {
            Card(onClick = onOpenSearch, modifier = Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        strings.searchHint,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                strings.searchExamples.forEach { example ->
                    AssistChip(onClick = { onSearch(example) }, label = { Text(example) })
                }
            }
        }
        if (lastRead != null) {
            item {
                Card(
                    onClick = { onContinueReading(lastRead) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(strings.continueReading, style = MaterialTheme.typography.titleSmall)
                        Text(
                            strings.surahAyah(lastRead.surah, lastRead.ayah),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
        item {
            Card(onClick = onOpenPrayer, modifier = Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(Icons.Filled.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text(prayerTitle, style = MaterialTheme.typography.titleMedium)
                        if (prayerLine != null) Text(prayerLine, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        item {
            Card(onClick = onOpenLearn, modifier = Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(Icons.Filled.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text(learnTitle, style = MaterialTheme.typography.titleMedium)
                        Text(learnSubtitle, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        item {
            Card(onClick = onOpenQuran, modifier = Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(strings.navQuran, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        item { Text(strings.situations, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp)) }
        items(situations, key = { it.id }) { situation ->
            Card(onClick = { onOpenSituation(situation.id) }, modifier = Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box { Text(situation.emoji, style = MaterialTheme.typography.headlineSmall) }
                    Column {
                        Text(situation.title.pick(lang), style = MaterialTheme.typography.titleMedium)
                        Text(
                            situation.subtitle.pick(lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
