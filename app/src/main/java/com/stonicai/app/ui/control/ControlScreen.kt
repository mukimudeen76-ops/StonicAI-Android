package com.stonicai.app.ui.control

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardReturn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Settings as Cog
import androidx.compose.material.icons.filled.StackedBarChart
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.stonicai.app.accessibility.DeviceApps
import com.stonicai.app.accessibility.StonicAccessibilityService
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
fun ControlScreen(
    onBack: () -> Unit,
    onCommand: (String) -> Unit
) {
    val context = LocalContext.current
    val a11yOn = StonicAccessibilityService.isReady()
    var query by remember { mutableStateOf("") }
    val apps = remember(query) {
        if (query.isBlank()) DeviceApps.installedLaunchableApps(context).take(30)
        else DeviceApps.search(context, query, limit = 60)
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(BgBlack)
            .statusBarsPadding()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(58.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Text)
            }
            Column(Modifier.weight(1f)) {
                Text("Device Control", color = Text, fontWeight = FontWeight.Bold)
                Text(
                    if (a11yOn) "STONIC CONTROL ONLINE" else "STONIC CONTROL OFFLINE",
                    color = if (a11yOn) Cyan else Danger,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        HorizontalDivider(color = BorderFaint)

        if (!a11yOn) {
            Surface(
                color = Cyan.copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Cyan.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
                    .clickable {
                        context.startActivity(
                            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text("Enable Stonic Control", color = Text, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Tap to open Accessibility settings and turn on “Stonic Control”. " +
                            "This lets Stonic open apps, press Back/Home/Recents, take screenshots and tap/type — " +
                            "Android's equivalent of desktop control.",
                        color = TextDim,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Quick actions
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ActionTile("Home", Icons.Default.Home, Modifier.weight(1f)) { onCommand("home") }
            ActionTile("Back", Icons.Default.KeyboardReturn, Modifier.weight(1f)) { onCommand("back") }
            ActionTile("Recents", Icons.Default.StackedBarChart, Modifier.weight(1f)) {
                onCommand("recents")
            }
            ActionTile("Alerts", Icons.Default.Notifications, Modifier.weight(1f)) {
                onCommand("notifications")
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ActionTile("Screenshot", Icons.Default.PhoneAndroid, Modifier.weight(1f)) {
                onCommand("screenshot")
            }
            ActionTile("Quick Settings", Icons.Default.Cog, Modifier.weight(1f)) {
                onCommand("quick settings")
            }
            ActionTile("Apps", Icons.Default.Apps, Modifier.weight(1f)) {
                onCommand("recents")
            }
        }

        Spacer(Modifier.height(10.dp))
        Text(
            "INSTALLED APPS",
            color = TextMuted,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(6.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search apps…", color = TextMuted) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = BgInput,
                unfocusedContainerColor = BgInput,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Text,
                unfocusedTextColor = Text,
                cursorColor = Cyan
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
        )

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(apps, key = { it.packageName }) { app ->
                Surface(
                    color = BgPanel,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderFaint),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            StonicAccessibilityService.get()?.launchApp(app.packageName)
                                ?: run {
                                    val intent = context.packageManager
                                        .getLaunchIntentForPackage(app.packageName)
                                    if (intent != null) {
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(intent)
                                    }
                                }
                        }
                ) {
                    Row(
                        Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(40.dp)
                                .background(Cyan.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                app.label.take(1).uppercase(),
                                color = Cyan,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Spacer(Modifier.size(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(app.label, color = Text, fontWeight = FontWeight.SemiBold)
                            Text(
                                app.packageName,
                                color = TextMuted,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        Text("OPEN", color = Cyan, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionTile(label: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        color = BgPanel,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderFaint),
        modifier = modifier
            .height(78.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, label, tint = Cyan, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(6.dp))
            Text(
                label.uppercase(),
                color = TextDim,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
