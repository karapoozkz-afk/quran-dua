package app.bibliya.android.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.bibliya.android.ui.LocalDeps
import app.bibliya.android.ui.LocalStrings
import app.bibliya.android.ui.LocalTranslation
import app.bibliya.android.ui.LocalUiLanguage
import app.bibliya.android.ui.components.VersesText
import app.bibliya.android.ui.components.refLabel
import app.bibliya.android.ui.components.shareText
import app.bibliya.shared.data.inTranslation
import app.bibliya.shared.model.DailyVerse
import app.bibliya.shared.model.PlanSegment
import app.bibliya.shared.model.ReadingPosition
import app.bibliya.shared.model.Situation
import app.bibliya.shared.model.VerseRef
import app.bibliya.shared.model.pick

/** Today's reading of the yearly plan, as Home shows it. */
data class PlanToday(val day: Int, val segments: List<PlanSegment>, val doneCount: Int, val doneToday: Boolean)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    verseOfDay: DailyVerse?,
    situations: List<Situation>,
    lastRead: ReadingPosition?,
    plan: PlanToday?,
    scrollToVerse: Boolean,
    onVerseShown: () -> Unit,
    onOpenSearch: (String) -> Unit,
    onOpenSituation: (String) -> Unit,
    onAllSituations: () -> Unit,
    onOpenRef: (VerseRef) -> Unit,
    onContinue: (ReadingPosition) -> Unit,
    onOpenPlan: () -> Unit,
    onStartPlan: () -> Unit,
    onMarkPlanDay: (Int) -> Unit,
    listState: LazyListState = rememberLazyListState(),
) {
    val strings = LocalStrings.current
    val deps = LocalDeps.current
    val lang = LocalTranslation.current
    val uiLang = LocalUiLanguage.current

    LaunchedEffect(scrollToVerse) {
        if (scrollToVerse) {
            listState.animateScrollToItem(1)
            onVerseShown()
        }
    }

    LazyColumn(state = listState, contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(strings.homeQuestion, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth().clickable { onOpenSearch("") },
                ) {
                    Row(Modifier.padding(horizontal = 18.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.size(12.dp))
                        Text(strings.searchHint, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    strings.searchExamples.forEach { example ->
                        AssistChip(onClick = { onOpenSearch(example) }, label = { Text(example) })
                    }
                }
            }
        }

        if (verseOfDay != null) {
            item {
                val ref = verseOfDay.refFor(lang)
                val label = ref?.let { deps.content.refLabel(it, lang) }.orEmpty()
                val text = verseOfDay.plainText(lang)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                ) {
                    Column(Modifier.padding(start = 18.dp, end = 8.dp, top = 16.dp, bottom = 4.dp)) {
                        Text(strings.verseOfDay, style = MaterialTheme.typography.labelLarge)
                        VersesText(
                            (verseOfDay.text[lang] ?: emptyList()).map { it[0].toInt() to it[1] },
                            Modifier.padding(top = 8.dp, end = 10.dp),
                            scale = 1.1f,
                            numbers = false,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(label, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                            IconButton(onClick = { deps.platform.copy(shareText(text, label, lang), strings.copied) }) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = strings.copy, modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = { deps.platform.share(shareText(text, label, lang)) }) {
                                Icon(Icons.Filled.Share, contentDescription = strings.share, modifier = Modifier.size(20.dp))
                            }
                            if (ref != null) {
                                IconButton(onClick = { onOpenRef(ref) }) {
                                    Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = strings.openInBible)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (lastRead != null) {
            item {
                val book = deps.content.book(lastRead.book)
                Card(Modifier.fillMaxWidth().clickable { onContinue(lastRead) }) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.size(14.dp))
                        Column {
                            Text(strings.continueReading, style = MaterialTheme.typography.labelLarge)
                            Text(
                                "${book?.name(lastRead.lang) ?: lastRead.book} ${lastRead.chapter}",
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(Modifier.fillMaxWidth().clickable(onClick = onOpenPlan)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.size(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(strings.planTitle, style = MaterialTheme.typography.titleMedium)
                            Text(
                                if (plan == null) strings.planSubtitle else strings.planProgress(plan.doneCount, deps.content.plan.size),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    if (plan == null) {
                        TextButton(onClick = onStartPlan) { Text(strings.planStart) }
                    } else {
                        Text(strings.planToday(plan.day), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        plan.segments.forEach { seg ->
                            val local = seg.inTranslation(lang)
                            val name = deps.content.book(local.book)?.name(lang) ?: local.book
                            val chapters = if (local.to != local.from) "${local.from}–${local.to}" else "${local.from}"
                            Text(
                                "$name $chapters",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.clickable { onOpenRef(VerseRef(local.book, local.from)) }.padding(vertical = 2.dp),
                            )
                        }
                        if (plan.doneToday) {
                            Text(strings.planDone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            TextButton(onClick = { onMarkPlanDay(plan.day) }) { Text(strings.planMarkRead) }
                        }
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(strings.navSituations, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    TextButton(onClick = onAllSituations) { Text(strings.allSituations) }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    situations.take(10).forEach { s ->
                        AssistChip(onClick = { onOpenSituation(s.id) }, label = { Text("${s.emoji} ${s.title.pick(uiLang)}") })
                    }
                }
            }
        }
    }
}
