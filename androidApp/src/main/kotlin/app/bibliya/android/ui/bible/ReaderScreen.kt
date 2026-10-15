package app.bibliya.android.ui.bible

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bibliya.android.ui.LocalDeps
import app.bibliya.android.ui.LocalSettings
import app.bibliya.android.ui.LocalStrings
import app.bibliya.android.ui.components.BackTopBar
import app.bibliya.android.ui.components.NoticeCard
import app.bibliya.android.ui.components.scriptureStyle
import app.bibliya.android.ui.components.shareText
import app.bibliya.shared.data.TRANSLATIONS
import app.bibliya.shared.model.Book
import app.bibliya.shared.model.Bookmark
import app.bibliya.shared.model.ReadingPosition
import app.bibliya.shared.model.Verse

/** Where to go from a chapter: the previous and next chapter, crossing into neighbouring books. */
data class ChapterNav(val book: String, val chapter: Int)

fun neighbours(books: List<Book>, lang: String, book: String, chapter: Int): Pair<ChapterNav?, ChapterNav?> {
    val index = books.indexOfFirst { it.id == book }
    if (index < 0) return null to null
    val count = books[index].chapterCount(lang)
    val prev = when {
        chapter > 1 -> ChapterNav(book, chapter - 1)
        index > 0 -> books[index - 1].let { ChapterNav(it.id, it.chapterCount(lang)) }
        else -> null
    }
    val next = when {
        chapter < count -> ChapterNav(book, chapter + 1)
        index < books.lastIndex -> ChapterNav(books[index + 1].id, 1)
        else -> null
    }
    return prev to next
}

