package app.qurandua.android.ui.journal

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import app.qurandua.android.ui.JournalController
import app.qurandua.android.ui.WallController
import app.qurandua.shared.journal.wallStringsFor
import app.qurandua.android.ui.components.BackTopBar
import app.qurandua.android.ui.components.DuaCard
import app.qurandua.android.ui.components.NoticeCard
import app.qurandua.android.ui.components.SectionTitle
import app.qurandua.shared.journal.JournalEntry
import app.qurandua.shared.journal.JournalKind
import app.qurandua.shared.journal.JournalStrings
import app.qurandua.shared.journal.isValidPin
import app.qurandua.shared.journal.journalStringsFor
import app.qurandua.shared.model.AyahKey
import app.qurandua.shared.model.DuaItem
import kotlinx.coroutines.delay
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * The repentance journal: regrets, grudges and repentance, written for no one but the person.
 * The screen is kept out of screenshots and the recent-apps preview, and locks again whenever the
 * app goes to the background. The duas for forgiveness come from the existing content as they are.
 */
@Composable
fun JournalScreen(
    journal: JournalController,
    wall: WallController?,
    language: String,
    duas: List<DuaItem>,
    favorites: Set<String>,
    onOpenInQuran: (AyahKey) -> Unit,
    onBack: () -> Unit,
) {
    val s = journalStringsFor(language)
    val w = wallStringsFor(language)
    val hasPin by journal.hasPin.collectAsState()
    var unlocked by rememberSaveable { mutableStateOf(false) }
    // Shared first, as asked; the personal tab is one tap away.
    var tab by rememberSaveable { mutableStateOf(if (wall == null) 1 else 0) }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { unlocked = false }
    SecureWindow()

    Scaffold(topBar = { BackTopBar(s.title, onBack) }) { padding ->
        Column(Modifier.padding(padding)) {
            if (wall != null) {
                TabRow(selectedTabIndex = tab) {
                    Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text(w.tabWall) })
                    Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text(w.tabMine) })
                }
            }
            when {
                wall != null && tab == 0 -> WallTab(wall, language, s, w)
                hasPin && !unlocked -> PinGate(journal, s, onUnlocked = { unlocked = true }, modifier = Modifier)
                else -> JournalBody(journal, s, duas, favorites, onOpenInQuran, onPinSet = { unlocked = true }, modifier = Modifier)
            }
        }
    }
}

/** FLAG_SECURE while the journal is on screen: no screenshots, blank in the app switcher. */
@Composable
private fun SecureWindow() {
    val window = (LocalContext.current as? Activity)?.window
    DisposableEffect(window) {
        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose { window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun JournalBody(
    journal: JournalController,
    s: JournalStrings,
    duas: List<DuaItem>,
    favorites: Set<String>,
    onOpenInQuran: (AyahKey) -> Unit,
    onPinSet: () -> Unit,
    modifier: Modifier,
) {
    val entries by journal.entries.collectAsState()
    val hasPin by journal.hasPin.collectAsState()
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    var text by rememberSaveable { mutableStateOf("") }
    var kind by rememberSaveable { mutableStateOf(JournalKind.REGRET) }
    var duasOpen by rememberSaveable { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<JournalEntry?>(null) }
    var askDeleteAll by remember { mutableStateOf(false) }
    var settingPin by remember { mutableStateOf(false) }

    fun reset() {
        editingId = null
        text = ""
        kind = JournalKind.REGRET
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(s.subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item { NoticeCard(s.privacy) }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        JournalKind.entries.forEach { k ->
                            FilterChip(selected = kind == k, onClick = { kind = k }, label = { Text(s.kind(k)) })
                        }
                    }
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        placeholder = { Text(s.hint) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            enabled = text.isNotBlank(),
                            onClick = {
                                val now = System.currentTimeMillis()
                                val old = entries.firstOrNull { it.id == editingId }
                                journal.save(JournalEntry(old?.id ?: now, old?.createdAt ?: now, kind, text.trim()))
                                reset()
                            },
                        ) { Text(s.save) }
                        if (editingId != null) TextButton(onClick = ::reset) { Text(s.cancel) }
                    }
                }
            }
        }
        if (duas.isNotEmpty()) {
            item {
                Card(onClick = { duasOpen = !duasOpen }, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(s.duasTitle, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Icon(if (duasOpen) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null)
                    }
                }
            }
            if (duasOpen) {
                items(duas, key = { "dua-" + it.id }) { dua ->
                    DuaCard(item = dua, isFavorite = dua.id in favorites, onOpenInQuran = onOpenInQuran)
                }
            }
        }
        item { SectionTitle(s.entriesTitle, Modifier.padding(top = 4.dp)) }
        if (entries.isEmpty()) {
            item { Text(s.empty, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        items(entries, key = { it.id }) { entry ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        s.kind(entry.kind) + " · " + formatDate(entry.createdAt),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(entry.text, style = MaterialTheme.typography.bodyLarge)
                    Row {
                        TextButton(onClick = {
                            editingId = entry.id
                            text = entry.text
                            kind = entry.kind
                        }) { Text(s.edit) }
                        TextButton(onClick = { toDelete = entry }) { Text(s.delete) }
                    }
                }
            }
        }
        item { SectionTitle(s.protection, Modifier.padding(top = 8.dp)) }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(s.pinLock, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f).padding(start = 12.dp))
                Switch(checked = hasPin, onCheckedChange = { on -> if (on) settingPin = true else journal.clearPin() })
            }
        }
        if (entries.isNotEmpty()) {
            item { TextButton(onClick = { askDeleteAll = true }) { Text(s.deleteAll, color = MaterialTheme.colorScheme.error) } }
        }
    }

    toDelete?.let { entry ->
        ConfirmDialog(s.deleteConfirm, s.delete, s.cancel, onConfirm = {
            journal.delete(entry.id)
            if (editingId == entry.id) reset()
            toDelete = null
        }, onDismiss = { toDelete = null })
    }
    if (askDeleteAll) {
        ConfirmDialog(s.deleteAllConfirm, s.delete, s.cancel, onConfirm = {
            journal.deleteAll()
            reset()
            askDeleteAll = false
        }, onDismiss = { askDeleteAll = false })
    }
    if (settingPin) {
        NewPinDialog(s, onDone = { pin ->
            journal.setPin(pin)
            onPinSet()
            settingPin = false
        }, onDismiss = { settingPin = false })
    }
}

