package com.stonicai.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.stonicai.app.ui.theme.Cyan
import kotlin.math.cos
import kotlin.math.sin

/**
 * The central Stonic "Neural Core" — faithful to the desktop React VlI component:
 * a rotating wireframe globe with orbiting nodes and a pulsing cyan halo.
 */
@Composable
fun NeuralCore(
    size: Dp = 170.dp,
    isSpeaking: Boolean = false,
    isConnected: Boolean = true,
    isConnecting: Boolean = false,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "core")
    val rotation by transition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(if (isSpeaking) 6000 else 18000, easing = LinearEasing)),
        label = "rot"
    )
    val rotationReverse by transition.animateFloat(
        initialValue = 360f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(26000, easing = LinearEasing)),
        label = "rotr"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.55f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(if (isSpeaking) 700 else 2200, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val nodeBounce by transition.animateFloat(
        initialValue = 0.85f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse),
        label = "nb"
    )

    val core = if (isConnecting) Color(0xFFFBBF24) else Cyan

    Box(modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.size(size)) {
            val r = this.size.minDimension / 2f
            val c = center

            // outer glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        core.copy(alpha = 0.35f * pulse),
                        core.copy(alpha = 0.10f * pulse),
                        Color.Transparent
                    ),
                    center = c, radius = r * 1.25f
                ),
                radius = r * 1.25f, center = c
            )

            // outer thin ring
            drawCircle(core.copy(alpha = 0.18f), radius = r * 0.98f, center = c, style = Stroke(1.2f))
            drawCircle(core.copy(alpha = 0.08f), radius = r * 0.82f, center = c, style = Stroke(0.8f))

            // meridians
            rotate(rotation, c) {
                for (i in 0 until 3) {
                    val scale = 0.35f + i * 0.22f
                    drawOval(
                        color = core.copy(alpha = 0.30f),
                        topLeft = androidx.compose.ui.geometry.Offset(
                            c.x - r * scale, c.y - r * 0.85f
                        ),
                        size = androidx.compose.ui.geometry.Size(r * scale * 2, r * 1.7f),
                        style = Stroke(1f)
                    )
                }
            }

            // parallels
            rotate(rotationReverse, c) {
                for (i in -2..2) {
                    val yFrac = i * 0.28f
                    val rr = r * kotlin.math.sqrt(1f - yFrac * yFrac) * 0.9f
                    drawCircle(
                        color = core.copy(alpha = 0.18f),
                        radius = rr,
                        center = Offset(c.x, c.y + r * yFrac),
                        style = Stroke(0.8f)
                    )
                }
            }

            // central bright core
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        core.copy(alpha = 0.85f),
                        core.copy(alpha = 0.05f)
                    ),
                    center = c, radius = r * 0.32f
                ),
                radius = r * 0.32f * pulse, center = c
            )

            // orbiting nodes
            val orbitR = r * 0.78f
            for (i in 0 until 6) {
                val angle = Math.toRadians((360.0 / 6 * i + rotation).toDouble())
                val nx = c.x + (orbitR * cos(angle)).toFloat()
                val ny = c.y + (orbitR * sin(angle)).toFloat()
                drawCircle(
                    color = core,
                    radius = 3.2f * if (i == 0) nodeBounce else 1f,
                    center = Offset(nx, ny)
                )
                drawCircle(
                    color = core.copy(alpha = 0.5f),
                    radius = 7f,
                    center = Offset(nx, ny),
                    style = Stroke(1f)
                )
            }
        }
    }
}
