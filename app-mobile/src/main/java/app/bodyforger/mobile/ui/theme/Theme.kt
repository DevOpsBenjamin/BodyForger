package app.bodyforger.mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val NeonLime = Color(0xFFCCFF00)
val Obsidian = Color(0xFF0A0A0C)
val SurfaceDark = Color(0xFF16161A)
val SurfaceElevated = Color(0xFF222228)
val SurfaceBorder = Color(0xFF2A2A32)
val ElectricCyan = Color(0xFF00E5FF)
val AmberGold = Color(0xFFFFB800)
val CrimsonRed = Color(0xFFFF3B30)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFF8E8E93)
val TextMuted = Color(0xFF8E8E93)
val TextTertiary = Color(0xFF55555C)

private val DarkColorScheme = darkColorScheme(
    primary = NeonLime,
    secondary = ElectricCyan,
    tertiary = AmberGold,
    background = Obsidian,
    surface = SurfaceDark,
    surfaceVariant = SurfaceElevated,
    outline = SurfaceBorder,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary
)

@Composable
fun BodyForgerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
