package com.stonicai.app.ui.memory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stonicai.app.ui.theme.BgBlack
import com.stonicai.app.ui.theme.BgPanel
import com.stonicai.app.ui.theme.BorderFaint
import com.stonicai.app.ui.theme.Cyan
import com.stonicai.app.ui.theme.Text
import com.stonicai.app.ui.theme.TextDim
import com.stonicai.app.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryScreen(onBack: () -> Unit) {
    val items = listOf(
        "User language" to "Hindi / English (Hinglish)",
        "Device" to "Android phone — Stonic app",
        "Preferred model" to "Set in Settings",
        "Persona" to "Stonic (switch via the bottom dock)"
    )
    Column(Modifier.fillMaxSize().background(BgBlack).statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().height(58.dp).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Text)
            }
            Text("Core Memory", color = Text, fontWeight = FontWeight.Bold)
        }
        HorizontalDivider(color = BorderFaint)
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Surface(
                    color = BgPanel, shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, BorderFaint), modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Box(
                            Modifier.size(44.dp)
                                .background(Cyan.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Default.Memory, null, tint = Cyan) }
                        Spacer(Modifier.size(12.dp))
                        Text("Memory is empty.", color = Text, fontWeight = FontWeight.Bold)
                        Text(
                            "Long-press any AI reply to save it to Core Memory (coming next build). " +
                                "Stonic will remember facts about you across conversations.",
                            color = TextDim, style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            items(items.size) { idx ->
                val (k, v) = items[idx]
                Surface(
                    color = BgPanel, shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, BorderFaint), modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(k.uppercase(), color = TextMuted,
                            style = MaterialTheme.typography.labelSmall)
                        Spacer(Modifier.size(2.dp))
                        Text(v, color = Text, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
