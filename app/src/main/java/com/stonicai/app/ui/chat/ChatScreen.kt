package com.stonicai.app.ui.chat

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stonicai.app.accessibility.StonicAccessibilityService
import com.stonicai.app.data.ChatMessage
import com.stonicai.app.data.MessageStatus
import com.stonicai.app.data.Models
import com.stonicai.app.data.Persona
import com.stonicai.app.data.Sender
import com.stonicai.app.ui.components.MarkdownText
import com.stonicai.app.ui.components.StonicOrb
import com.stonicai.app.ui.theme.BgBlack
import com.stonicai.app.ui.theme.BgInput
import com.stonicai.app.ui.theme.BgPanel
import com.stonicai.app.ui.theme.BorderFaint
import com.stonicai.app.ui.theme.BorderSoft
import com.stonicai.app.ui.theme.Cyan
import com.stonicai.app.ui.theme.Danger
import com.stonicai.app.ui.theme.Ok
import com.stonicai.app.ui.theme.Text
import com.stonicai.app.ui.theme.TextDim
import com.stonicai.app.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    pendingCommand: String? = null,
    onOpenSettings: () -> Unit,
    onOpenSkills: () -> Unit = {},
    onOpenMemory: () -> Unit = {},
    onOpenTasks: () -> Unit = {},
    onOpenDesktop: () -> Unit = {},
    vm: ChatViewModel = viewModel()
) {
    LaunchedEffect(pendingCommand) {
        if (!pendingCommand.isNullOrBlank()) vm.send(pendingCommand)
    }
    val state by vm.messages.collectAsState()
    val settings by vm.settings.collectAsState()
    val isStreaming by vm.isStreaming.collectAsState()
    val context = LocalContext.current
    val keyboard = LocalSoftwareKeyboardController.current

    var input by rememberSaveable { mutableStateOf("") }
    var modelMenu by remember { mutableStateOf(false) }
    var personaMenu by remember { mutableStateOf(false) }
    var clearDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    val speech = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        val text = matches?.firstOrNull()
        if (!text.isNullOrBlank()) {
            input = if (input.isBlank()) text else "$input $text"
        }
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

    LaunchedEffect(state.size, state.lastOrNull()?.text?.length) {
        if (state.isNotEmpty()) listState.animateScrollToItem(state.lastIndex)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBlack)
            .statusBarsPadding()
    ) {
        // ── Top HUD ────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StonicOrb(active = isStreaming, size = 40.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    Persona.byId(settings.personaId).displayName,
                    color = Text,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isStreaming) Cyan else Ok)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (isStreaming) "ANALYZING" else "COMMUNICATION LINK STABLE",
                        color = TextMuted,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            // A11y shield — opens Accessibility settings for "desktop control"
            val a11yOn = StonicAccessibilityService.isReady()
            Surface(
                color = if (a11yOn) Cyan.copy(alpha = 0.12f) else BgInput,
                shape = CircleShape,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (a11yOn) Cyan.copy(alpha = 0.4f) else BorderFaint
                ),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        context.startActivity(
                            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }) {
                    Icon(
                        Icons.Default.Shield,
                        contentDescription = "Desktop control",
                        tint = if (a11yOn) Cyan else TextDim,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(Modifier.width(6.dp))
            IconButton(onClick = onOpenMemory) {
                Icon(Icons.Default.Memory, "Memory", tint = TextDim)
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Default.Settings, "Settings", tint = TextDim)
            }
        }

        HorizontalDivider(color = BorderFaint, thickness = 1.dp)

        // ── Messages ───────────────────────────────────────────────────────
        Box(modifier = Modifier.weight(1f)) {
            if (state.isEmpty()) {
                EmptyState(
                    persona = Persona.byId(settings.personaId),
                    expert = settings.expertMode
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state, key = { it.id }) { MessageBubble(it) }
                }
            }
        }

        // ── Quick command chips (Android control) ──────────────────────────
        if (!isStreaming) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Chip("Home") { vm.send("home") }
                Chip("Back") { vm.send("back") }
                Chip("Screenshot") { vm.send("screenshot") }
                Chip("Apps") { onOpenDesktop() }
            }
        }

        // ── Input ──────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgBlack)
        ) {
            HorizontalDivider(color = BorderFaint, thickness = 1.dp)

            // Mode row: model + persona + expert
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    SmallPill(
                        label = Models.byId(settings.selectedModelId).displayName,
                        onClick = { modelMenu = true }
                    )
                    DropdownMenu(
                        expanded = modelMenu,
                        onDismissRequest = { modelMenu = false },
                        modifier = Modifier.background(BgPanel)
                    ) {
                        Models.ALL.forEach { m ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(m.displayName, color = Text)
                                        Text(
                                            m.provider.replaceFirstChar { it.uppercase() },
                                            color = TextMuted,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                },
                                onClick = {
                                    vm.selectModel(m.id)
                                    modelMenu = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
                Box {
                    SmallPill(
                        label = "Soul: ${Persona.byId(settings.personaId).displayName}",
                        onClick = { personaMenu = true }
                    )
                    DropdownMenu(
                        expanded = personaMenu,
                        onDismissRequest = { personaMenu = false },
                        modifier = Modifier.background(BgPanel)
                    ) {
                        Persona.values().forEach { p ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(p.displayName, color = Text)
                                        Text(p.tagline, color = TextMuted,
                                            style = MaterialTheme.typography.labelSmall)
                                    }
                                },
                                onClick = {
                                    vm.setPersona(p.id)
                                    personaMenu = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                Text(
                    if (settings.expertMode) "EXPERT" else "NORMAL",
                    color = if (settings.expertMode) Cyan else TextMuted,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { vm.setExpert(!settings.expertMode) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .navigationBarsPadding()
                    .imePadding(),
                verticalAlignment = Alignment.Bottom
            ) {
                Surface(
                    color = BgPanel,
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSoft),
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        placeholder = {
                            Text(
                                if (settings.expertMode) "Command Stonic…" else "Message Stonic…",
                                color = TextMuted
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 150.dp),
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
                        maxLines = 6
                    )
                }

                Spacer(Modifier.width(10.dp))

                CircleButton(
                    bg = BgPanel,
                    fg = if (isStreaming) Danger else TextDim,
                    border = BorderFaint,
                    onClick = {
                        val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                            Manifest.permission.RECORD_AUDIO else null
                        if (perm == null || context.checkSelfPermission(perm) ==
                            android.content.pm.PackageManager.PERMISSION_GRANTED
                        ) {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(
                                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                )
                            }
                            runCatching { speech.launch(intent) }
                        } else {
                            micPerm.launch(perm)
                        }
                    }
                ) {
                    Icon(Icons.Default.Mic, "Voice", tint = it, modifier = Modifier.size(20.dp))
                }

                Spacer(Modifier.width(8.dp))

                CircleButton(
                    bg = if (isStreaming) Danger else Cyan,
                    fg = Color.Black,
                    onClick = {
                        if (isStreaming) vm.stop()
                        else if (input.isNotBlank()) {
                            vm.send(input)
                            input = ""
                            keyboard?.hide()
                        }
                    }
                ) { color ->
                    Icon(
                        if (isStreaming) Icons.Default.Stop else Icons.AutoMirrored.Filled.Send,
                        contentDescription = if (isStreaming) "Stop" else "Send",
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    if (clearDialog) {
        AlertDialog(
            onDismissRequest = { clearDialog = false },
            title = { Text("Clear conversation?") },
            text = { Text("This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { vm.clear(); clearDialog = false }) {
                    Text("Clear", color = Danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { clearDialog = false }) { Text("Cancel") }
            },
            containerColor = BgPanel
        )
    }
}

@Composable
private fun EmptyState(persona: Persona, expert: Boolean) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            StonicOrb(active = true, size = 84.dp)
            Spacer(Modifier.height(18.dp))
            Text(
                persona.displayName.uppercase(),
                color = Text,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                letterSpacing = 3.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                if (expert) "EXPERT AGENT ONLINE" else "NEURAL CORE READY",
                color = TextMuted,
                style = MaterialTheme.typography.labelSmall
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Try: “Open WhatsApp”, “Take a screenshot”, “Search the web for weather”, or just ask anything.",
                color = TextDim,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.widthIn(max = 280.dp)
            )
        }
    }
}

@Composable
private fun MessageBubble(msg: ChatMessage) {
    val isUser = msg.sender == Sender.USER
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Text(
            if (isUser) "YOU" else "STONIC",
            color = TextMuted,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp, end = 4.dp)
        )
        Surface(
            color = if (isUser) Cyan else BgPanel,
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            border = if (isUser) null else androidx.compose.foundation.BorderStroke(1.dp, BorderSoft),
            modifier = Modifier.widthIn(max = 340.dp)
        ) {
            if (isUser) {
                Text(
                    msg.text,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            } else {
                when (msg.status) {
                    MessageStatus.STREAMING -> {
                        if (msg.text.isEmpty()) TypingDots(Modifier.padding(14.dp))
                        else MarkdownText(
                            markdown = msg.text + " ▍",
                            textColor = Text,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                    else -> MarkdownText(
                        markdown = msg.text,
                        textColor = Text,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TypingDots(modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf(0, 1, 2).forEach { _ ->
            Box(
                Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(TextDim)
            )
        }
    }
}

@Composable
private fun Chip(label: String, onClick: () -> Unit) {
    Surface(
        color = BgInput,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderFaint),
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        Text(
            label.uppercase(),
            color = TextDim,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun SmallPill(label: String, onClick: () -> Unit) {
    Surface(
        color = BgInput,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderFaint),
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        Text(
            label,
            color = TextDim,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun CircleButton(
    bg: Color,
    fg: Color,
    border: Color? = null,
    onClick: () -> Unit,
    content: @Composable (Color) -> Unit
) {
    Surface(
        color = bg,
        shape = CircleShape,
        border = border?.let { androidx.compose.foundation.BorderStroke(1.dp, it) },
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(
                role = androidx.compose.ui.semantics.Role.Button,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Box(contentAlignment = Alignment.Center) { content(fg) }
    }
}
