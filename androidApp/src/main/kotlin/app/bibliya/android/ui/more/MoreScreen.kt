package app.bibliya.android.ui.more

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.bibliya.android.AppConfig
import app.bibliya.android.ui.LocalDeps
import app.bibliya.android.ui.LocalSettings
import app.bibliya.android.ui.LocalStrings
import app.bibliya.android.ui.LocalTranslation
import app.bibliya.android.ui.components.SectionTitle
import app.bibliya.shared.model.AppSettings
import app.bibliya.shared.model.ThemeMode

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun MoreScreen(
    notificationsAllowed: Boolean,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    onEnableReminder: () -> Unit,
) {
    val strings = LocalStrings.current
    val settings = LocalSettings.current
    val deps = LocalDeps.current
    val translation = LocalTranslation.current
    var timeDialog by remember { mutableStateOf(false) }

    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
        item { SectionTitle(strings.settings) }
        item {
            ChoiceRow(
                title = strings.language,
                options = listOf("" to strings.languageAuto, "ru" to "Русский", "en" to "English"),
                selected = settings.language,
                onSelect = { code -> onUpdateSettings { it.copy(language = code) } },
            )
        }
        item {
            ChoiceRow(
                title = strings.translation,
                options = listOf("ru" to "Синодальный", "en" to "KJV"),
                selected = translation,
                onSelect = { code -> onUpdateSettings { it.copy(translation = code) } },
            )
        }
        item {
            ChoiceRow(
                title = strings.theme,
                options = listOf(
                    ThemeMode.SYSTEM.name to strings.themeSystem,
                    ThemeMode.LIGHT.name to strings.themeLight,
                    ThemeMode.DARK.name to strings.themeDark,
                ),
                selected = settings.theme.name,
                onSelect = { name -> onUpdateSettings { it.copy(theme = ThemeMode.valueOf(name)) } },
            )
        }
        item {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text("${strings.textSize}: ${settings.fontSize}", style = MaterialTheme.typography.bodyLarge)
                Slider(
                    value = settings.fontSize.toFloat(),
                    onValueChange = { v -> onUpdateSettings { it.copy(fontSize = v.toInt()) } },
                    valueRange = 14f..32f,
                    steps = 17,
                )
            }
        }
        item {
            ListItem(
                headlineContent = { Text(strings.showDeuterocanon) },
                supportingContent = { Text(strings.showDeuterocanonNote) },
                trailingContent = {
                    Switch(checked = settings.showDeuterocanon, onCheckedChange = { on -> onUpdateSettings { it.copy(showDeuterocanon = on) } })
                },
            )
        }
        item {
            ListItem(
                headlineContent = { Text(strings.reminder) },
                supportingContent = {
                    Text(if (settings.dailyReminder && !notificationsAllowed) strings.notificationsDenied else strings.reminderNote)
                },
                trailingContent = {
                    Switch(checked = settings.dailyReminder, onCheckedChange = { on ->
                        onUpdateSettings { it.copy(dailyReminder = on) }
                        if (on) onEnableReminder()
                    })
                },
            )
        }
        if (settings.dailyReminder) {
            item {
                ListItem(
                    headlineContent = { Text(strings.reminderTime) },
                    trailingContent = {
                        Text(
                            "%02d:%02d".format(settings.reminderMinute / 60, settings.reminderMinute % 60),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    },
                    modifier = Modifier.clickable { timeDialog = true },
                )
            }
        }

        item { HorizontalDivider(Modifier.padding(vertical = 8.dp)) }
        item { SectionTitle(strings.about) }
        item { Expandable(strings.about, strings.aboutText) }
        item { Expandable(strings.sources, strings.sourcesText) }
        item {
            Expandable(strings.privacy, strings.privacyText) {
                TextButton(onClick = { deps.platform.openUrl(AppConfig.PRIVACY_POLICY_URL) }) { Text(AppConfig.PRIVACY_POLICY_URL, maxLines = 1) }
            }
        }
        if (AppConfig.hasSupportEmail) {
            item {
                ListItem(
                    headlineContent = { Text(strings.feedback) },
                    supportingContent = { Text(AppConfig.SUPPORT_EMAIL) },
                    modifier = Modifier.clickable { deps.platform.email(AppConfig.SUPPORT_EMAIL, strings.appName) },
                )
            }
        }
        item {
            ListItem(headlineContent = { Text(strings.version) }, trailingContent = { Text(deps.versionName) })
        }
    }

    if (timeDialog) {
        val state = rememberTimePickerState(
            initialHour = settings.reminderMinute / 60,
            initialMinute = settings.reminderMinute % 60,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { timeDialog = false },
            text = { TimePicker(state = state) },
            confirmButton = {
                TextButton(onClick = {
                    timeDialog = false
                    onUpdateSettings { it.copy(reminderMinute = state.hour * 60 + state.minute) }
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { timeDialog = false }) { Text(strings.cancel) } },
        )
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun ChoiceRow(title: String, options: List<Pair<String, String>>, selected: String, onSelect: (String) -> Unit) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { (code, label) ->
                FilterChip(selected = code == selected, onClick = { onSelect(code) }, label = { Text(label) })
            }
        }
    }
}

@Composable
private fun Expandable(title: String, text: String, extra: @Composable () -> Unit = {}) {
    var open by rememberSaveable(title) { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().clickable { open = !open }.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text(if (open) "−" else "+", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        }
        AnimatedVisibility(visible = open) {
            Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
                Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                extra()
            }
        }
    }
}
