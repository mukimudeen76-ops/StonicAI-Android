package com.stonicai.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val StonicColorScheme = darkColorScheme(
    primary = StonicAccent,
    onPrimary = Color.Black,
    secondary = Color.White,
    onSecondary = Color.Black,
    background = StonicBg,
    onBackground = Color.White,
    surface = StonicSurface,
    onSurface = Color.White,
    surfaceVariant = StonicInput,
    onSurfaceVariant = StonicTextDim,
    error = StonicError,
    outline = StonicBorderStrong
)

@Composable
fun StonicTheme(content: @Composable () -> Unit) {
    // Stonic is always a dark "terminal" UI, matching the desktop app.
    isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = StonicColorScheme,
        typography = StonicTypography,
        content = content
    )
}
