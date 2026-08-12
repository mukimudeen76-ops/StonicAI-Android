package com.stonicai.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.stonicai.app.ui.theme.Cyan

@Composable
fun StonicOrb(
    active: Boolean = false,
    size: androidx.compose.ui.unit.Dp = 64.dp,
    color: Color = Cyan
) {
    val transition = rememberInfiniteTransition(label = "orb")
    val pulse by transition.animateFloat(
        initialValue = if (active) 0.65f else 0.85f,
        targetValue = if (active) 1.05f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (active) 900 else 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb-scale"
    )
    val halo by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = if (active) 0.7f else 0.45f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse),
        label = "halo"
    )

    Canvas(modifier = Modifier.size(size)) {
        val r = this.size.minDimension / 2f
        val c = center
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color.copy(alpha = halo), Color.Transparent),
                center = c,
                radius = r * 1.4f
            ),
            radius = r * 1.4f,
            center = c
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.9f),
                    color.copy(alpha = 0.85f),
                    color.copy(alpha = 0.15f)
                ),
                center = Offset(c.x - r * 0.25f, c.y - r * 0.3f),
                radius = r * 1.1f
            ),
            radius = r * pulse,
            center = c
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = r * 0.25f * pulse,
            center = Offset(c.x - r * 0.28f, c.y - r * 0.3f)
        )
    }
}