@Composable
fun ReaderScreen(
    book: Book,
    chapter: Int,
    /** Verses to show highlighted and scroll to, e.g. from a passage or the verse of the day. */
    focus: IntRange?,
    lang: String,
    books: List<Book>,
    ready: Boolean,
    bookmarks: List<Bookmark>,
    loadChapter: suspend (lang: String, book: String, chapter: Int) -> List<Verse>,
    onToggleBookmark: (Verse) -> Unit,
    onLastRead: (ReadingPosition) -> Unit,
    onNavigate: (ChapterNav) -> Unit,
    onTranslation: (String) -> Unit,
    onFontSize: (Int) -> Unit,
    onBack: () -> Unit,
) {
    val strings = LocalStrings.current
    val deps = LocalDeps.current
    val settings = LocalSettings.current
    var verses by remember(book.id, chapter, lang) { mutableStateOf<List<Verse>?>(null) }
    val selected = remember(book.id, chapter, lang) { mutableStateListOf<Int>() }
    val listState = rememberLazyListState()
    val speechKey = "${lang}|${book.id}|$chapter"
    val speaking by deps.speech.speaking.collectAsState()
    val speakingVerse by deps.speech.verse.collectAsState()
    val missingVoice by deps.speech.missingVoice.collectAsState()
    val isSpeaking = speaking == speechKey
    var fontMenu by remember { mutableStateOf(false) }
    var translationMenu by remember { mutableStateOf(false) }

    LaunchedEffect(book.id, chapter, lang, ready) {
        if (!ready) return@LaunchedEffect
        val loaded = loadChapter(lang, book.id, chapter)
        verses = loaded
        val start = focus?.first ?: 1
        onLastRead(ReadingPosition(lang, book.id, chapter, start))
        val index = loaded.indexOfFirst { it.number >= start }.coerceAtLeast(0)
        if (index > 0) listState.scrollToItem(index)
    }
    // Follow the voice through the chapter.
    LaunchedEffect(speakingVerse, isSpeaking) {
        val v = speakingVerse ?: return@LaunchedEffect
        if (!isSpeaking) return@LaunchedEffect
        val index = verses?.indexOfFirst { it.number == v } ?: -1
        if (index >= 0) listState.animateScrollToItem(index)
    }

    val bookmarked = bookmarks.filter { it.lang == lang && it.book == book.id && it.chapter == chapter }.map { it.verse }.toSet()
    val (prev, next) = neighbours(books, lang, book.id, chapter)

    Scaffold(
        topBar = {
            BackTopBar("${book.shortName(lang)} $chapter", onBack) {
                IconButton(onClick = {
                    if (isSpeaking) deps.speech.stop()
                    else verses?.let { list -> deps.speech.speak(speechKey, lang, list.map { it.number to it.text }) }
                }) {
                    Icon(
                        if (isSpeaking) Icons.Filled.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = if (isSpeaking) strings.stopListening else strings.listen,
                    )
                }
                Box {
                    IconButton(onClick = { translationMenu = true }) { Icon(Icons.Filled.Translate, contentDescription = strings.translation) }
                    DropdownMenu(expanded = translationMenu, onDismissRequest = { translationMenu = false }) {
                        TRANSLATIONS.forEach { code ->
                            DropdownMenuItem(
                                text = { Text(if (code == "ru") "Синодальный перевод" else "King James Version") },
                                onClick = {
                                    translationMenu = false
                                    deps.speech.stop()
                                    onTranslation(code)
                                },
                                leadingIcon = if (code == lang) ({ Text("✓") }) else null,
                            )
                        }
                    }
                }
                Box {
                    IconButton(onClick = { fontMenu = true }) { Icon(Icons.Filled.FormatSize, contentDescription = strings.textSize) }
                    DropdownMenu(expanded = fontMenu, onDismissRequest = { fontMenu = false }) {
                        Column(Modifier.padding(horizontal = 16.dp).width(240.dp)) {
                            Text("${strings.textSize}: ${settings.fontSize}", style = MaterialTheme.typography.labelLarge)
                            Slider(
                                value = settings.fontSize.toFloat(),
                                onValueChange = { onFontSize(it.toInt()) },
                                valueRange = 14f..32f,
                                steps = 17,
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (selected.isNotEmpty()) {
                val chosen = verses.orEmpty().filter { it.number in selected }.sortedBy { it.number }
                val numbers = chosen.map { it.number }
                val range = if (numbers.size > 1 && numbers.last() - numbers.first() == numbers.size - 1) {
                    "${numbers.first()}–${numbers.last()}"
                } else {
                    numbers.joinToString(",")
                }
                val label = "${book.shortName(lang)} $chapter:$range"
                val text = shareText(chosen.joinToString(" ") { it.text }, label, lang)
                val allBookmarked = chosen.all { it.number in bookmarked }
                BottomAppBar {
                    Text(label, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(start = 16.dp).weight(1f))
                    IconButton(onClick = {
                        chosen.filter { (it.number in bookmarked) == allBookmarked }.forEach(onToggleBookmark)
                        selected.clear()
                    }) {
                        Icon(
                            if (allBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = if (allBookmarked) strings.removeBookmark else strings.bookmark,
                        )
                    }
                    IconButton(onClick = {
                        deps.platform.copy(text, strings.copied)
                        selected.clear()
                    }) { Icon(Icons.Filled.ContentCopy, contentDescription = strings.copy) }
                    IconButton(onClick = {
                        deps.platform.share(text)
                        selected.clear()
                    }) { Icon(Icons.Filled.Share, contentDescription = strings.share) }
                }
            }
        },
    ) { padding ->
        val list = verses
        when {
            book.chapterCount(lang) == 0 -> Box(Modifier.padding(padding).padding(16.dp)) { NoticeCard(strings.notInTranslation) }
            !ready || list == null -> Column(
                Modifier.fillMaxSize().padding(padding).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
                Spacer(Modifier.size(16.dp))
                Text(strings.loadingBible, style = MaterialTheme.typography.bodyMedium)
            }
            else -> LazyColumn(
                state = listState,
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 24.dp),
                modifier = Modifier.padding(padding),
            ) {
                if (isSpeaking && missingVoice) {
                    item {
                        Column(Modifier.padding(4.dp)) {
                            NoticeCard(strings.noVoice)
                            TextButton(onClick = deps.speech::openVoiceSettings) { Text(strings.voiceSettings) }
                        }
                    }
                }
                items(list, key = { it.number }) { verse ->
                    val highlighted = verse.number in selected ||
                        (isSpeaking && verse.number == speakingVerse) ||
                        (focus != null && verse.number in focus && selected.isEmpty() && !isSpeaking)
                    val numberColor = MaterialTheme.colorScheme.primary
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = numberColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)) {
                                append("${verse.number}  ")
                            }
                            append(verse.text)
                            if (verse.number in bookmarked) append("  🔖")
                        },
                        style = scriptureStyle(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (highlighted) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
                                RoundedCornerShape(8.dp),
                            )
                            .clickable {
                                if (verse.number in selected) selected.remove(verse.number) else selected.add(verse.number)
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                    )
                }
                item {
                    Row(
                        Modifier.fillMaxWidth().padding(top = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        if (prev != null) {
                            OutlinedButton(onClick = { deps.speech.stop(); onNavigate(prev) }) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null)
                                Text(navLabel(books, lang, prev))
                            }
                        } else {
                            Spacer(Modifier.size(1.dp))
                        }
                        if (next != null) {
                            OutlinedButton(onClick = { deps.speech.stop(); onNavigate(next) }) {
                                Text(navLabel(books, lang, next))
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun navLabel(books: List<Book>, lang: String, nav: ChapterNav): String =
    "${books.firstOrNull { it.id == nav.book }?.shortName(lang) ?: nav.book} ${nav.chapter}"
