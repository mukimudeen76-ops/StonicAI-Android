package com.stonicai.app.nav

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.stonicai.app.data.SettingsRepository
import com.stonicai.app.ui.chat.ChatScreen
import com.stonicai.app.ui.settings.SettingsScreen
import kotlinx.coroutines.flow.first

@Composable
fun StonicNavHost() {
    val nav = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    val repo = remember { SettingsRepository(context.applicationContext as Application) }
    val onboarded by repo.isOnboarded.collectAsState(initial = null)

    LaunchedEffect(Unit) {
        val seen = repo.isOnboarded.first()
        if (!seen) nav.navigate(Destinations.Onboarding.route) {
            popUpTo(Destinations.Chat.route) { inclusive = true }
        }
    }

    if (onboarded == null) return // brief splash

    NavHost(navController = nav, startDestination = Destinations.Chat.route) {
        composable(Destinations.Chat.route) {
            ChatScreen(onOpenSettings = { nav.navigate(Destinations.Settings.route) })
        }
        composable(Destinations.Settings.route) {
            SettingsScreen(onBack = { nav.popBackStack() })
        }
        composable(Destinations.Onboarding.route) {
            SettingsScreen(
                onBack = {},
                isOnboarding = true,
                onOnboardingDone = {
                    nav.navigate(Destinations.Chat.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
