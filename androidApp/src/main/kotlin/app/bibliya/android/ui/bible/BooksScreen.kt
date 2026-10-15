package app.bibliya.android.ui.bible

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.bibliya.android.ui.LocalSettings
import app.bibliya.android.ui.LocalStrings
import app.bibliya.android.ui.components.BackTopBar
import app.bibliya.android.ui.components.NoticeCard
import app.bibliya.shared.model.Book

/** The Bible tab: books of the Old and New Testament in the translation's own order. */
@Composable
fun BooksScreen(books: List<Book>, lang: String, onOpenBook: (String) -> Unit) {
    val strings = LocalStrings.current
    var testament by rememberSaveable { mutableIntStateOf(0) }
    val shown = books.filter { it.isNewTestament == (testament == 1) }
    Column(Modifier.fillMaxSize()) {
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            listOf(strings.oldTestament, strings.newTestament).forEachIndexed { i, label ->
                SegmentedButton(
                    selected = testament == i,
                    onClick = { testament = i },
                    shape = SegmentedButtonDefaults.itemShape(i, 2),
                ) { Text(label) }
            }
        }
        LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
            if (testament == 0 && shown.any { it.dc }) {
                item { NoticeCard(strings.deuterocanonicalNote(LocalSettings.current.tradition), Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) }
            }
            items(shown, key = { it.id }) { book ->
                Row(
                    Modifier.fillMaxWidth().clickable { onOpenBook(book.id) }.padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(book.name(lang), style = MaterialTheme.typography.bodyLarge)
                        if (book.dc) {
                            Text(strings.deuterocanonical(LocalSettings.current.tradition), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                    Text(
                        book.chapterCount(lang).toString(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                HorizontalDivider(Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.surfaceVariant)
            }
        }
    }
}

/** A grid of chapter numbers for one book. */
@Composable
fun ChaptersScreen(book: Book, lang: String, onOpenChapter: (Int) -> Unit, onBack: () -> Unit) {
    val strings = LocalStrings.current
    Scaffold(topBar = { BackTopBar(book.name(lang), onBack) }) { padding ->
        val count = book.chapterCount(lang)
        if (count == 0) {
            Box(Modifier.padding(padding).padding(16.dp)) { NoticeCard(strings.notInTranslation) }
            return@Scaffold
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(64.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(padding),
        ) {
            items((1..count).toList()) { n ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.aspectRatio(1f).clickable { onOpenChapter(n) },
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(n.toString(), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}
