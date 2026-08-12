package com.stonicai.app.nav

sealed class Destinations(val route: String) {
    data object Home : Destinations("home")
    data object Chat : Destinations("chat")
    data object Settings : Destinations("settings")
    data object Voice : Destinations("voice")
    data object Permissions : Destinations("permissions")
    data object Control : Destinations("control")
    data object Memory : Destinations("memory")
}
