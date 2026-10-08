package app.qurandua.android.ui.more

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.TextButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.qurandua.android.ui.LocalContentLang
import app.qurandua.android.ui.LocalDeps
import app.qurandua.android.ui.LocalSettings
import app.qurandua.android.ui.LocalStrings
import app.qurandua.android.ui.components.NoticeCard
import app.qurandua.android.ui.components.SectionTitle
import app.qurandua.shared.data.UI_LANGUAGES
import app.qurandua.shared.model.AppSettings
import app.qurandua.shared.model.Charity
import app.qurandua.shared.model.pick
import app.qurandua.shared.model.ThemeMode
import app.qurandua.shared.model.TRANSLIT_SCRIPTS

/** Settings, sadaqah and the sources behind the content. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MoreScreen(
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    onOpenZakat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    val settings = LocalSettings.current
    val deps = LocalDeps.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item { SectionTitle(strings.settings) }
        item {
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(strings.language, style = MaterialTheme.typography.bodyLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    UI_LANGUAGES.forEach { code ->
                        FilterChip(
                            selected = settings.language == code,
                            onClick = { onUpdateSettings { it.copy(language = code) } },
                            label = { Text(LANGUAGE_NAMES[code] ?: code) },
                        )
                    }
                }
            }
        }
        item {
            SettingRow(strings.showTranslit, settings.showTransliteration) { value ->
                onUpdateSettings { it.copy(showTransliteration = value) }
            }
        }
        item {
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(strings.translitScript, style = MaterialTheme.typography.bodyLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TRANSLIT_SCRIPTS.forEach { code ->
                        FilterChip(
                            selected = settings.translitScript == code,
                            onClick = { onUpdateSettings { it.copy(translitScript = code) } },
                            label = { Text(strings.translitScriptName(code)) },
                        )
                    }
                }
                Text(strings.translitQuranNote, style = MaterialTheme.typography.bodySmall)
            }
        }
        item {
            SettingRow(strings.showTranslation, settings.showTranslation) { value ->
                onUpdateSettings { it.copy(showTranslation = value) }
            }
        }
        item {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text("${strings.arabicSize}: ${settings.arabicFontSize}", style = MaterialTheme.typography.bodyLarge)
                Slider(
                    value = settings.arabicFontSize.toFloat(),
                    onValueChange = { value -> onUpdateSettings { it.copy(arabicFontSize = value.toInt()) } },
                    valueRange = 18f..44f,
                    steps = 12,
                )
            }
        }
        item {
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(strings.theme, style = MaterialTheme.typography.bodyLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val labels = mapOf(
                        ThemeMode.SYSTEM to strings.themeSystem,
                        ThemeMode.LIGHT to strings.themeLight,
                        ThemeMode.DARK to strings.themeDark,
                    )
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = settings.theme == mode,
                            onClick = { onUpdateSettings { it.copy(theme = mode) } },
                            label = { Text(labels.getValue(mode)) },
                        )
                    }
                }
            }
        }

        item { SectionTitle(strings.donate) }
        item {
            Card(onClick = onOpenZakat, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(strings.zakat, style = MaterialTheme.typography.titleMedium)
                    Text(strings.zakatIntro, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            Text(
                strings.charitiesTitle,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
        if (deps.content.charities.isEmpty()) {
            item {
                Text(
                    strings.charitiesEmpty,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
        items(deps.content.charities, key = { it.id }) { charity ->
            CharityCard(charity, Modifier.padding(horizontal = 16.dp))
        }
        item {
            Text(
                strings.supportApp,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        item { SectionTitle(strings.about) }
        item { NoticeCard(strings.aboutBody, Modifier.padding(horizontal = 16.dp)) }
        item {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(strings.sourcesTitle, style = MaterialTheme.typography.titleSmall)
                Text(
                    strings.sourcesBody,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SettingRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

private val LANGUAGE_NAMES = mapOf(
    "ru" to "Русский", "kk" to "Қазақша", "ky" to "Кыргызча", "uz" to "O‘zbekcha", "en" to "English", "es" to "Español", "ar" to "العربية",
    "tr" to "Türkçe", "id" to "Bahasa Indonesia", "ur" to "اردو",
)

@Composable
private fun CharityCard(charity: Charity, modifier: Modifier = Modifier) {
    val strings = LocalStrings.current
    val lang = LocalContentLang.current
    val deps = LocalDeps.current
    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(charity.name.pick(lang), style = MaterialTheme.typography.titleMedium)
            Text(
                charity.purposes.joinToString(" · ") { strings.charityPurpose(it) },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text("${strings.charityReg}: ${charity.registrationNumber} (${charity.country})", style = MaterialTheme.typography.bodySmall)
            Text(
                strings.charityVerified(charity.verifiedOn, charity.verifiedBy),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { deps.platform.openUrl(charity.donateUrl) }) { Text(strings.charityDonate) }
                TextButton(onClick = { deps.platform.openUrl(charity.website) }) { Text(strings.charityWebsite) }
            }
        }
    }
}
