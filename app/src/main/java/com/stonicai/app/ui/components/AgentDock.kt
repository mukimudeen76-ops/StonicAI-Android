package com.stonicai.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stonicai.app.data.Persona
import com.stonicai.app.ui.theme.BorderSoft
import com.stonicai.app.ui.theme.Cyan
import com.stonicai.app.ui.theme.Text
import com.stonicai.app.ui.theme.TextDim
import com.stonicai.app.ui.theme.TextMuted

/**
 * Bottom row of cute "cartoon" agents, one per persona. Tap to switch the
 * active soul. The selected agent bobs and glows like the desktop orb.
 */
@Composable
fun AgentDock(
    selected: Persona,
    onSelect: (Persona) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Persona.values().forEach { p ->
            AgentAvatar(p, p.id == selected.id) { onSelect(p) }
        }
    }
}

@Composable
private fun AgentAvatar(p: Persona, selected: Boolean, onClick: () -> Unit) {
    val t = rememberInfiniteTransition(label = "bob-${p.id}")
    val bob by t.animateFloat(
        initialValue = 0f, targetValue = if (selected) -6f else 0f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "y"
    )
    val pulse by t.animateFloat(
        initialValue = 0.6f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "p"
    )
    val color = Color(p.color)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.BottomCenter) {
            if (selected) {
                Box(
                    Modifier
                        .size(48.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(color.copy(alpha = 0.55f * pulse), Color.Transparent)
                            ), CircleShape
                        )
                )
            }
            Surface(
                color = if (selected) color.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.03f),
                shape = CircleShape,
                border = BorderStroke(
                    1.5.dp,
                    if (selected) color else BorderSoft
                ),
                modifier = Modifier
                    .size(if (selected) 46.dp else 40.dp)
                    .offset(y = bob.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(p.emoji, fontSize = if (selected) 22.sp else 18.sp)
                }
            }
        }
        Spacer(Modifier.height(2.dp))
        Text(
            p.displayName.uppercase(),
            color = if (selected) color else TextMuted,
            fontSize = 7.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1
        )
    }
}
