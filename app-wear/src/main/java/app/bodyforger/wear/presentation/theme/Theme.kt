package app.bodyforger.wear.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

val NeonLime = Color(0xFFCCFF00)
val ElectricCyan = Color(0xFF00E5FF)
val Obsidian = Color(0xFF000000)
val SurfaceDark = Color(0xFF16161A)

val WearColors = Colors(
    primary = NeonLime,
    secondary = ElectricCyan,
    background = Obsidian,
    surface = SurfaceDark,
    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun BodyForgerWearTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = WearColors,
        content = content
    )
}