/** Asks for the PIN before anything of the journal is shown. */
@Composable
private fun PinGate(journal: JournalController, s: JournalStrings, onUnlocked: () -> Unit, modifier: Modifier) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var wait by remember { mutableStateOf(journal.waitMillis()) }
    var forgot by remember { mutableStateOf(false) }
    LaunchedEffect(wait > 0) {
        while (journal.waitMillis() > 0) {
            wait = journal.waitMillis()
            delay(1_000)
        }
        wait = 0
    }

    fun tryUnlock() {
        if (journal.checkPin(pin)) {
            onUnlocked()
        } else {
            pin = ""
            wait = journal.waitMillis()
            error = s.wrongPin
        }
    }

    Column(
        modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(s.enterPin, style = MaterialTheme.typography.titleMedium)
        PinField(pin, onChange = { pin = it; error = null }, onDone = ::tryUnlock)
        val message = if (wait > 0) s.tooManyTries.replace("%1", ((wait + 999) / 1000).toString()) else error
        if (message != null) Text(message, color = MaterialTheme.colorScheme.error)
        Button(enabled = wait == 0L && isValidPin(pin), onClick = ::tryUnlock) { Text(s.unlock) }
        TextButton(onClick = { forgot = true }) { Text(s.forgotPin) }
    }

    if (forgot) {
        AlertDialog(
            onDismissRequest = { forgot = false },
            title = { Text(s.forgotPin) },
            text = { Text(s.forgotBody) },
            confirmButton = {
                TextButton(onClick = {
                    journal.deleteAll()
                    forgot = false
                    onUnlocked()
                }) { Text(s.deleteAll, color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { forgot = false }) { Text(s.cancel) } },
        )
    }
}

/** New PIN, typed twice. */
@Composable
private fun NewPinDialog(s: JournalStrings, onDone: (String) -> Unit, onDismiss: () -> Unit) {
    var first by remember { mutableStateOf("") }
    var second by remember { mutableStateOf("") }
    val mismatch = second.length >= first.length && second.isNotEmpty() && first != second
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.pinLock) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(s.newPin)
                PinField(first, onChange = { first = it })
                Text(s.repeatPin)
                PinField(second, onChange = { second = it })
                if (mismatch) Text(s.pinMismatch, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            TextButton(enabled = isValidPin(first) && first == second, onClick = { onDone(first) }) { Text(s.save) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancel) } },
    )
}

@Composable
private fun PinField(value: String, onChange: (String) -> Unit, onDone: (() -> Unit)? = null) {
    OutlinedTextField(
        value = value,
        onValueChange = { v -> onChange(v.filter { it in '0'..'9' }.take(8)) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { onDone?.invoke() }),
    )
}

@Composable
private fun ConfirmDialog(text: String, confirm: String, cancel: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { Text(text) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirm, color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(cancel) } },
    )
}

/** Numeric date and time, readable in every language: 15.10.2026 14:05. */
internal fun formatDate(millis: Long): String {
    val t = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.currentSystemDefault())
    fun two(n: Int) = n.toString().padStart(2, '0')
    return "${two(t.dayOfMonth)}.${two(t.monthNumber)}.${t.year} ${two(t.hour)}:${two(t.minute)}"
}
