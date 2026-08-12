package com.stonicai.app.nav

sealed class Destinations(val route: String) {
    data object Chat : Destinations("chat")
    data object Settings : Destinations("settings")
    data object Onboarding : Destinations("onboarding")
}
