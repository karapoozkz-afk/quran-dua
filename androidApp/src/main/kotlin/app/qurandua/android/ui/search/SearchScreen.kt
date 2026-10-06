package app.qurandua.android.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import app.qurandua.android.ui.LocalContentLang
import app.qurandua.android.ui.LocalStrings
import app.qurandua.android.ui.QuranSearchState
import app.qurandua.android.ui.components.ArabicText
import app.qurandua.android.ui.components.BackTopBar
import app.qurandua.android.ui.components.DuaCard
import app.qurandua.android.ui.components.EmptyState
import app.qurandua.android.ui.components.SectionTitle
import app.qurandua.shared.model.AyahKey
import app.qurandua.shared.model.Situation
import app.qurandua.shared.model.pick
import app.qurandua.shared.search.SearchResults

/**
 * One field, natural language. Results come back in the order the user needs them:
 * matching situations, then duas, then ayahs of the Quran.
 */
@Composable
fun SearchScreen(
    query: String,
    results: SearchResults?,
    quranResults: QuranSearchState,
    favorites: Set<String>,
    onQueryChange: (String) -> Unit,
    onOpenSituation: (String) -> Unit,
    onOpenAyah: (AyahKey) -> Unit,
    onBack: () -> Unit,
) {
    val strings = LocalStrings.current
    val lang = LocalContentLang.current

    Scaffold(topBar = { BackTopBar(strings.whatToRead, onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text(strings.searchHint) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = strings.back)
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Search),
            )

            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (results == null) return@LazyColumn

                if (results.isEmpty && quranResults.ayahs.isEmpty() && !quranResults.loading) {
                    item { EmptyState(strings.noResults) }
                }

                if (results.situations.isNotEmpty()) {
                    item { SectionTitle(strings.resultsSituations, Modifier.padding(horizontal = 0.dp)) }
                    items(results.situations, key = { "s_${it.id}" }) { situation ->
                        SituationRow(situation, lang, onOpenSituation)
                    }
                }

                if (results.duas.isNotEmpty()) {
                    item { SectionTitle(strings.resultsDuas, Modifier.padding(horizontal = 0.dp)) }
                    items(results.duas, key = { "d_${it.id}" }) { dua ->
                        DuaCard(item = dua, isFavorite = dua.id in favorites, onOpenInQuran = onOpenAyah)
                    }
                }

                if (quranResults.loading) {
                    item { CircularProgressIndicator(Modifier.padding(16.dp)) }
                } else if (quranResults.ayahs.isNotEmpty()) {
                    item { SectionTitle(strings.resultsAyahs, Modifier.padding(horizontal = 0.dp)) }
                    items(quranResults.ayahs, key = { "a_${it.surah}_${it.number}" }) { ayah ->
                        Card(onClick = { onOpenAyah(ayah.key) }, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    strings.surahAyah(ayah.surah, ayah.number),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                ArabicText(ayah.arabic, scale = 0.85f)
                                ayah.translation?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SituationRow(situation: Situation, lang: String, onOpen: (String) -> Unit) {
    Card(onClick = { onOpen(situation.id) }, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("${situation.emoji} ${situation.title.pick(lang)}", style = MaterialTheme.typography.titleMedium)
            Text(
                situation.subtitle.pick(lang),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
