package com.stonicai.app.ui.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.stonicai.app.accessibility.StonicAccessibilityService
import com.stonicai.app.ui.components.NeuralCore
import com.stonicai.app.ui.theme.BgBlack
import com.stonicai.app.ui.theme.BgPanel
import com.stonicai.app.ui.theme.BorderFaint
import com.stonicai.app.ui.theme.BorderSoft
import com.stonicai.app.ui.theme.Cyan
import com.stonicai.app.ui.theme.Ok
import com.stonicai.app.ui.theme.Text
import com.stonicai.app.ui.theme.TextDim
import com.stonicai.app.ui.theme.TextMuted

@Composable
fun PermissionGate(onContinue: () -> Unit) {
    val context = LocalContext.current
    var mic by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var notif by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
            else true
        )
    }
    var a11y by remember { mutableStateOf(StonicAccessibilityService.isReady()) }

    val micLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> mic = granted }
    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> notif = granted }

    // Refresh accessibility state when returning from system settings
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                a11y = StonicAccessibilityService.isReady()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(BgBlack)
            .statusBarsPadding()
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))
            NeuralCore(size = 120.dp)
            Spacer(Modifier.height(14.dp))
            Text("STONIC", color = Text, fontWeight = FontWeight.Black,
                fontSize = 22.sp, letterSpacing = 5.sp)
            Text(
                "POWER ACCESS",
                color = Cyan,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "Grant these permissions so Stonic can hear you, notify you, and control your apps.",
                color = TextDim,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(Modifier.height(18.dp))

            PermissionRow(
                icon = Icons.Default.Mic,
                title = "Microphone",
                subtitle = "Talk to Stonic and dictate messages.",
                granted = mic,
                onGrant = { micLauncher.launch(Manifest.permission.RECORD_AUDIO) }
            )
            PermissionRow(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                subtitle = "Alerts when background tasks finish.",
                granted = notif,
                onGrant = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            )
            PermissionRow(
                icon = Icons.Default.Shield,
                title = "Stonic Control",
                subtitle = "Open apps, press back/home, screenshots — no root needed.",
                granted = a11y,
                onGrant = {
                    context.startActivity(
                        Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            )

            Spacer(Modifier.weight(1f))

            val allSet = mic && notif && a11y
            Surface(
                color = if (allSet) Cyan else Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(16.dp),
                border = if (allSet) null else BorderStroke(1.dp, BorderSoft),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        // mark notif as "asked" so we don't re-prompt aggressively
                        onContinue()
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        if (allSet) "ENTER STONIC" else "CONTINUE",
                        color = if (allSet) Color.Black else Text,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 3.sp
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "You can change these anytime in Settings.",
                color = TextMuted,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun PermissionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    granted: Boolean,
    onGrant: () -> Unit
) {
    Surface(
        color = BgPanel,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, if (granted) Cyan.copy(alpha = 0.25f) else BorderFaint),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { if (!granted) onGrant() }
            )
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(42.dp)
                    .background(
                        if (granted) Ok.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.05f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (granted) Icons.Default.Check else icon,
                    title,
                    tint = if (granted) Ok else Cyan,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Text, fontWeight = FontWeight.Bold)
                Text(subtitle, color = TextDim, style = MaterialTheme.typography.bodyMedium)
            }
            if (!granted) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Cyan)
                        .clickable(onClick = onGrant)
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text("ALLOW", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            } else {
                Text("ON", color = Ok, fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}
