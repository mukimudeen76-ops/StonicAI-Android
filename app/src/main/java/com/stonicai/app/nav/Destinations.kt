package com.stonicai.app.nav

sealed class Destinations(val route: String) {
    data object Chat : Destinations("chat")
    data object Settings : Destinations("settings")
    data object Onboarding : Destinations("onboarding")
    data object Control : Destinations("control")
    data object Memory : Destinations("memory")
}
