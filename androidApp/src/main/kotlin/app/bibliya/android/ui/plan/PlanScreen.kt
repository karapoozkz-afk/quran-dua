package app.bibliya.android.ui.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.bibliya.android.ui.LocalDeps
import app.bibliya.android.ui.LocalStrings
import app.bibliya.android.ui.LocalTranslation
import app.bibliya.android.ui.components.BackTopBar
import app.bibliya.shared.data.inTranslation
import app.bibliya.shared.model.PlanSegment
import app.bibliya.shared.model.VerseRef

/** The first day not yet marked: where the reader picks up, however many days were missed. */
fun currentPlanDay(done: Set<Int>, total: Int): Int = (1..total).firstOrNull { it !in done } ?: total

@Composable
fun PlanScreen(
    plan: List<List<PlanSegment>>,
    started: Boolean,
    done: Set<Int>,
    onStart: () -> Unit,
    onReset: () -> Unit,
    onToggleDay: (Int, Boolean) -> Unit,
    onOpenRef: (VerseRef) -> Unit,
    onBack: () -> Unit,
) {
    val strings = LocalStrings.current
    val deps = LocalDeps.current
    val lang = LocalTranslation.current
    val current = currentPlanDay(done, plan.size)
    val listState = rememberLazyListState()
    var confirmReset by remember { mutableStateOf(false) }
    LaunchedEffect(started) { if (started) listState.scrollToItem((current - 1 + 1).coerceAtLeast(0)) }

    Scaffold(topBar = {
        BackTopBar(strings.planTitle, onBack) {
            if (started) TextButton(onClick = { confirmReset = true }) { Text(strings.planRestart) }
        }
    }) { padding ->
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(padding),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                    Text(strings.planNote, style = MaterialTheme.typography.bodyMedium)
                    if (started) {
                        Text(strings.planProgress(done.size, plan.size), style = MaterialTheme.typography.labelLarge)
                        LinearProgressIndicator(progress = { done.size / plan.size.toFloat() }, modifier = Modifier.fillMaxWidth())
                    } else {
                        Button(onClick = onStart) { Text(strings.planStart) }
                    }
                }
            }
            itemsIndexed(plan) { index, segments ->
                val day = index + 1
                val isCurrent = started && day == current
                Row(
                    Modifier.fillMaxWidth()
                        .background(
                            if (isCurrent) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(10.dp),
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(strings.planDay(day), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        segments.forEach { seg ->
                            val local = seg.inTranslation(lang)
                            val name = deps.content.book(local.book)?.name(lang) ?: local.book
                            val chapters = if (local.to != local.from) "${local.from}–${local.to}" else "${local.from}"
                            Text(
                                "$name $chapters",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.clickable { onOpenRef(VerseRef(local.book, local.from)) }.padding(vertical = 2.dp),
                            )
                        }
                    }
                    if (started) {
                        Checkbox(checked = day in done, onCheckedChange = { onToggleDay(day, it) })
                    }
                }
            }
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text(strings.planRestart) },
            text = { Text(strings.planRestartConfirm) },
            confirmButton = {
                TextButton(onClick = {
                    confirmReset = false
                    onReset()
                }) { Text(strings.planRestart) }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text(strings.cancel) } },
        )
    }
}
