package app.qurandua.android.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.qurandua.android.ui.LocalDeps
import app.qurandua.android.ui.LocalSettings
import app.qurandua.android.ui.LocalStrings
import app.qurandua.shared.model.Grade

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackTopBar(title: String, onBack: (() -> Unit)?) {
    TopAppBar(
        title = { Text(title, maxLines = 1) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = LocalStrings.current.back)
                }
            }
        },
    )
}

/** Arabic (Quran or dua) text: always right-to-left, Amiri Quran font, user-chosen size. */
@Composable
fun ArabicText(text: String, modifier: Modifier = Modifier, scale: Float = 1f) {
    val size = LocalSettings.current.arabicFontSize * scale
    Text(
        text = text,
        modifier = modifier.fillMaxWidth(),
        style = TextStyle(
            fontFamily = LocalDeps.current.arabicFont,
            fontSize = size.sp,
            lineHeight = (size * 1.9f).sp,
            textDirection = TextDirection.Rtl,
            textAlign = TextAlign.Right,
            color = MaterialTheme.colorScheme.onSurface,
        ),
    )
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
fun gradeColors(grade: Grade): Pair<Color, Color> {
    val c = MaterialTheme.colorScheme
    return when (grade) {
        Grade.QURAN, Grade.SAHIH -> c.primaryContainer to c.onPrimaryContainer
        Grade.HASAN -> c.secondaryContainer to c.onSecondaryContainer
        Grade.ATHAR -> c.surfaceVariant to c.onSurfaceVariant
        Grade.DAIF, Grade.DISPUTED, Grade.NOSOURCE -> c.errorContainer to c.onErrorContainer
    }
}

@Composable
fun GradeBadge(grade: Grade) {
    val (bg, fg) = gradeColors(grade)
    Surface(color = bg, contentColor = fg, shape = RoundedCornerShape(50)) {
        Text(
            text = LocalStrings.current.gradeLabel(grade),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

/** Disclaimers and scholarly notes shown above a list. */
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

/**
 * References stay one tap away instead of on the main view: a small "i" that opens the text.
 * Used for hadith sources, lesson evidence and licences, so a reader can still check them.
 */
@Composable
fun InfoButton(open: Boolean, label: String, onToggle: () -> Unit) {
    IconButton(onClick = onToggle, modifier = Modifier.size(36.dp)) {
        Icon(
            if (open) Icons.Filled.Info else Icons.Outlined.Info,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (open) 1f else 0.6f),
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
fun HiddenText(open: Boolean, text: String) {
    AnimatedVisibility(visible = open) {
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** An "i" button with its text under it, for places where the two can sit together. */
@Composable
fun MoreInfo(text: String, label: String, modifier: Modifier = Modifier) {
    var open by rememberSaveable { mutableStateOf(false) }
    Column(modifier) {
        InfoButton(open, label) { open = !open }
        HiddenText(open, text)
    }
}
