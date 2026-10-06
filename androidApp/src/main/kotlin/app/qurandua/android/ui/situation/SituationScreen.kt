package app.qurandua.android.ui.situation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.qurandua.android.ui.LocalContentLang
import app.qurandua.android.ui.components.BackTopBar
import app.qurandua.android.ui.components.DuaCard
import app.qurandua.android.ui.components.NoticeCard
import app.qurandua.shared.model.AyahKey
import app.qurandua.shared.model.DuaItem
import app.qurandua.shared.model.Situation
import app.qurandua.shared.model.pick

@Composable
fun SituationScreen(
    situation: Situation,
    duas: List<DuaItem>,
    favorites: Set<String>,
    onBack: () -> Unit,
    onOpenInQuran: (AyahKey) -> Unit,
) {
    val lang = LocalContentLang.current
    Scaffold(topBar = { BackTopBar("${situation.emoji} ${situation.title.pick(lang)}", onBack) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        situation.subtitle.pick(lang),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    situation.notice?.pick(lang)?.takeIf { it.isNotBlank() }?.let { NoticeCard(it) }
                }
            }
            items(duas, key = { it.id }) { dua ->
                DuaCard(item = dua, isFavorite = dua.id in favorites, onOpenInQuran = onOpenInQuran)
            }
        }
    }
}
