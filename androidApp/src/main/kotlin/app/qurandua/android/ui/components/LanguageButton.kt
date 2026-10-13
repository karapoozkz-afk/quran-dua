package app.qurandua.android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.qurandua.shared.data.UI_LANGUAGES

/** Flag and short code of each interface language, e.g. 🇰🇿 KZ. Tatar has no emoji flag of its own. */
val LANGUAGE_BADGES = mapOf(
    "ru" to ("🇷🇺" to "RU"), "kk" to ("🇰🇿" to "KZ"), "ky" to ("🇰🇬" to "KG"), "uz" to ("🇺🇿" to "UZ"),
    "tg" to ("🇹🇯" to "TJ"), "tt" to ("🌙" to "TT"), "az" to ("🇦🇿" to "AZ"), "tr" to ("🇹🇷" to "TR"),
    "en" to ("🇬🇧" to "EN"), "fr" to ("🇫🇷" to "FR"), "de" to ("🇩🇪" to "DE"), "es" to ("🇪🇸" to "ES"),
    "ar" to ("🇸🇦" to "AR"), "id" to ("🇮🇩" to "ID"), "ur" to ("🇵🇰" to "UR"),
)

val LANGUAGE_NAMES = mapOf(
    "ru" to "Русский", "kk" to "Қазақша", "ky" to "Кыргызча", "uz" to "O‘zbekcha", "en" to "English", "es" to "Español", "ar" to "العربية",
    "tr" to "Türkçe", "id" to "Bahasa Indonesia", "ur" to "اردو",
    "az" to "Azərbaycanca", "tg" to "Тоҷикӣ", "tt" to "Татарча", "fr" to "Français", "de" to "Deutsch",
)

/** A round button with the current language's flag; tapping it lists every language with its flag and code. */
@Composable
fun LanguageButton(language: String, onPick: (String) -> Unit, modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf(false) }
    val (flag, code) = LANGUAGE_BADGES[language] ?: ("🌐" to language.uppercase())
    Box(modifier) {
        Surface(
            onClick = { open = true },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.size(52.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text(flag, fontSize = 20.sp)
                Text(code, style = MaterialTheme.typography.labelSmall)
            }
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            UI_LANGUAGES.forEach { lang ->
                val (f, c) = LANGUAGE_BADGES[lang] ?: ("🌐" to lang.uppercase())
                DropdownMenuItem(
                    text = {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(f, fontSize = 20.sp)
                            Text(c, style = MaterialTheme.typography.titleSmall)
                            Text(LANGUAGE_NAMES[lang] ?: lang, style = MaterialTheme.typography.bodyMedium)
                        }
                    },
                    trailingIcon = { if (lang == language) Icon(Icons.Filled.Check, contentDescription = null) },
                    onClick = {
                        open = false
                        onPick(lang)
                    },
                )
            }
        }
    }
}
