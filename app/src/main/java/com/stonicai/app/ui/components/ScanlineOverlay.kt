package com.stonicai.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.stonicai.app.ui.theme.Cyan

@Composable
fun ScanlineOverlay(modifier: Modifier = Modifier) {
    val t = rememberInfiniteTransition(label = "scan")
    val y by t.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing), RepeatMode.Restart),
        label = "y"
    )
    Canvas(modifier = modifier.fillMaxSize()) {
        // subtle moving scan line
        val yy = size.height * y
        drawLine(
            brush = Brush.verticalGradient(
                0f to Color.Transparent,
                0.5f to Cyan.copy(alpha = 0.12f),
                1f to Color.Transparent,
                startY = yy - 40f, endY = yy + 40f
            ),
            start = Offset(0f, yy - 40f), end = Offset(size.width, yy - 40f),
            strokeWidth = 80f
        )
        // grid noise
        val step = 28f
        var x = 0f
        while (x < size.width) {
            drawLine(Color.White.copy(alpha = 0.025f), Offset(x, 0f), Offset(x, size.height), 0.6f)
            x += step
        }
        var yy2 = 0f
        while (yy2 < size.height) {
            drawLine(Color.White.copy(alpha = 0.025f), Offset(0f, yy2), Offset(size.width, yy2), 0.6f)
            yy2 += step
        }
    }
}
