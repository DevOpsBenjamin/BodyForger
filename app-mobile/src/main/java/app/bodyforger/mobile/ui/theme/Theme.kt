package app.bodyforger.mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val NeonLime = Color(0xFFCCFF00)
val Obsidian = Color(0xFF0F0F11)
val SurfaceDark = Color(0xFF1B1B1E)
val TextPrimary = Color(0xFFEEEEEE)
val TextSecondary = Color(0xFFAAAAAA)

private val DarkColorScheme = darkColorScheme(
    primary = NeonLime,
    background = Obsidian,
    surface = SurfaceDark,
    onPrimary = Color.Black,
    onBackground = TextPrimary,
    onSurface = TextPrimary
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
