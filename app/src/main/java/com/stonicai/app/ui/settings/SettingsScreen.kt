package com.stonicai.app.ui.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Mic
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stonicai.app.data.Models
import com.stonicai.app.data.Persona
import com.stonicai.app.ui.theme.BgBlack
import com.stonicai.app.ui.theme.BgInput
import com.stonicai.app.ui.theme.BgPanel
import com.stonicai.app.ui.theme.BorderFaint
import com.stonicai.app.ui.theme.Cyan
import com.stonicai.app.ui.theme.Danger
import com.stonicai.app.ui.theme.Text
import com.stonicai.app.ui.theme.TextDim
import com.stonicai.app.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenVoice: () -> Unit = {},
    isOnboarding: Boolean = false,
    onOnboardingDone: () -> Unit = {},
    vm: SettingsViewModel = viewModel()
) {
    val s by vm.settings.collectAsState()
    var openai by remember(s.keys.openai) { mutableStateOf(s.keys.openai) }
    var anthropic by remember(s.keys.anthropic) { mutableStateOf(s.keys.anthropic) }
    var google by remember(s.keys.google) { mutableStateOf(s.keys.google) }
    var groq by remember(s.keys.groq) { mutableStateOf(s.keys.groq) }
    var sys by remember(s.systemPrompt) { mutableStateOf(s.systemPrompt) }
    var tts by remember(s.ttsEnabled) { mutableStateOf(s.ttsEnabled) }
    var haptics by remember(s.hapticsEnabled) { mutableStateOf(s.hapticsEnabled) }
    var expert by remember(s.expertMode) { mutableStateOf(s.expertMode) }

    LaunchedEffect(sys) {
        kotlinx.coroutines.delay(400)
        if (sys != s.systemPrompt) vm.save(systemPrompt = sys)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isOnboarding) "Setup Stonic" else "Settings",
                        color = Text, fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (!isOnboarding) IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Text)
                    }
                },
                actions = {
                    if (isOnboarding) TextButton(onClick = {
                        vm.save(
                            openai = openai, anthropic = anthropic,
                            google = google, groq = groq, tts = tts
                        )
                        vm.completeOnboarding()
                        onOnboardingDone()
                    }) { Text("Next", color = Cyan, fontWeight = FontWeight.Bold) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgBlack)
            )
        },
        containerColor = BgBlack
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isOnboarding) Card {
                Title("Welcome to Stonic")
                Body("Add a key from Google AI Studio or Groq (both free) to start chatting, or skip and use only device commands like “open WhatsApp”, “screenshot”, “home”.")
            }

            Card {
                Title("Model Engine")
                Spacer(Modifier.height(8.dp))
                Models.ALL.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 8.dp)) {
                        row.forEach { m ->
                            val selected = m.id == s.selectedModelId
                            Surface(
                                color = if (selected) Cyan else BgInput,
                                shape = RoundedCornerShape(12.dp),
                                onClick = { vm.save(modelId = m.id) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    m.displayName,
                                    color = if (selected) Color.Black else TextDim,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }

            Card {
                Title("Soul")
                Spacer(Modifier.height(8.dp))
                Persona.values().toList().chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 8.dp)) {
                        row.forEach { p ->
                            val selected = p.id == s.personaId
                            Surface(
                                color = if (selected) Cyan else BgInput,
                                shape = RoundedCornerShape(12.dp),
                                onClick = { vm.save(personaId = p.id) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(Modifier.padding(10.dp)) {
                                    Text("${p.emoji} ${p.displayName}",
                                        color = if (selected) Color.Black else Text,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelMedium)
                                    Text(p.tagline,
                                        color = if (selected) Color.Black.copy(0.7f) else TextMuted,
                                        style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }

            Card {
                Title("API Keys")
                Spacer(Modifier.height(6.dp))
                Key("OpenAI (GPT-4o)", openai, "sk-...") { openai = it; vm.save(openai = it) }
                Key("Anthropic (Claude)", anthropic, "sk-ant-...") { anthropic = it; vm.save(anthropic = it) }
                Key("Google AI Studio (Gemini)", google, "AIza...") { google = it; vm.save(google = it) }
                Key("Groq (Llama, free)", groq, "gsk_...") { groq = it; vm.save(groq = it) }
                Spacer(Modifier.height(6.dp))
                Body("Keys stay on this device and go directly to each provider over HTTPS.")
            }

            Card {
                NavRow(Icons.Default.Mic, "Voice & Speech", "Volume, speed, pitch, voice picker", onOpenVoice)
                HorizontalDivider(color = BorderFaint, modifier = Modifier.padding(vertical = 4.dp))
                Toggle("Speak responses", "Stonic reads replies aloud.", tts) {
                    tts = it; vm.save(tts = it)
                }
                HorizontalDivider(color = BorderFaint)
                Toggle("Haptic feedback", "Vibration on send/errors.", haptics) {
                    haptics = it; vm.save(haptics = it)
                }
                HorizontalDivider(color = BorderFaint)
                Toggle("Expert Agent", "Faster, more tactical replies.", expert) {
                    expert = it; vm.save(expert = it)
                }
            }

            Card {
                Title("Custom system prompt (optional)")
                OutlinedTextField(
                    value = sys, onValueChange = { sys = it },
                    placeholder = { Text("Override the soul's instructions…", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth().height(130.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = fields()
                )
            }

            if (!isOnboarding) Card {
                Title("About")
                Body("Stonic AI for Android · v1.1.0\nNative Kotlin / Jetpack Compose with on-device control.")
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { vm.resetAll() }) {
                    Text("Reset all data", color = Danger)
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable private fun Card(content: @Composable () -> Unit) {
    Surface(color = BgPanel, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) { content() }
    }
}
@Composable private fun Title(t: String) = Text(t.uppercase(), color = TextMuted,
    style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
@Composable private fun Body(t: String) = Text(t, color = TextDim, style = MaterialTheme.typography.bodyMedium)

@Composable private fun Key(label: String, value: String, ph: String, onChange: (String) -> Unit) {
    Column(Modifier.padding(vertical = 6.dp)) {
        Text(label, color = Text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value, onValueChange = onChange,
            placeholder = { Text(ph, color = TextMuted) },
            singleLine = true, visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            shape = RoundedCornerShape(12.dp), colors = fields(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable private fun Toggle(title: String, sub: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Text, style = MaterialTheme.typography.bodyLarge)
            Text(sub, color = TextMuted, style = MaterialTheme.typography.labelSmall)
        }
        Switch(checked = checked, onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black, checkedTrackColor = Cyan,
                uncheckedThumbColor = Text, uncheckedTrackColor = BgInput
            ))
    }
}

@Composable private fun NavRow(icon: ImageVector, title: String, sub: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null, onClick = onClick
        ).padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(36.dp).background(Cyan.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center) {
            Icon(icon, title, tint = Cyan, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.size(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Text, fontWeight = FontWeight.SemiBold)
            Text(sub, color = TextMuted, style = MaterialTheme.typography.labelSmall)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TextMuted)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun fields() = androidx.compose.material3.TextFieldDefaults.colors(
    focusedContainerColor = BgInput, unfocusedContainerColor = BgInput,
    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
    focusedTextColor = Text, unfocusedTextColor = Text, cursorColor = Cyan
)
