package app.qurandua.android.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import app.qurandua.android.ui.LocalContentLang
import app.qurandua.android.ui.LocalDeps
import app.qurandua.android.ui.LocalSettings
import app.qurandua.android.ui.LocalStrings
import app.qurandua.android.ui.ayahAudioUrl
import app.qurandua.shared.model.AyahKey
import app.qurandua.shared.model.DuaItem
import app.qurandua.shared.model.ItemKind
import app.qurandua.shared.model.pick
import app.qurandua.shared.model.translitLanguageFor
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.launch

/**
 * One dua or guidance card: title, grade badge, Arabic, transliteration, translation,
 * the source, and the actions. The grade and the source are never optional — the card
 * exists so the user can check where the text comes from.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DuaCard(
    item: DuaItem,
    isFavorite: Boolean,
    onOpenInQuran: ((AyahKey) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    val lang = LocalContentLang.current
    val settings = LocalSettings.current
    val deps = LocalDeps.current
    val scope = rememberCoroutineScope()
    val playing by deps.audio.playing.collectAsState()
    val isPlaying = playing == item.id
    val arabicVoice by deps.audio.arabicVoice.collectAsState()

    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.title.pick(lang),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { scope.launch { deps.user.toggleFavorite(item.id) } }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (isFavorite) strings.removeFavorite else strings.addFavorite,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GradeBadge(item.grade)
                item.repeat?.let { Text(strings.repeatTimes(it), style = MaterialTheme.typography.labelLarge) }
            }

            item.arabic?.takeIf { it.isNotBlank() }?.let { ArabicText(it) }

            if (settings.showTransliteration) {
                item.translit?.pick(translitLanguageFor(settings.translitScript, lang))?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic)
                }
            }

            if (settings.showTranslation || item.kind == ItemKind.INFO) {
                Text(item.translation.pick(lang), style = MaterialTheme.typography.bodyLarge)
            }

            item.note?.pick(lang)?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            HorizontalDivider()

            Text(
                text = "${strings.source}: ${item.source.pick(lang)}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AnimatedVisibility(visible = item.grade.ordinal >= 3) {
                Text(
                    text = strings.gradeExplain(item.grade),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            // Ayahs play the reciter's recording; other duas fall back to the phone's Arabic voice.
            val audioKeys = item.quranAyahs.ifEmpty { listOfNotNull(item.quranRef) }
            val spoken = item.arabic?.takeIf { it.isNotBlank() && audioKeys.isEmpty() }
            if (spoken != null && isPlaying) {
                if (arabicVoice == false) {
                    Text(strings.noArabicVoice, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = { deps.audio.openVoiceSettings() }) { Text(strings.voiceSettings) }
                } else {
                    Text(strings.ttsNote, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (audioKeys.isNotEmpty() || spoken != null) {
                    TextButton(onClick = {
                        when {
                            isPlaying -> deps.audio.stop()
                            audioKeys.isNotEmpty() -> deps.audio.play(item.id, audioKeys.map { ayahAudioUrl(it.surah, it.ayah) })
                            spoken != null -> deps.audio.speak(item.id, spoken)
                        }
                    }) {
                        Icon(if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow, contentDescription = null)
                        Text(if (isPlaying) strings.stop else strings.listen, Modifier.padding(start = 6.dp))
                    }
                }
                val target = item.quranRef ?: item.quranLink
                if (target != null && onOpenInQuran != null) {
                    TextButton(onClick = { onOpenInQuran(target) }) {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null)
                        Text(strings.openInQuran, Modifier.padding(start = 6.dp))
                    }
                }
                TextButton(onClick = { deps.platform.copy(item.shareText(lang, strings.source)) }) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = null)
                    Text(strings.copy, Modifier.padding(start = 6.dp))
                }
                TextButton(onClick = { deps.platform.share(item.shareText(lang, strings.source)) }) {
                    Icon(Icons.Filled.Share, contentDescription = null)
                    Text(strings.share, Modifier.padding(start = 6.dp))
                }
            }
        }
    }
}

/** Shared text always carries the source, so a screenshot cannot drop it. */
private fun DuaItem.shareText(lang: String, sourceLabel: String): String = buildString {
    appendLine(title.pick(lang))
    arabic?.takeIf { it.isNotBlank() }?.let { appendLine(); appendLine(it) }
    translit?.pick(lang)?.takeIf { it.isNotBlank() }?.let { appendLine(); appendLine(it) }
    appendLine()
    appendLine(translation.pick(lang))
    appendLine()
    append("$sourceLabel: ${source.pick(lang)}")
}
