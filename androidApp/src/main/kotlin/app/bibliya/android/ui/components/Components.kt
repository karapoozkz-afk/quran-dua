package app.bibliya.android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bibliya.android.ui.LocalDeps
import app.bibliya.android.ui.LocalSettings
import app.bibliya.android.ui.LocalStrings
import app.bibliya.android.ui.LocalTranslation
import app.bibliya.shared.data.ContentRepository
import app.bibliya.shared.model.Passage
import app.bibliya.shared.model.VerseRef
import app.bibliya.shared.model.pick

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackTopBar(title: String, onBack: (() -> Unit)?, actions: @Composable () -> Unit = {}) {
    TopAppBar(
        title = { Text(title, maxLines = 1) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = LocalStrings.current.back)
                }
            }
        },
        actions = { actions() },
    )
}

/** Scripture is set in a serif face at the reader's chosen size. */
@Composable
fun scriptureStyle(scale: Float = 1f): TextStyle {
    val size = LocalSettings.current.fontSize * scale
    return TextStyle(
        fontFamily = FontFamily.Serif,
        fontSize = size.sp,
        lineHeight = (size * 1.55f).sp,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

/** Verses run together as one paragraph, each led by a small raised number. */
@Composable
fun VersesText(verses: List<Pair<Int, String>>, modifier: Modifier = Modifier, scale: Float = 1f, numbers: Boolean = true) {
    val numberColor = MaterialTheme.colorScheme.primary
    Text(
        text = buildAnnotatedString {
            verses.forEachIndexed { i, (n, text) ->
                if (numbers && verses.size > 1) {
                    withStyle(SpanStyle(color = numberColor, fontSize = 11.sp, baselineShift = BaselineShift(0.3f), fontWeight = FontWeight.SemiBold)) {
                        append("$n ")
                    }
                }
                append(text)
                if (i != verses.lastIndex) append(" ")
            }
        },
        style = scriptureStyle(scale),
        modifier = modifier,
    )
}

/** "Ин 3:16" or "Пс 22:1–6" in the language of the translation. */
fun ContentRepository.refLabel(ref: VerseRef, lang: String, longName: Boolean = false): String {
    val book = book(ref.book)
    val name = if (longName) book?.name(lang) else book?.shortName(lang)
    val verses = if (ref.verseEnd != ref.verse) "${ref.verse}–${ref.verseEnd}" else "${ref.verse}"
    return "${name ?: ref.book} ${ref.chapter}:$verses"
}

/** Text to share or copy: the verses, then the reference and the translation. */
fun shareText(text: String, reference: String, lang: String): String {
    val translation = if (lang == "en") "KJV" else "Синодальный перевод"
    return "$text\n— $reference ($translation)"
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
fun NoticeCard(text: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
    ) {
        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Filled.Info, contentDescription = null)
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun EmptyState(text: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/** A passage for a situation: title, reference, text and actions. */
@Composable
fun PassageCard(
    passage: Passage,
    favorite: Boolean,
    onToggleFavorite: () -> Unit,
    onOpen: (VerseRef) -> Unit,
    modifier: Modifier = Modifier,
) {
    val deps = LocalDeps.current
    val strings = LocalStrings.current
    val lang = LocalTranslation.current
    val uiLang = app.bibliya.android.ui.LocalUiLanguage.current
    val ref = passage.refFor(lang)
    val verses = passage.versesFor(lang)
    val label = ref?.let { deps.content.refLabel(it, lang) }.orEmpty()
    val plain = verses.joinToString(" ") { it.second }
    Card(modifier = modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(start = 16.dp, end = 8.dp, top = 14.dp, bottom = 4.dp)) {
            Text(passage.title.pick(uiLang), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(end = 8.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            VersesText(verses, Modifier.padding(top = 8.dp, end = 8.dp), scale = 0.95f)
            passage.note?.let {
                Text(
                    it.pick(uiLang),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, end = 8.dp),
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        if (favorite) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = if (favorite) strings.unfavorite else strings.favorite,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                IconButton(onClick = { deps.platform.copy(shareText(plain, label, lang), strings.copied) }) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = strings.copy, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = { deps.platform.share(shareText(plain, label, lang)) }) {
                    Icon(Icons.Filled.Share, contentDescription = strings.share, modifier = Modifier.size(20.dp))
                }
                if (ref != null) {
                    IconButton(onClick = { onOpen(ref) }) {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = strings.openInBible)
                    }
                }
            }
        }
    }
}
