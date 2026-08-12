package com.stonicai.app.nav

import android.app.Application
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.stonicai.app.data.SettingsRepository
import com.stonicai.app.ui.chat.ChatScreen
import com.stonicai.app.ui.settings.SettingsScreen
import com.stonicai.app.ui.theme.StonicAccent
import com.stonicai.app.ui.theme.StonicBg

@Composable
fun StonicNavHost() {
    val context = LocalContext.current
    val repo = remember { SettingsRepository(context.applicationContext as Application) }

    // Read onboarding flag BEFORE building the graph so we can choose the
    // correct start destination. Previously this returned early and tried to
    // navigate from a LaunchedEffect before the NavHost graph existed, which
    // crashed with "Navigation graph has not been set for NavController".
    val onboarded by repo.isOnboarded.collectAsState(initial = null)

    if (onboarded == null) {
        SplashScreen()
        return
    }

    val start = if (onboarded == true) Destinations.Chat.route else Destinations.Onboarding.route
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = start) {
        chatGraph(
            onOpenSettings = { nav.navigate(Destinations.Settings.route) }
        )
        settingsGraph(onBack = { nav.popBackStack() })
        onboardingGraph(
            onDone = {
                nav.navigate(Destinations.Chat.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }
}

private fun NavGraphBuilder.chatGraph(onOpenSettings: () -> Unit) {
    composable(Destinations.Chat.route) {
        ChatScreen(onOpenSettings = onOpenSettings)
    }
}

private fun NavGraphBuilder.settingsGraph(onBack: () -> Unit) {
    composable(Destinations.Settings.route) {
        SettingsScreen(onBack = onBack)
    }
}

private fun NavGraphBuilder.onboardingGraph(onDone: () -> Unit) {
    composable(Destinations.Onboarding.route) {
        SettingsScreen(onBack = {}, isOnboarding = true, onOnboardingDone = onDone)
    }
}

@Composable
private fun SplashScreen() {
    val transition = rememberInfiniteTransition(label = "splash")
    val scale by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StonicBg),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .scale(scale)
                    .background(StonicAccent, CircleShape)
            )
            Spacer(Modifier.height(20.dp))
            Text(
                "STONIC",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Initializing…",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
