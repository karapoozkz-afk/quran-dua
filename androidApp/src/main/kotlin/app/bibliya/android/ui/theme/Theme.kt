package app.bibliya.android.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import app.bibliya.shared.model.ThemeMode

private val Burgundy = Color(0xFF6E2433)
private val BurgundyLight = Color(0xFFF2B4BE)
private val Gold = Color(0xFF9A7230)
private val GoldLight = Color(0xFFE5C27E)

private val LightColors = lightColorScheme(
    primary = Burgundy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF6DDE1),
    onPrimaryContainer = Color(0xFF3B0A15),
    secondary = Gold,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF4E6C8),
    onSecondaryContainer = Color(0xFF3A2A05),
    tertiary = Color(0xFF3D5A80),
    background = Color(0xFFFAF7F0),
    surface = Color(0xFFFAF7F0),
    surfaceVariant = Color(0xFFEFEADF),
    onSurfaceVariant = Color(0xFF4D4740),
    // Warm paper tones for cards and the tab bar instead of Material's default lavender.
    surfaceContainerLowest = Color(0xFFFFFDF8),
    surfaceContainerLow = Color(0xFFF6F1E7),
    surfaceContainer = Color(0xFFF2ECE0),
    surfaceContainerHigh = Color(0xFFEDE6D9),
    surfaceContainerHighest = Color(0xFFE8E0D2),
)

private val DarkColors = darkColorScheme(
    primary = BurgundyLight,
    onPrimary = Color(0xFF4A1020),
    primaryContainer = Color(0xFF5A1C29),
    onPrimaryContainer = Color(0xFFF6DDE1),
    secondary = GoldLight,
    onSecondary = Color(0xFF3A2A05),
    secondaryContainer = Color(0xFF574316),
    onSecondaryContainer = Color(0xFFF4E6C8),
    tertiary = Color(0xFFA9C7F0),
    background = Color(0xFF151214),
    surface = Color(0xFF151214),
    surfaceVariant = Color(0xFF2C2629),
    onSurfaceVariant = Color(0xFFD0C7C2),
    surfaceContainerLowest = Color(0xFF100D0F),
    surfaceContainerLow = Color(0xFF1D191B),
    surfaceContainer = Color(0xFF221D20),
    surfaceContainerHigh = Color(0xFF2C2629),
    surfaceContainerHighest = Color(0xFF373033),
)

@Composable
fun BibleTheme(mode: ThemeMode, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    // The app's own theme can differ from the phone's: keep the status and navigation bar icons readable.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }
    MaterialTheme(colorScheme = if (dark) DarkColors else LightColors, content = content)
}
