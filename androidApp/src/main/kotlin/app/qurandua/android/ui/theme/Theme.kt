package app.qurandua.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import app.qurandua.shared.model.ThemeMode

private val Emerald = Color(0xFF0F6B4F)
private val EmeraldLight = Color(0xFF6FD3AE)
private val Gold = Color(0xFFB8893B)
private val GoldLight = Color(0xFFE5C27E)

private val LightColors = lightColorScheme(
    primary = Emerald,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3F0E4),
    onPrimaryContainer = Color(0xFF00382A),
    secondary = Gold,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF6E7C8),
    onSecondaryContainer = Color(0xFF3D2B00),
    tertiary = Color(0xFF8A4B38),
    background = Color(0xFFFBFAF6),
    surface = Color(0xFFFBFAF6),
    surfaceVariant = Color(0xFFEFEDE4),
    onSurfaceVariant = Color(0xFF4A4A42),
)

private val DarkColors = darkColorScheme(
    primary = EmeraldLight,
    onPrimary = Color(0xFF003828),
    primaryContainer = Color(0xFF0B5039),
    onPrimaryContainer = Color(0xFFD3F0E4),
    secondary = GoldLight,
    onSecondary = Color(0xFF3D2B00),
    secondaryContainer = Color(0xFF5A4413),
    onSecondaryContainer = Color(0xFFF6E7C8),
    tertiary = Color(0xFFFFB59E),
    background = Color(0xFF111412),
    surface = Color(0xFF111412),
    surfaceVariant = Color(0xFF2A2E2B),
    onSurfaceVariant = Color(0xFFC7C9C2),
)

@Composable
fun QuranDuaTheme(mode: ThemeMode, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MaterialTheme(colorScheme = if (dark) DarkColors else LightColors, content = content)
}
