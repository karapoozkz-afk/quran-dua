package app.qurandua.android.ui.quran

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.qurandua.android.ui.LocalDeps
import app.qurandua.android.ui.LocalSettings
import app.qurandua.android.ui.LocalStrings
import app.qurandua.android.ui.ayahAudioUrl
import app.qurandua.android.ui.components.ArabicText
import app.qurandua.android.ui.components.BackTopBar
import app.qurandua.android.ui.components.EmptyState
import app.qurandua.shared.model.Ayah
import app.qurandua.shared.model.AyahKey
import app.qurandua.shared.model.SurahInfo
import androidx.compose.ui.text.font.FontStyle
import kotlinx.coroutines.flow.drop

@Composable
fun SurahListScreen(
    surahs: List<SurahInfo>,
    quranReady: Boolean,
    onOpenSurah: (Int) -> Unit,
    lastRead: AyahKey? = null,
    onContinueReading: (AyahKey) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    if (!quranReady && surahs.isEmpty()) {
        Column(
            modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator()
            Text(strings.loadingQuran, textAlign = TextAlign.Center)
        }
        return
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        if (lastRead != null) {
            item(key = "continue") {
                Card(
                    onClick = { onContinueReading(lastRead) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Bookmark, contentDescription = null)
                        Column(Modifier.padding(start = 12.dp)) {
                            Text(strings.continueReading, style = MaterialTheme.typography.titleSmall)
                            val name = surahs.firstOrNull { it.number == lastRead.surah }?.transliteration
                            Text(
                                listOfNotNull(name, strings.surahAyah(lastRead.surah, lastRead.ayah)).joinToString(" · "),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
        }
        items(surahs, key = { it.number }) { surah ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { onOpenSurah(surah.number) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(surah.number.toString(), style = MaterialTheme.typography.labelLarge)
                    }
                }
                Column(Modifier.weight(1f)) {
                    Text(surah.transliteration, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${surah.translatedName} · ${strings.ayahsCount(surah.ayahCount)} · " +
                            if (surah.revelation == "meccan") strings.meccan else strings.medinan,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(surah.nameArabic, style = MaterialTheme.typography.titleMedium)
            }
            HorizontalDivider()
        }
    }
}

/** Read one surah: Arabic, transliteration and translation per ayah, with audio. */
@Composable
fun SurahReaderScreen(
    surahNumber: Int,
    initialAyah: Int?,
    loadSurah: suspend (Int) -> SurahInfo?,
    loadAyahs: suspend (Int) -> List<Ayah>,
    bookmarks: List<AyahKey>,
    onToggleBookmark: (AyahKey) -> Unit,
    onLastRead: (AyahKey) -> Unit,
    onBack: () -> Unit,
) {
    val strings = LocalStrings.current
    val settings = LocalSettings.current
    val deps = LocalDeps.current
    val playing by deps.audio.playing.collectAsState()
    var info by remember(surahNumber) { mutableStateOf<SurahInfo?>(null) }
    var ayahs by remember(surahNumber) { mutableStateOf<List<Ayah>>(emptyList()) }
    val listState = rememberLazyListState()

    LaunchedEffect(surahNumber) {
        info = loadSurah(surahNumber)
        ayahs = loadAyahs(surahNumber)
        val target = initialAyah?.takeIf { it > 1 }
        // Item 0 is the "listen to the whole surah" row, so ayah n sits at index n.
        if (target != null && target <= ayahs.size) listState.scrollToItem(target)
    }

    LaunchedEffect(surahNumber, listState) {
        snapshotIndex(listState) { index ->
            ayahs.getOrNull((index - 1).coerceAtLeast(0))?.let { onLastRead(it.key) }
        }
    }

    // While the whole surah plays, highlight the ayah being recited and keep it on screen.
    val surahKey = "surah_$surahNumber"
    val audioIndex by deps.audio.index.collectAsState()
    val recitingAyah = if (playing == surahKey) audioIndex + 1 else null
    LaunchedEffect(recitingAyah) {
        recitingAyah?.let { listState.animateScrollToItem(it) }
    }

    Scaffold(topBar = { BackTopBar(info?.transliteration ?: strings.navQuran, onBack) }) { padding ->
        if (ayahs.isEmpty()) {
            EmptyState(strings.loadingQuran, Modifier.padding(padding))
            return@Scaffold
        }
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "listen") {
                FilledTonalButton(
                    onClick = {
                        if (playing == surahKey) deps.audio.stop()
                        else deps.audio.play(surahKey, ayahs.map { ayahAudioUrl(it.surah, it.number) })
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(if (playing == surahKey) Icons.Filled.Stop else Icons.Filled.PlayArrow, contentDescription = null)
                    Text(if (playing == surahKey) strings.stop else strings.listenSurah, Modifier.padding(start = 8.dp))
                }
            }
            items(ayahs, key = { it.number }) { ayah ->
                val key = ayah.key
                val audioKey = "ayah_${ayah.surah}_${ayah.number}"
                val bookmarked = key in bookmarks
                val reciting = recitingAyah == ayah.number
                Card(
                    Modifier.fillMaxWidth(),
                    colors = if (reciting) {
                        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    } else {
                        CardDefaults.cardColors()
                    },
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "${ayah.surah}:${ayah.number}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(onClick = {
                                if (playing == audioKey) deps.audio.stop()
                                else deps.audio.play(audioKey, listOf(ayahAudioUrl(ayah.surah, ayah.number)))
                            }) {
                                Icon(
                                    if (playing == audioKey) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                    contentDescription = if (playing == audioKey) strings.stop else strings.listen,
                                )
                            }
                            IconButton(onClick = { onToggleBookmark(key) }) {
                                Icon(
                                    if (bookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                    contentDescription = strings.bookmark,
                                )
                            }
                        }
                        ArabicText(ayah.arabic)
                        if (settings.showTransliteration) {
                            Text(ayah.transliteration, style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic)
                        }
                        if (settings.showTranslation) {
                            ayah.translation?.let { Text(it, style = MaterialTheme.typography.bodyLarge) }
                        }
                    }
                }
            }
        }
    }
}

/** Remembers the top visible ayah so "continue reading" survives app restarts. */
private suspend fun snapshotIndex(
    state: androidx.compose.foundation.lazy.LazyListState,
    onIndex: (Int) -> Unit,
) {
    // The first value is where the surah opened; only the reader's own scrolling moves the mark,
    // so peeking into another surah does not lose the place.
    androidx.compose.runtime.snapshotFlow { state.firstVisibleItemIndex }
        .drop(1)
        .collect { onIndex(it) }
}
