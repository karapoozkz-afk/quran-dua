package app.qurandua.android.ui.journal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.qurandua.android.ui.WallController
import app.qurandua.android.ui.components.NoticeCard
import app.qurandua.shared.journal.JournalKind
import app.qurandua.shared.journal.JournalStrings
import app.qurandua.shared.journal.WALL_MAX_CHARS
import app.qurandua.shared.journal.WallPost
import app.qurandua.shared.journal.WallStrings
import kotlinx.coroutines.launch

/**
 * The shared wall: approved posts in the reader's language. Posts wait for the moderator before
 * anyone sees them, and the only answer is one of the ready-made duas, so no one can judge.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WallTab(wall: WallController, language: String, s: JournalStrings, w: WallStrings) {
    if (!wall.configured) {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) { NoticeCard(w.notReady) }
        return
    }
    val scope = rememberCoroutineScope()
    var posts by remember { mutableStateOf<List<WallPost>?>(null) }
    var failed by remember { mutableStateOf(false) }
    var reload by remember { mutableIntStateOf(0) }
    var text by rememberSaveable { mutableStateOf("") }
    var kind by rememberSaveable { mutableStateOf(JournalKind.REGRET) }
    var sending by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var toReport by remember { mutableStateOf<WallPost?>(null) }
    // Answers given in this session, shown before the next reload brings the new counts.
    val given = remember { mutableStateMapOf<String, Int>() }

    LaunchedEffect(language, reload) {
        failed = false
        val fresh = runCatching { wall.posts(language) }.onFailure { failed = true }.getOrNull()
        if (fresh != null) {
            posts = fresh
            // The fresh counts already include the answers given here.
            given.clear()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { NoticeCard(w.notice) }
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
                        onValueChange = { text = it.take(WALL_MAX_CHARS) },
                        placeholder = { Text(w.hint) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                    )
                    Button(
                        enabled = text.isNotBlank() && !sending,
                        onClick = {
                            sending = true
                            scope.launch {
                                val ok = runCatching { wall.send(text.trim(), kind, language) }.isSuccess
                                if (ok) text = ""
                                message = if (ok) w.sent else w.offline
                                sending = false
                            }
                        },
                    ) { Text(w.send) }
                    message?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(w.tabWall, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = { reload++ }) { Icon(Icons.Filled.Refresh, contentDescription = w.retry) }
            }
        }
        val list = posts
        if (list == null) {
            if (failed) item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(w.offline, color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = { reload++ }) { Text(w.retry) }
                }
            }
            else item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }
        } else if (list.isEmpty()) {
            item {
                Text(w.empty, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(list, key = { it.id }) { post ->
                val mine = given[post.id] ?: wall.myDua(post.id)
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                s.kind(post.kind) + " · " + formatDate(post.createdAt),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f),
                            )
                            if (!wall.reported(post.id)) {
                                IconButton(onClick = { toReport = post }) { Icon(Icons.Filled.Flag, contentDescription = w.report) }
                            }
                        }
                        Text(post.text, style = MaterialTheme.typography.bodyLarge)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            w.duas.forEachIndexed { index, label ->
                                // Until the next reload, an answer given here is added to the loaded count.
                                val count = post.duas.getOrElse(index) { 0 } + if (given[post.id] == index) 1 else 0
                                FilterChip(
                                    selected = mine == index,
                                    enabled = mine == null,
                                    onClick = {
                                        given[post.id] = index
                                        scope.launch {
                                            if (runCatching { wall.dua(post.id, index) }.isFailure) {
                                                given.remove(post.id)
                                                message = w.offline
                                            }
                                        }
                                    },
                                    label = { Text(if (count > 0) "$label · $count" else label) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    toReport?.let { post ->
        AlertDialog(
            onDismissRequest = { toReport = null },
            text = { Text(w.reportConfirm) },
            confirmButton = {
                TextButton(onClick = {
                    toReport = null
                    scope.launch {
                        message = if (runCatching { wall.report(post.id) }.isSuccess) w.reported else w.offline
                    }
                }) { Text(w.report, color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { toReport = null }) { Text(s.cancel) } },
        )
    }
}
