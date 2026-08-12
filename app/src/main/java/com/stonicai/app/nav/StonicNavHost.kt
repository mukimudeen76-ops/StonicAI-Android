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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.stonicai.app.data.SettingsRepository
import com.stonicai.app.ui.chat.ChatScreen
import com.stonicai.app.ui.control.ControlScreen
import com.stonicai.app.ui.home.HomeScreen
import com.stonicai.app.ui.memory.MemoryScreen
import com.stonicai.app.ui.settings.SettingsScreen
import com.stonicai.app.ui.theme.BgBlack
import com.stonicai.app.ui.theme.Cyan

@Composable
fun StonicNavHost() {
    val context = LocalContext.current
    val repo = remember { SettingsRepository(context.applicationContext as Application) }
    val onboarded by repo.isOnboarded.collectAsState(initial = null)
    if (onboarded == null) { Splash(); return }

    val start = if (onboarded == true) Destinations.Home.route else Destinations.Onboarding.route
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = start) {
        composable(Destinations.Home.route) {
            HomeScreen(
                onOpenMemory = { nav.navigate(Destinations.Memory.route) },
                onOpenSettings = { nav.navigate(Destinations.Settings.route) },
                onOpenSoul = { nav.navigate(Destinations.Settings.route) },
                onOpenSkills = { nav.navigate(Destinations.Control.route) },
                onOpenDesktop = { nav.navigate(Destinations.Control.route) },
                onStartChat = { text ->
                    nav.currentBackStackEntry
                        ?.savedStateHandle?.set("pending_command", text)
                    nav.navigate(Destinations.Chat.route)
                }
            )
        }
        composable(Destinations.Chat.route) {
            val pending = nav.currentBackStackEntry
                ?.savedStateHandle?.get<String>("pending_command")
            nav.currentBackStackEntry?.savedStateHandle?.remove<String>("pending_command")
            ChatScreen(onBack = { nav.popBackStack() }, pendingCommand = pending)
        }
        composable(Destinations.Settings.route) {
            SettingsScreen(onBack = { nav.popBackStack() })
        }
        composable(Destinations.Onboarding.route) {
            SettingsScreen(
                onBack = {},
                isOnboarding = true,
                onOnboardingDone = {
                    nav.navigate(Destinations.Home.route) { popUpTo(0) { inclusive = true } }
                }
            )
        }
        composable(Destinations.Control.route) {
            ControlScreen(onBack = { nav.popBackStack() }, onCommand = { nav.popBackStack() })
        }
        composable(Destinations.Memory.route) {
            MemoryScreen(onBack = { nav.popBackStack() })
        }
    }
}

@Composable
private fun Splash() {
    val t = rememberInfiniteTransition(label = "s")
    val scale by t.animateFloat(
        0.85f, 1.15f,
        infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "pulse"
    )
    Box(Modifier.fillMaxSize().background(BgBlack), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(56.dp).scale(scale).background(Cyan, CircleShape))
            Spacer(Modifier.height(18.dp))
            Text("STONIC", color = Color.White, fontSize = 22.sp,
                fontWeight = FontWeight.Black, letterSpacing = 4.sp)
            Spacer(Modifier.height(6.dp))
            Text("Initializing…", color = Color.White.copy(alpha = 0.4f),
                fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}
