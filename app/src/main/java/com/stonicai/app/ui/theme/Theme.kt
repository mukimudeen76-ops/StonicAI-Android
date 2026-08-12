package com.stonicai.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val StonicColors = darkColorScheme(
    primary = Cyan,
    onPrimary = Color.Black,
    secondary = IronIce,
    onSecondary = Color.Black,
    background = BgBlack,
    onBackground = Text,
    surface = BgPanel,
    onSurface = Text,
    surfaceVariant = BgInput,
    onSurfaceVariant = TextDim,
    error = Danger,
    outline = BorderSoft
)

@Composable
fun StonicTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = StonicColors,
        typography = StonicTypography,
        content = content
    )
}
