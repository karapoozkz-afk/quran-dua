package app.qurandua.android.ui.learn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import app.qurandua.android.R
import app.qurandua.android.ui.components.BackTopBar
import app.qurandua.android.ui.components.HiddenText
import app.qurandua.android.ui.components.InfoButton
import app.qurandua.android.ui.components.NoticeCard
import app.qurandua.shared.learn.Lesson
import app.qurandua.shared.learn.LessonGrade
import app.qurandua.shared.learn.inLanguage
import app.qurandua.shared.learn.label
import app.qurandua.shared.learn.learnStringsFor
import app.qurandua.shared.model.AyahKey

@Composable
fun LearnListScreen(lessons: List<Lesson>, lang: String, onOpen: (String) -> Unit, onBack: () -> Unit) {
    val s = learnStringsFor(lang)
    Scaffold(topBar = { BackTopBar(s.title, onBack) }) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item { NoticeCard(s.note) }
            itemsIndexed(lessons, key = { _, l -> l.id }) { i, lesson ->
                Card(onClick = { onOpen(lesson.id) }, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        StepNumber(i + 1)
                        Column {
                            Text(lesson.title.inLanguage(lang), style = MaterialTheme.typography.titleMedium)
                            if (lang != "ru") lesson.title["ru"]?.takeIf { it != lesson.title.inLanguage(lang) }?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LessonScreen(lesson: Lesson, lang: String, onOpenAyah: (AyahKey) -> Unit, onBack: () -> Unit) {
    val s = learnStringsFor(lang)
    Scaffold(topBar = { BackTopBar(lesson.title.inLanguage(lang), onBack) }) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            itemsIndexed(lesson.steps) { i, step ->
                var showRefs by rememberSaveable(lesson.id, i) { mutableStateOf(false) }
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StepNumber(i + 1)
                            Spacer1()
                            LessonGradeBadge(step.grade, lang)
                            InfoButton(showRefs, s.source) { showRefs = !showRefs }
                        }
                        step.pose?.let { poseDrawable(it) }?.let { res ->
                            Image(
                                painterResource(res),
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth().height(170.dp),
                            )
                        }
                        Text(step.text.inLanguage(lang), style = MaterialTheme.typography.bodyLarge)
                        step.say?.let { say ->
                            Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(s.say, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                    say.translit?.let { Text(it, style = MaterialTheme.typography.bodyLarge, fontStyle = FontStyle.Italic) }
                                    say.ru?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                                    say.ref?.let { ref ->
                                        quranRef(ref)?.let { key ->
                                            TextButton(onClick = { onOpenAyah(key) }) {
                                                Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null)
                                                Text("  ${key.surah}:${key.ayah}")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        HiddenText(
                            showRefs,
                            listOfNotNull(step.say?.ref, "${s.evidence}: ${step.evidence}", "${s.source}: ${step.source}").joinToString("\n"),
                        )
                    }
                }
            }
        }
    }
}

/** Pictures drawn by tools/gen_poses.py; a step naming an unknown pose simply shows none. */
private fun poseDrawable(pose: String): Int? = when (pose) {
    "stand" -> R.drawable.pose_stand
    "takbir" -> R.drawable.pose_takbir
    "qiyam" -> R.drawable.pose_qiyam
    "ruku" -> R.drawable.pose_ruku
    "sujud" -> R.drawable.pose_sujud
    "jalsa" -> R.drawable.pose_jalsa
    "salam" -> R.drawable.pose_salam
    "takbir_w" -> R.drawable.pose_takbir_w
    "qiyam_w" -> R.drawable.pose_qiyam_w
    "ruku_w" -> R.drawable.pose_ruku_w
    "sujud_w" -> R.drawable.pose_sujud_w
    "jalsa_w" -> R.drawable.pose_jalsa_w
    else -> null
}

/** "Коран 1:1-7" or "Коран 112" opens that place in the reader. */
private fun quranRef(ref: String): AyahKey? {
    if (!ref.contains("Коран")) return null
    Regex("""(\d{1,3}):(\d{1,3})""").find(ref)?.let { return AyahKey(it.groupValues[1].toInt(), it.groupValues[2].toInt()) }
    return Regex("""Коран\s+(\d{1,3})\b""").find(ref)?.let { AyahKey(it.groupValues[1].toInt(), 1) }
}

@Composable
private fun StepNumber(n: Int) {
    Surface(color = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer, shape = CircleShape) {
        Text("$n", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.Spacer1() = androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))

@Composable
private fun LessonGradeBadge(grade: LessonGrade, lang: String) {
    val c = MaterialTheme.colorScheme
    val (bg, fg) = when (grade) {
        LessonGrade.QURAN, LessonGrade.SAHIH -> c.primaryContainer to c.onPrimaryContainer
        LessonGrade.HASAN -> c.secondaryContainer to c.onSecondaryContainer
        LessonGrade.FIQH_HANAFI, LessonGrade.OPINION -> c.surfaceVariant to c.onSurfaceVariant
    }
    Surface(color = bg, contentColor = fg, shape = RoundedCornerShape(50)) {
        Text(grade.label(lang), style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
    }
}
