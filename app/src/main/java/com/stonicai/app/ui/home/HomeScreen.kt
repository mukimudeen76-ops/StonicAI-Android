package com.stonicai.app.ui.home

import android.Manifest
import android.content.Intent
import android.os.Build
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stonicai.app.data.Models
import com.stonicai.app.data.Persona
import com.stonicai.app.ui.chat.ChatViewModel
import com.stonicai.app.ui.components.AgentDock
import com.stonicai.app.ui.components.NeuralCore
import com.stonicai.app.ui.components.ScanlineOverlay
import com.stonicai.app.ui.theme.BgBlack
import com.stonicai.app.ui.theme.BgInput
import com.stonicai.app.ui.theme.BgPanel
import com.stonicai.app.ui.theme.BorderFaint
import com.stonicai.app.ui.theme.BorderSoft
import com.stonicai.app.ui.theme.Cyan
import com.stonicai.app.ui.theme.Danger
import com.stonicai.app.ui.theme.Text
import com.stonicai.app.ui.theme.TextDim
import com.stonicai.app.ui.theme.TextMuted

@Composable
fun HomeScreen(
    onOpenMemory: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSoul: () -> Unit,
    onOpenSkills: () -> Unit,
    onOpenDesktop: () -> Unit,
    onOpenVoice: () -> Unit = {},
    onStartChat: (String) -> Unit = {},
    vm: ChatViewModel = viewModel()
) {
    val context = LocalContext.current
    val keyboard = LocalSoftwareKeyboardController.current
    val settings by vm.settings.collectAsState()
    val isStreaming by vm.isStreaming.collectAsState()
    var input by rememberSaveable { mutableStateOf("") }

    fun submit() {
        val text = input.trim()
        if (text.isEmpty() || isStreaming) return
        input = ""
        keyboard?.hide()
        onStartChat(text)
    }

    val speech = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        val text = matches?.firstOrNull()
        if (!text.isNullOrBlank()) input = if (input.isBlank()) text else "$input $text"
    }
    val micPerm = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            }
            runCatching { speech.launch(intent) }
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(BgBlack)
            .statusBarsPadding()
    ) {
        ScanlineOverlay(Modifier.align(Alignment.Center))

        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(10.dp).background(Cyan, CircleShape))
                    Spacer(Modifier.width(8.dp))
                    Text("STONIC", color = Text, fontWeight = FontWeight.Black,
                        fontSize = 13.sp, letterSpacing = 4.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    TopIcon(Icons.Default.Memory, "Memory", onOpenMemory)
                    TopIcon(Icons.Default.RecordVoiceOver, "Voice", onOpenVoice)
                    TopIcon(Icons.Default.Psychology, "Soul", onOpenSoul)
                    TopIcon(Icons.Default.AutoAwesome, "Skills", onOpenSkills)
                    TopIcon(Icons.Default.Settings, "Settings", onOpenSettings)
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = BorderFaint)
            Spacer(Modifier.weight(1f))

            Surface(
                color = BgPanel,
                shape = RoundedCornerShape(48.dp),
                border = BorderStroke(1.dp, Cyan.copy(alpha = 0.12f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    NeonCorners()
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ThinkingChip(settings.expertMode) { vm.setExpert(!settings.expertMode) }
                        Spacer(Modifier.height(18.dp))
                        NeuralCore(size = if (isStreaming) 190.dp else 170.dp, isSpeaking = isStreaming)
                        Spacer(Modifier.height(18.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(4.dp).background(Cyan, CircleShape))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (isStreaming) "ANALYZING" else "COMMUNICATION LINK STABLE",
                                color = if (isStreaming) TextDim else Cyan,
                                fontWeight = FontWeight.Black, fontSize = 9.sp, letterSpacing = 4.sp
                            )
                            Spacer(Modifier.width(8.dp))
                            Box(Modifier.size(4.dp).background(Cyan, CircleShape))
                        }
                        Spacer(Modifier.height(16.dp))
                        Surface(
                            color = if (isStreaming) Danger else Cyan,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .width(180.dp).height(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { if (input.isNotBlank()) submit() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    if (isStreaming) "STOP" else "START AI",
                                    color = Color.Black, fontWeight = FontWeight.Black,
                                    fontSize = 12.sp, letterSpacing = 2.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MiniPill("PERSONA", Persona.byId(settings.personaId).displayName.uppercase())
                MiniPill("ENGINE", Models.byId(settings.selectedModelId).displayName.uppercase())
            }

            Spacer(Modifier.weight(1f))

            AgentDock(
                selected = Persona.byId(settings.personaId),
                onSelect = { vm.setPersona(it.id) },
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Surface(
                color = BgInput.copy(alpha = 0.6f),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BorderSoft),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = input, onValueChange = { input = it },
                        placeholder = {
                            Text(
                                if (settings.expertMode) "AWAITING COMMAND…" else "MESSAGE STONIC…",
                                color = TextMuted, fontWeight = FontWeight.Black,
                                fontSize = 10.sp, letterSpacing = 2.sp
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Text,
                            unfocusedTextColor = Text,
                            cursorColor = Cyan
                        ),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Default
                        ),
                        maxLines = 5
                    )
                    Box(
                        Modifier.size(40.dp).clip(CircleShape).background(BgPanel)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                    Manifest.permission.RECORD_AUDIO else null
                                if (perm == null || context.checkSelfPermission(perm) ==
                                    android.content.pm.PackageManager.PERMISSION_GRANTED
                                ) {
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    }
                                    runCatching { speech.launch(intent) }
                                } else micPerm.launch(perm)
                            },
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.Mic, "Voice", tint = TextDim, modifier = Modifier.size(18.dp)) }
                    Spacer(Modifier.width(6.dp))
                    Box(
                        Modifier.size(44.dp).clip(CircleShape)
                            .background(if (isStreaming) Danger else Cyan)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (isStreaming) vm.stop()
                                else if (input.isNotBlank()) submit()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isStreaming) Icons.Default.Stop else Icons.AutoMirrored.Filled.Send,
                            "Send", tint = Color.Black, modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopIcon(icon: ImageVector, label: String, onClick: () -> Unit) {
    Box(
        Modifier.size(36.dp).clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) { Icon(icon, label, tint = TextDim, modifier = Modifier.size(18.dp)) }
}

@Composable
private fun ThinkingChip(enabled: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (enabled) Cyan.copy(alpha = 0.08f) else Color.Transparent,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, if (enabled) Cyan.copy(alpha = 0.35f) else BorderFaint),
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null, onClick = onClick
        )
    ) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Bolt, "Expert",
                tint = if (enabled) Cyan else TextMuted, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(6.dp))
            Text("EXPERT", color = if (enabled) Cyan else TextMuted,
                fontWeight = FontWeight.Black, fontSize = 9.sp, letterSpacing = 2.sp)
        }
    }
}

@Composable
private fun MiniPill(label: String, value: String) {
    Surface(color = BgPanel, shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, BorderFaint)) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
            Text(label, color = TextMuted, fontSize = 8.sp,
                fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            Text(value, color = Text, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
private fun NeonCorners() {
    val t = androidx.compose.animation.core.rememberInfiniteTransition(label = "corner")
    val a by t.animateFloat(
        0.3f, 0.8f,
        androidx.compose.animation.core.infiniteRepeatable(
            androidx.compose.animation.core.tween(1800),
            androidx.compose.animation.core.RepeatMode.Reverse
        ), label = "a"
    )
    val corners = listOf(
        Alignment.TopStart, Alignment.TopEnd, Alignment.BottomStart, Alignment.BottomEnd
    )
    Box(Modifier.fillMaxSize()) {
        corners.forEach { align ->
            Box(
                Modifier.align(align).padding(18.dp).size(28.dp)
                    .background(
                        Brush.radialGradient(listOf(Cyan.copy(alpha = a), Color.Transparent)),
                        CircleShape
                    )
            )
        }
    }
}
