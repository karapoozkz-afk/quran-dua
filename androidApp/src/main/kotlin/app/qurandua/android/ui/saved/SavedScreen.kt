package app.qurandua.android.ui.saved

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.qurandua.android.ui.LocalStrings
import app.qurandua.android.ui.components.DuaCard
import app.qurandua.android.ui.components.EmptyState
import app.qurandua.android.ui.components.SectionTitle
import app.qurandua.shared.model.AyahKey
import app.qurandua.shared.model.DuaItem

@Composable
fun SavedScreen(
    duas: List<DuaItem>,
    bookmarks: List<AyahKey>,
    favorites: Set<String>,
    onOpenAyah: (AyahKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    if (duas.isEmpty() && bookmarks.isEmpty()) {
        EmptyState(strings.savedEmpty, modifier.fillMaxSize())
        return
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (duas.isNotEmpty()) {
            item { SectionTitle(strings.savedDuas, Modifier.padding(horizontal = 0.dp)) }
            items(duas, key = { it.id }) { dua ->
                DuaCard(item = dua, isFavorite = dua.id in favorites, onOpenInQuran = onOpenAyah)
            }
        }
        if (bookmarks.isNotEmpty()) {
            item { SectionTitle(strings.savedAyahs, Modifier.padding(horizontal = 0.dp)) }
            items(bookmarks, key = { "${it.surah}_${it.ayah}" }) { key ->
                Card(onClick = { onOpenAyah(key) }, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        strings.surahAyah(key.surah, key.ayah),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }
    }
}
