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
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import app.qurandua.android.R
import app.qurandua.shared.data.UI_LANGUAGES

/** Short code of each interface language, shown next to its flag, e.g. KZ. */
val LANGUAGE_CODES = mapOf(
    "ru" to "RU", "kk" to "KZ", "ky" to "KG", "uz" to "UZ", "tg" to "TJ", "tt" to "TT", "az" to "AZ", "tr" to "TR",
    "en" to "EN", "fr" to "FR", "de" to "DE", "es" to "ES", "ar" to "AR", "id" to "ID", "ur" to "UR",
)

/**
 * Flags are pictures (res/drawable-nodpi, from flag-icons, MIT; Tatarstan drawn by hand): emoji flags show
 * as two letters on many phones and on Windows.
 */
private val FLAGS = mapOf(
    "ru" to R.drawable.flag_ru, "kk" to R.drawable.flag_kk, "ky" to R.drawable.flag_ky, "uz" to R.drawable.flag_uz,
    "tg" to R.drawable.flag_tg, "tt" to R.drawable.flag_tt, "az" to R.drawable.flag_az, "tr" to R.drawable.flag_tr,
    "en" to R.drawable.flag_en, "fr" to R.drawable.flag_fr, "de" to R.drawable.flag_de, "es" to R.drawable.flag_es,
    "ar" to R.drawable.flag_ar, "id" to R.drawable.flag_id, "ur" to R.drawable.flag_ur,
)

@Composable
private fun Flag(lang: String, size: Dp) {
    val res = FLAGS[lang] ?: return
    Image(
        painterResource(res), contentDescription = null, contentScale = ContentScale.Crop,
        modifier = Modifier.size(size).clip(CircleShape).border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
    )
}

val LANGUAGE_NAMES = mapOf(
    "ru" to "Русский", "kk" to "Қазақша", "ky" to "Кыргызча", "uz" to "O‘zbekcha", "en" to "English", "es" to "Español", "ar" to "العربية",
    "tr" to "Türkçe", "id" to "Bahasa Indonesia", "ur" to "اردو",
    "az" to "Azərbaycanca", "tg" to "Тоҷикӣ", "tt" to "Татарча", "fr" to "Français", "de" to "Deutsch",
)

/** A round button with the current language's flag and code; tapping it lists every language with its flag and code. */
@Composable
fun LanguageButton(language: String, onPick: (String) -> Unit, modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf(false) }
    val code = LANGUAGE_CODES[language] ?: language.uppercase()
    Box(modifier) {
        Surface(
            onClick = { open = true },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.size(52.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Flag(language, 24.dp)
                Text(code, style = MaterialTheme.typography.labelSmall)
            }
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            UI_LANGUAGES.forEach { lang ->
                val c = LANGUAGE_CODES[lang] ?: lang.uppercase()
                DropdownMenuItem(
                    text = {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Flag(lang, 28.dp)
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
