package com.stonicai.app.ui.chat

import android.Manifest
import android.content.Intent
import android.os.Build
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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stonicai.app.data.ChatMessage
import com.stonicai.app.data.MessageStatus
import com.stonicai.app.data.Models
import com.stonicai.app.data.Sender
import com.stonicai.app.ui.components.MarkdownText
import com.stonicai.app.ui.theme.StonicAccent
import com.stonicai.app.ui.theme.StonicBg
import com.stonicai.app.ui.theme.StonicBorder
import com.stonicai.app.ui.theme.StonicError
import com.stonicai.app.ui.theme.StonicInput
import com.stonicai.app.ui.theme.StonicSuccess
import com.stonicai.app.ui.theme.StonicSurface
import com.stonicai.app.ui.theme.StonicTextDim
import com.stonicai.app.ui.theme.StonicTextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onOpenSettings: () -> Unit,
    vm: ChatViewModel = viewModel()
) {
    val state by vm.messages.collectAsState()
    val settings by vm.settings.collectAsState()
    val isStreaming by vm.isStreaming.collectAsState()
    var input by rememberSaveable { mutableStateOf("") }
    var modelMenu by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val context = LocalContext.current

    val micPerm = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
            }
            runCatching { speechLauncher.launch(intent) }
        }
    }

    val speechLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        val text = matches?.firstOrNull()
        if (!text.isNullOrBlank()) {
            input = if (input.isBlank()) text else "$input $text"
        }
    }

    LaunchedEffect(state.size, state.lastOrNull()?.text?.length) {
        if (state.isNotEmpty()) listState.animateScrollToItem(state.lastIndex)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StonicBg)
            .statusBarsPadding()
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .background(StonicBg.copy(alpha = 0.85f))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isStreaming) StonicAccent else StonicSuccess)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        "Stonic OS",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        if (isStreaming) "ANALYZING" else "READY",
                        color = StonicTextDim,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box {
                    Surface(
                        color = StonicInput,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StonicBorder),
                        modifier = Modifier.clickable(
                            role = Role.Button,
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { modelMenu = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                Models.byId(settings.selectedModelId).displayName,
                                color = StonicTextDim,
                                style = MaterialTheme.typography.labelSmall
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("▾", color = StonicTextDim, fontSize = 11.sp)
                        }
                    }
                    DropdownMenu(
                        expanded = modelMenu,
                        onDismissRequest = { modelMenu = false },
                        modifier = Modifier.background(StonicSurface)
                    ) {
                        Models.ALL.forEach { m ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(m.displayName, color = Color.White)
                                        Text(
                                            m.provider.replaceFirstChar { it.uppercase() },
                                            color = StonicTextMuted,
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
                IconButton(onClick = { showClearDialog = true }) {
                    Text("C", color = StonicTextDim, fontSize = 16.sp, fontWeight = FontWeight.Black)
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = StonicTextDim)
                }
            }
        }

        HorizontalDivider(color = StonicBorder, thickness = 1.dp)

        if (showClearDialog) {
            AlertDialog(
                onDismissRequest = { showClearDialog = false },
                title = { Text("Clear conversation?") },
                text = { Text("This cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = { vm.clear(); showClearDialog = false }) {
                        Text("Clear", color = StonicError)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDialog = false }) { Text("Cancel") }
                },
                containerColor = StonicSurface
            )
        }

        // Messages
        Box(modifier = Modifier.weight(1f)) {
            if (state.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    items(state, key = { it.id }) { msg -> MessageBubble(msg) }
                }
            }
        }

        // Input
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(StonicBg)
        ) {
            HorizontalDivider(color = StonicBorder, thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .navigationBarsPadding()
                    .imePadding(),
                verticalAlignment = Alignment.Bottom
            ) {
                Surface(
                    color = StonicSurface,
                    shape = RoundedCornerShape(22.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StonicBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.padding(start = 6.dp, end = 6.dp, top = 4.dp, bottom = 4.dp)
                    ) {
                        OutlinedTextField(
                            value = input,
                            onValueChange = { input = it },
                            placeholder = {
                                Text(
                                    "Initiate command…",
                                    color = StonicTextMuted,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(max = 140.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = StonicAccent
                            ),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Default
                            ),
                            maxLines = 6
                        )

                        IconButton(
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
                                    runCatching { speechLauncher.launch(intent) }
                                } else {
                                    micPerm.launch(perm)
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = "Voice",
                                tint = if (isStreaming) StonicError else StonicTextDim
                            )
                        }
                    }
                }

                Spacer(Modifier.width(10.dp))

                val bg = if (isStreaming) StonicError else Color.White
                val fg = if (isStreaming) Color.White else Color.Black
                Surface(
                    color = bg,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .clickable(
                            role = Role.Button,
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            if (isStreaming) vm.stop()
                            else if (input.isNotBlank()) {
                                vm.send(input)
                                input = ""
                            }
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (isStreaming) Icons.Default.Stop else Icons.AutoMirrored.Filled.Send,
                            contentDescription = if (isStreaming) "Stop" else "Send",
                            tint = fg,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "✦",
                color = StonicTextMuted,
                fontSize = 32.sp
            )
            Spacer(Modifier.height(10.dp))
            Text("Stonic AI", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                "Your on-device AI command center.",
                color = StonicTextDim,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "Type or tap the mic to begin.",
                color = StonicTextMuted,
                style = MaterialTheme.typography.labelSmall
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
            color = StonicTextMuted,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp, end = 4.dp)
        )
        Surface(
            color = if (isUser) Color.White else StonicSurface,
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            border = if (isUser) null else androidx.compose.foundation.BorderStroke(1.dp, StonicBorder),
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            if (isUser) {
                Text(
                    msg.text,
                    color = Color.Black,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            } else {
                when (msg.status) {
                    MessageStatus.STREAMING -> {
                        if (msg.text.isEmpty()) {
                            TypingDots(modifier = Modifier.padding(14.dp))
                        } else {
                            MarkdownText(
                                markdown = msg.text + "▍",
                                textColor = Color.White,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }
                    else -> {
                        MarkdownText(
                            markdown = msg.text,
                            textColor = Color.White,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TypingDots(modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf(0, 1, 2).forEach { i ->
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(StonicTextDim)
            )
        }
    }
}

// Helper removed — settings are changed via vm.selectModel().
