package app.bibliya.android.ui.situations

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bibliya.android.ui.LocalStrings
import app.bibliya.android.ui.LocalUiLanguage
import app.bibliya.android.ui.components.BackTopBar
import app.bibliya.android.ui.components.NoticeCard
import app.bibliya.android.ui.components.PassageCard
import app.bibliya.shared.model.Passage
import app.bibliya.shared.model.Situation
import app.bibliya.shared.model.VerseRef
import app.bibliya.shared.model.pick

/** "What to read": every life situation as a tile, with search on top. */
@Composable
fun SituationsScreen(situations: List<Situation>, onOpenSituation: (String) -> Unit, onOpenSearch: () -> Unit) {
    val strings = LocalStrings.current
    val lang = LocalUiLanguage.current
    LazyVerticalGrid(
        columns = GridCells.Adaptive(160.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenSearch),
            ) {
                Row(Modifier.padding(horizontal = 18.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.size(12.dp))
                    Text(strings.searchHint, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        items(situations, key = { it.id }) { s ->
            Card(Modifier.heightIn(min = 118.dp).clickable { onOpenSituation(s.id) }) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(s.emoji, fontSize = 26.sp)
                    Text(s.title.pick(lang), style = MaterialTheme.typography.titleSmall)
                    Text(
                        s.subtitle.pick(lang),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
fun SituationScreen(
    situation: Situation,
    passages: List<Passage>,
    favorites: Set<String>,
    onToggleFavorite: (String) -> Unit,
    onOpenRef: (VerseRef) -> Unit,
    onBack: () -> Unit,
) {
    val lang = LocalUiLanguage.current
    Scaffold(topBar = { BackTopBar("${situation.emoji} ${situation.title.pick(lang)}", onBack) }) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(padding),
        ) {
            item {
                Text(situation.subtitle.pick(lang), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            situation.notice?.let { notice -> item { NoticeCard(notice.pick(lang)) } }
            items(passages, key = { it.id }) { p ->
                PassageCard(p, favorite = p.id in favorites, onToggleFavorite = { onToggleFavorite(p.id) }, onOpen = onOpenRef)
            }
        }
    }
}
