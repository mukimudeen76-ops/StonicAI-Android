package com.stonicai.app.ui.settings

import android.app.Application
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stonicai.app.data.SettingsRepository
import com.stonicai.app.data.StonicSettings
import com.stonicai.app.tts.StonicTts
import com.stonicai.app.tts.StonicVoice
import com.stonicai.app.ui.chat.ServiceLocator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.stonicai.app.ui.theme.BgBlack
import com.stonicai.app.ui.theme.BgInput
import com.stonicai.app.ui.theme.BgPanel
import com.stonicai.app.ui.theme.BorderFaint
import com.stonicai.app.ui.theme.BorderSoft
import com.stonicai.app.ui.theme.Cyan
import com.stonicai.app.ui.theme.Text
import com.stonicai.app.ui.theme.TextDim
import com.stonicai.app.ui.theme.TextMuted

class VoiceViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ServiceLocator.repo(app)
    val tts = ServiceLocator.tts(app)
    val settings: StateFlow<StonicSettings> =
        repo.settings.stateIn(viewModelScope, SharingStarted.Eagerly, StonicSettings())
    private var voices: List<StonicVoice> = emptyList()

    fun loadVoices(cb: (List<StonicVoice>) -> Unit) {
        if (voices.isNotEmpty()) { cb(voices); return }
        tts.setOnVoicesReady { voices = it; cb(it) }
    }

    fun saveVolume(v: Float) = viewModelScope.launch { repo.save(ttsVolume = v) }
    fun saveRate(v: Float) = viewModelScope.launch { repo.save(ttsRate = v) }
    fun savePitch(v: Float) = viewModelScope.launch { repo.save(ttsPitch = v) }
    fun setVoice(name: String) = viewModelScope.launch { repo.save(ttsVoice = name) }
    fun setTts(on: Boolean) = viewModelScope.launch { repo.save(tts = on) }

    fun test() = tts.speak(
        "Stonic online. All systems nominal. Neural link stable. Awaiting your command, sir."
    )
    fun stop() = tts.stop()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceScreen(onBack: () -> Unit, vm: VoiceViewModel = viewModel()) {
    val s by vm.settings.collectAsState(initial = com.stonicai.app.data.StonicSettings())
    var voices by remember { mutableStateOf<List<StonicVoice>>(emptyList()) }

    LaunchedEffect(Unit) { vm.loadVoices { voices = it } }

    Column(
        Modifier
            .fillMaxSize()
            .background(BgBlack)
            .statusBarsPadding()
    ) {
        TopAppBar(
            title = {
                Text("Voice & Speech", color = Text, fontWeight = FontWeight.Bold)
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Text)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BgBlack)
        )
        HorizontalDivider(color = BorderFaint)

        LazyColumn(Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
            item {
                Surface(
                    color = BgPanel,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, BorderSoft)
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(40.dp)
                                    .background(Cyan.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) { Icon(Icons.Default.VolumeUp, null, tint = Cyan) }
                            Spacer(Modifier.size(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Stonic speaks replies", color = Text, fontWeight = FontWeight.Bold)
                                Text("High-quality Android TTS", color = TextDim,
                                    style = MaterialTheme.typography.bodyMedium)
                            }
                            Switch(
                                checked = s.ttsEnabled,
                                onCheckedChange = { vm.setTts(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = Cyan,
                                    uncheckedThumbColor = Text,
                                    uncheckedTrackColor = BgInput
                                )
                            )
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
            }

            item {
                SliderCard("Volume", "${(s.ttsVolume * 100).toInt()}%", s.ttsVolume, 0f..1.5f) {
                    vm.saveVolume(it)
                }
            }
            item {
                SliderCard("Speed", "%.2fx".format(s.ttsRate), s.ttsRate, 0.5f..2.0f) {
                    vm.saveRate(it)
                }
            }
            item {
                SliderCard("Pitch", "%.2f".format(s.ttsPitch), s.ttsPitch, 0.5f..2.0f) {
                    vm.savePitch(it)
                }
            }

            item {
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        color = Cyan, shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { vm.test() }
                    ) {
                        Row(
                            Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PlayArrow, "Test", tint = Color.Black)
                            Spacer(Modifier.size(6.dp))
                            Text("TEST VOICE", color = Color.Black, fontWeight = FontWeight.Black,
                                fontSize = 11.sp, letterSpacing = 2.sp)
                        }
                    }
                    Surface(
                        color = BgInput, shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, BorderFaint),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { vm.stop() }
                    ) {
                        Row(
                            Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Stop, "Stop", tint = Text)
                            Spacer(Modifier.size(6.dp))
                            Text("STOP", color = Text, fontWeight = FontWeight.Black,
                                fontSize = 11.sp, letterSpacing = 2.sp)
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))
            }

            item {
                Text(
                    "AVAILABLE VOICES",
                    color = TextMuted,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(8.dp))
            }

            if (voices.isEmpty()) {
                item {
                    Surface(color = BgPanel, shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, BorderFaint)) {
                        Text("Loading voices…", color = TextDim,
                            modifier = Modifier.padding(18.dp))
                    }
                }
            } else {
                items(voices, key = { it.name }) { v ->
                    val selected = s.ttsVoice == v.name ||
                        (s.ttsVoice.isBlank() && v.name == voices.firstOrNull()?.name)
                    Surface(
                        color = if (selected) Cyan.copy(alpha = 0.08f) else BgPanel,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            1.dp,
                            if (selected) Cyan.copy(alpha = 0.4f) else BorderFaint
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                vm.setVoice(v.name)
                                vm.test()
                            }
                    ) {
                        Row(
                            Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(v.name.substringAfter(";").substringBefore("="),
                                    color = Text, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "${v.language} · ${v.quality}" +
                                        if (v.isNetwork) " · network" else "",
                                    color = TextMuted,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            if (selected) Text("ACTIVE", color = Cyan,
                                fontWeight = FontWeight.Black, fontSize = 9.sp,
                                letterSpacing = 1.5.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SliderCard(
    title: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit
) {
    Surface(
        color = BgPanel,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, BorderFaint),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = Text, fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f))
                Text(valueText, color = Cyan, fontWeight = FontWeight.Black,
                    fontSize = 12.sp, letterSpacing = 1.sp)
            }
            Slider(
                value = value,
                onValueChange = onChange,
                valueRange = range,
                colors = SliderDefaults.colors(
                    thumbColor = Cyan,
                    activeTrackColor = Cyan,
                    inactiveTrackColor = BgInput
                )
            )
        }
    }
}
