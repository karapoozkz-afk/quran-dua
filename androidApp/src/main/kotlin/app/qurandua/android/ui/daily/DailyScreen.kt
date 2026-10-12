package app.qurandua.android.ui.daily

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import app.qurandua.android.ui.LocalDeps
import app.qurandua.android.ui.components.BackTopBar
import app.qurandua.android.ui.components.MoreInfo
import app.qurandua.android.ui.DailyController
import app.qurandua.android.ui.LocalStrings
import app.qurandua.shared.daily.DailyHadith
import app.qurandua.shared.daily.DailySlot
import app.qurandua.shared.daily.DailyStrings
import app.qurandua.shared.daily.dailyStringsFor
import app.qurandua.shared.daily.fallbackLanguageName
import app.qurandua.shared.daily.forLang
import app.qurandua.shared.daily.formatMinute
import app.qurandua.shared.daily.isFallbackFor
import app.qurandua.shared.learn.LessonGrade
import app.qurandua.shared.learn.label
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private const val PERMISSION_NOTIFICATIONS = "android.permission.POST_NOTIFICATIONS"

/** Today's morning and evening hadith, and when the two reminders come. */
@Composable
fun DailyScreen(daily: DailyController, lang: String, onBack: () -> Unit) {
    val strings = dailyStringsFor(lang)
    val deps = LocalDeps.current
    val settings by daily.settings.collectAsState()
    val today = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }
    var editing by remember { mutableStateOf<DailySlot?>(null) }

    Scaffold(topBar = { BackTopBar(strings.section, onBack) }) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            for (slot in DailySlot.entries) {
                val hadith = daily.content.forDay(today, slot) ?: continue
                item(key = slot.name) { HadithCard(hadith, slot, lang, strings) }
            }
            item(key = "settings") {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(strings.remind, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                            Switch(
                                checked = settings.on,
                                onCheckedChange = { on ->
                                    if (on) deps.permissions.request(PERMISSION_NOTIFICATIONS) { daily.update { it.copy(on = true) } }
                                    else daily.update { it.copy(on = false) }
                                },
                            )
                        }
                        if (settings.on) {
                            TimeRow(strings.morningTime, settings.morningMinute) { editing = DailySlot.MORNING }
                            TimeRow(strings.eveningTime, settings.eveningMinute) { editing = DailySlot.EVENING }
                        }
                    }
                }
            }
        }
    }

    editing?.let { slot ->
        val minute = if (slot == DailySlot.MORNING) settings.morningMinute else settings.eveningMinute
        TimeDialog(minute, strings, onDismiss = { editing = null }) { picked ->
            daily.update { if (slot == DailySlot.MORNING) it.copy(morningMinute = picked) else it.copy(eveningMinute = picked) }
            editing = null
        }
    }
}

@Composable
private fun HadithCard(hadith: DailyHadith, slot: DailySlot, lang: String, strings: DailyStrings) {
    val morning = slot == DailySlot.MORNING
    Card(
        Modifier.fillMaxWidth(),
        colors = if (morning) CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) else CardDefaults.cardColors(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(slotIcon(slot), contentDescription = null)
                Text(if (morning) strings.morning else strings.evening, style = MaterialTheme.typography.labelLarge)
            }
            Text(hadith.title.forLang(lang), style = MaterialTheme.typography.titleLarge)
            Text(hadith.summary.forLang(lang), style = MaterialTheme.typography.bodyLarge)
            Text(hadith.text.forLang(lang), style = MaterialTheme.typography.bodyMedium)
            val note = buildString {
                append(strings.source(hadith)).append(" · ").append(LessonGrade.SAHIH.label(lang))
                if (hadith.text.isFallbackFor(lang)) append("\n").append(strings.translationIn.replace("%1", fallbackLanguageName(lang)))
            }
            MoreInfo(note, LocalStrings.current.source)
        }
    }
}

private fun slotIcon(slot: DailySlot): ImageVector = if (slot == DailySlot.MORNING) Icons.Filled.WbSunny else Icons.Filled.Bedtime

@Composable
private fun TimeRow(label: String, minute: Int, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        TextButton(onClick = onClick) { Text(formatMinute(minute), style = MaterialTheme.typography.titleMedium) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeDialog(minute: Int, strings: DailyStrings, onDismiss: () -> Unit, onPick: (Int) -> Unit) {
    val state = rememberTimePickerState(initialHour = minute / 60, initialMinute = minute % 60, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = { onPick(state.hour * 60 + state.minute) }) { Text(strings.done) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } },
        text = { TimePicker(state = state) },
    )
}
