package com.stonicai.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stonicai.app.data.Models
import com.stonicai.app.ui.theme.StonicAccent
import com.stonicai.app.ui.theme.StonicBg
import com.stonicai.app.ui.theme.StonicBorder
import com.stonicai.app.ui.theme.StonicError
import com.stonicai.app.ui.theme.StonicInput
import com.stonicai.app.ui.theme.StonicSurface
import com.stonicai.app.ui.theme.StonicTextDim
import com.stonicai.app.ui.theme.StonicTextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    isOnboarding: Boolean = false,
    onOnboardingDone: () -> Unit = {},
    vm: SettingsViewModel = viewModel()
) {
    val s by vm.settings.collectAsState()

    var openai by remember(s.keys.openai) { mutableStateOf(s.keys.openai) }
    var anthropic by remember(s.keys.anthropic) { mutableStateOf(s.keys.anthropic) }
    var google by remember(s.keys.google) { mutableStateOf(s.keys.google) }
    var groq by remember(s.keys.groq) { mutableStateOf(s.keys.groq) }
    var systemPrompt by remember(s.systemPrompt) { mutableStateOf(s.systemPrompt) }
    var tts by remember(s.ttsEnabled) { mutableStateOf(s.ttsEnabled) }
    var haptics by remember(s.hapticsEnabled) { mutableStateOf(s.hapticsEnabled) }

    // Auto-save on changes (debounced feel via LaunchedEffect keys)
    LaunchedEffect(systemPrompt) {
        kotlinx.coroutines.delay(400)
        if (systemPrompt != s.systemPrompt) vm.save(systemPrompt = systemPrompt)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isOnboarding) "Setup" else "Settings",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (!isOnboarding) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                    }
                },
                actions = {
                    if (isOnboarding) {
                        TextButton(onClick = {
                            vm.save(
                                openai = openai,
                                anthropic = anthropic,
                                google = google,
                                groq = groq,
                                tts = tts
                            )
                            vm.completeOnboarding()
                            onOnboardingDone()
                        }) { Text("Done", color = StonicAccent, fontWeight = FontWeight.Bold) }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StonicBg)
            )
        },
        containerColor = StonicBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            if (isOnboarding) {
                SectionTitle("Welcome to Stonic")
                Text(
                    "Stonic runs natively on your Android. Add at least one AI provider " +
                        "key below — free options like Google AI Studio and Groq work great.",
                    color = StonicTextDim,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Card {
                SectionTitle("Model")
                Spacer(Modifier.height(6.dp))
                ModelChips(
                    selectedId = s.selectedModelId,
                    onSelect = { vm.save(modelId = it) }
                )
            }

            Card {
                SectionTitle("API Keys")
                Spacer(Modifier.height(6.dp))
                KeyField(
                    label = "OpenAI (GPT-4o / GPT-4o Mini)",
                    value = openai,
                    onChange = { openai = it; vm.save(openai = it) },
                    placeholder = "sk-..."
                )
                KeyField(
                    label = "Anthropic (Claude 3.5 Sonnet)",
                    value = anthropic,
                    onChange = { anthropic = it; vm.save(anthropic = it) },
                    placeholder = "sk-ant-..."
                )
                KeyField(
                    label = "Google AI Studio (Gemini 1.5)",
                    value = google,
                    onChange = { google = it; vm.save(google = it) },
                    placeholder = "AIza..."
                )
                KeyField(
                    label = "Groq (Llama 3.1 — free tier)",
                    value = groq,
                    onChange = { groq = it; vm.save(groq = it) },
                    placeholder = "gsk_..."
                )
                Text(
                    "Keys are stored only on this device (encrypted DataStore) and sent directly to each provider. " +
                        "Groq/Google offer generous free tiers.",
                    color = StonicTextMuted,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Card {
                SectionTitle("Assistant personality")
                OutlinedTextField(
                    value = systemPrompt,
                    onValueChange = { systemPrompt = it },
                    placeholder = {
                        Text(
                            "You are Stonic, a helpful AI…",
                            color = StonicTextMuted
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors()
                )
            }

            Card {
                SectionTitle("Voice & feedback")
                ToggleRow(
                    title = "Speak responses",
                    subtitle = "Stonic reads answers aloud using Android TTS.",
                    checked = tts,
                    onChange = { tts = it; vm.save(tts = it) }
                )
                HorizontalDivider(color = StonicBorder)
                ToggleRow(
                    title = "Haptic feedback",
                    subtitle = "Vibration on send, errors, and voice.",
                    checked = haptics,
                    onChange = { haptics = it; vm.save(haptics = it) }
                )
            }

            if (!isOnboarding) {
                Card {
                    SectionTitle("About")
                    Text(
                        "Stonic AI for Android · v1.0.0\n" +
                            "Native Kotlin / Jetpack Compose build.\n" +
                            "No desktop required — all AI runs via provider APIs.",
                        color = StonicTextDim,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(10.dp))
                    TextButton(onClick = { vm.resetAll() }) {
                        Text("Reset all data", color = StonicError)
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun Card(content: @Composable () -> Unit) {
    Surface(
        color = StonicSurface,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) { content() }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text.uppercase(),
        color = StonicTextMuted,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.sp
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KeyField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    placeholder: String
) {
    Column(Modifier.padding(vertical = 6.dp)) {
        Text(
            label,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            placeholder = { Text(placeholder, color = StonicTextMuted) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            shape = RoundedCornerShape(12.dp),
            colors = fieldColors(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, color = StonicTextDim, style = MaterialTheme.typography.labelSmall)
        }
        Spacer(Modifier.size(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = StonicAccent,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = StonicInput
            )
        )
    }
}

@Composable
private fun ModelChips(selectedId: String, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Models.ALL.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { m ->
                    val selected = m.id == selectedId
                    Surface(
                        color = if (selected) StonicAccent else StonicInput,
                        shape = RoundedCornerShape(12.dp),
                        onClick = { onSelect(m.id) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            m.displayName,
                            color = if (selected) Color.Black else StonicTextDim,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(horizontal = 10.dp, vertical = 12.dp)
                                .fillMaxWidth()
                        )
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun fieldColors() = androidx.compose.material3.TextFieldDefaults.colors(
    focusedContainerColor = StonicInput,
    unfocusedContainerColor = StonicInput,
    disabledContainerColor = StonicInput,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = StonicAccent,
    focusedLabelColor = StonicTextDim,
    unfocusedLabelColor = StonicTextDim
)

