package app.bibliya.android.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import app.bibliya.android.ui.LocalDeps
import app.bibliya.android.ui.LocalStrings
import app.bibliya.android.ui.LocalTranslation
import app.bibliya.android.ui.LocalUiLanguage
import app.bibliya.android.ui.VerseSearchState
import app.bibliya.android.ui.components.EmptyState
import app.bibliya.android.ui.components.PassageCard
import app.bibliya.android.ui.components.SectionTitle
import app.bibliya.android.ui.components.refLabel
import app.bibliya.android.ui.components.scriptureStyle
import app.bibliya.shared.model.VerseRef
import app.bibliya.shared.model.pick
import app.bibliya.shared.search.SearchResults
import app.bibliya.shared.search.TextNormalizer

@Composable
fun SearchScreen(
    query: String,
    results: SearchResults?,
    reference: VerseRef?,
    verses: VerseSearchState,
    favorites: Set<String>,
    onQueryChange: (String) -> Unit,
    onOpenSituation: (String) -> Unit,
    onOpenRef: (VerseRef) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onBack: () -> Unit,
) {
    val strings = LocalStrings.current
    val deps = LocalDeps.current
    val lang = LocalTranslation.current
    val uiLang = LocalUiLanguage.current
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }

    Scaffold(
        topBar = {
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(start = 4.dp, end = 12.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back) }
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = { Text(strings.searchHint, maxLines = 1) },
                    singleLine = true,
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) { Icon(Icons.Filled.Clear, contentDescription = null) }
                        }
                    },
                    modifier = Modifier.weight(1f).focusRequester(focus),
                )
            }
        },
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(padding),
        ) {
            if (reference != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable { onOpenRef(reference) },
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null)
                            Spacer(Modifier.size(12.dp))
                            Text("${strings.goToReference}: ${deps.content.refLabel(reference, lang, longName = true)}", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
            if (query.isBlank()) {
                item { EmptyState(strings.searchHint) }
                return@LazyColumn
            }
            val r = results
            if (r != null && r.situations.isNotEmpty()) {
                item { SectionTitle(strings.foundSituations) }
                items(r.situations.take(4), key = { "s-" + it.id }) { s ->
                    ListItem(
                        headlineContent = { Text("${s.emoji} ${s.title.pick(uiLang)}") },
                        supportingContent = { Text(s.subtitle.pick(uiLang)) },
                        modifier = Modifier.clickable { onOpenSituation(s.id) },
                    )
                }
            }
            if (r != null && r.passages.isNotEmpty()) {
                item { SectionTitle(strings.foundPassages) }
                items(r.passages.take(12), key = { "p-" + it.id }) { p ->
                    PassageCard(
                        p,
                        favorite = p.id in favorites,
                        onToggleFavorite = { onToggleFavorite(p.id) },
                        onOpen = onOpenRef,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
            if (verses.terms.isNotEmpty()) {
                item { SectionTitle(strings.foundVerses) }
                if (verses.loading) {
                    item { LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) }
                }
                items(verses.verses, key = { "v-${it.book}-${it.chapter}-${it.number}" }) { v ->
                    val label = deps.content.refLabel(VerseRef(v.book, v.chapter, v.number), lang)
                    val highlight = MaterialTheme.colorScheme.primary
                    Column(
                        Modifier.fillMaxWidth().clickable { onOpenRef(VerseRef(v.book, v.chapter, v.number)) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Text(
                            buildAnnotatedString {
                                // Bold the words that matched, so the eye finds them at once.
                                v.text.split(' ').forEachIndexed { i, word ->
                                    if (i > 0) append(' ')
                                    val norm = TextNormalizer.normalize(word)
                                    if (verses.terms.any { norm.startsWith(it) }) {
                                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = highlight)) { append(word) }
                                    } else {
                                        append(word)
                                    }
                                }
                            },
                            style = scriptureStyle(0.9f),
                        )
                    }
                }
            }
            if (r != null && r.isEmpty && verses.verses.isEmpty() && !verses.loading && reference == null) {
                item { EmptyState(strings.nothingFound) }
            }
        }
    }
}
