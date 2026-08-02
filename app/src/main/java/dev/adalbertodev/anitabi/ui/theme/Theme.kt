package dev.adalbertodev.anitabi.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Violet,
    onPrimary = OnViolet,
    secondary = VioletDim,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    surfaceContainer = SurfaceMenu,
    surfaceContainerLow = SurfaceCard,
    surfaceContainerHigh = SurfaceMenu,
    outline = VioletDim,
    error = ErrorRed,
    onError = OnErrorRed,

    tertiary = Pink80
)

@Composable
fun AniTabiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}