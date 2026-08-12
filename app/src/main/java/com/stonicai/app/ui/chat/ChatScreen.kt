package com.stonicai.app.ui.chat

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.stonicai.app.data.ChatMessage
import com.stonicai.app.data.MessageStatus
import com.stonicai.app.data.Sender
import com.stonicai.app.ui.components.MarkdownText
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onBack: () -> Unit,
    pendingCommand: String? = null,
    vm: ChatViewModel = viewModel()
) {
    val state by vm.messages.collectAsState()
    val isStreaming by vm.isStreaming.collectAsState()
    val context = LocalContext.current
    val keyboard = LocalSoftwareKeyboardController.current
    var input by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(pendingCommand) {
        if (!pendingCommand.isNullOrBlank()) {
            input = pendingCommand
            vm.send(pendingCommand)
            input = ""
        }
    }
    LaunchedEffect(state.size, state.lastOrNull()?.text?.length) {
        if (state.isNotEmpty()) listState.animateScrollToItem(state.lastIndex)
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

    Column(
        Modifier.fillMaxSize().background(BgBlack).statusBarsPadding()
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Text)
            }
            Column {
                Text("STONIC", color = Text, fontWeight = FontWeight.Black,
                    fontSize = 13.sp, letterSpacing = 3.sp)
                Text(
                    if (isStreaming) "ANALYZING" else "COMMUNICATION LINK STABLE",
                    color = if (isStreaming) TextDim else Cyan,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        HorizontalDivider(color = BorderFaint)

        Box(Modifier.weight(1f)) {
            if (state.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Awaiting command…", color = TextMuted,
                        style = MaterialTheme.typography.labelLarge)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) { items(state, key = { it.id }) { Bubble(it) } }
            }
        }

        Surface(
            color = BgInput.copy(alpha = 0.6f),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, BorderSoft),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .navigationBarsPadding()
                .imePadding()
        ) {
            Row(Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = input, onValueChange = { input = it },
                    placeholder = {
                        Text("MESSAGE STONIC…", color = TextMuted,
                            fontWeight = FontWeight.Black, fontSize = 10.sp)
                    },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Text, unfocusedTextColor = Text,
                        cursorColor = Cyan
                    ),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Default
                    ),
                    maxLines = 6
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
                Spacer(Modifier.size(6.dp))
                Box(
                    Modifier.size(44.dp).clip(CircleShape)
                        .background(if (isStreaming) Danger else Cyan)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (isStreaming) vm.stop()
                            else if (input.isNotBlank()) { vm.send(input); input = ""; keyboard?.hide() }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(if (isStreaming) Icons.Default.Stop else Icons.AutoMirrored.Filled.Send,
                        "Send", tint = Color.Black, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun Bubble(msg: ChatMessage) {
    val user = msg.sender == Sender.USER
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = if (user) Alignment.End else Alignment.Start
    ) {
        Text(
            if (user) "YOU" else "STONIC",
            color = TextMuted, style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp, end = 4.dp)
        )
        Surface(
            color = if (user) Cyan else BgPanel,
            shape = RoundedCornerShape(
                topStart = 18.dp, topEnd = 18.dp,
                bottomStart = if (user) 18.dp else 4.dp,
                bottomEnd = if (user) 4.dp else 18.dp
            ),
            border = if (user) null else BorderStroke(1.dp, BorderSoft),
            modifier = Modifier.widthIn(max = 340.dp)
        ) {
            if (user) Text(
                msg.text, color = Color.Black, fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            ) else when (msg.status) {
                MessageStatus.STREAMING -> if (msg.text.isEmpty())
                    Text("● ● ●", color = TextDim, modifier = Modifier.padding(14.dp))
                else MarkdownText(msg.text + " ▍", textColor = Text,
                    modifier = Modifier.padding(14.dp))
                else -> MarkdownText(msg.text, textColor = Text,
                    modifier = Modifier.padding(14.dp))
            }
        }
    }
}
