package app.bibliya.android.ui.saved

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.bibliya.android.ui.LocalDeps
import app.bibliya.android.ui.LocalStrings
import app.bibliya.android.ui.components.EmptyState
import app.bibliya.android.ui.components.PassageCard
import app.bibliya.android.ui.components.SectionTitle
import app.bibliya.android.ui.components.refLabel
import app.bibliya.android.ui.components.scriptureStyle
import app.bibliya.shared.model.Bookmark
import app.bibliya.shared.model.Passage
import app.bibliya.shared.model.VerseRef

@Composable
fun SavedScreen(
    passages: List<Passage>,
    bookmarks: List<Bookmark>,
    favorites: Set<String>,
    onToggleFavorite: (String) -> Unit,
    onOpenRef: (VerseRef) -> Unit,
    onOpenBookmark: (Bookmark) -> Unit,
) {
    val strings = LocalStrings.current
    val deps = LocalDeps.current
    if (passages.isEmpty() && bookmarks.isEmpty()) {
        EmptyState(strings.savedEmpty)
        return
    }
    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (bookmarks.isNotEmpty()) {
            item { SectionTitle(strings.savedVerses) }
            items(bookmarks, key = { "b-${it.lang}-${it.book}-${it.chapter}-${it.verse}" }) { b ->
                Column(Modifier.fillMaxWidth().clickable { onOpenBookmark(b) }.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        deps.content.refLabel(VerseRef(b.book, b.chapter, b.verse), b.lang),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    b.text?.let { Text(it, style = scriptureStyle(0.9f)) }
                }
            }
        }
        if (passages.isNotEmpty()) {
            item { SectionTitle(strings.savedPassages) }
            items(passages, key = { "p-" + it.id }) { p ->
                PassageCard(
                    p,
                    favorite = p.id in favorites,
                    onToggleFavorite = { onToggleFavorite(p.id) },
                    onOpen = onOpenRef,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}
