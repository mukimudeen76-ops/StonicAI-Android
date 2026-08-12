package com.stonicai.app.data

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.stonicai.app.accessibility.DeviceApps
import com.stonicai.app.accessibility.StonicAccessibilityService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class AgentResult(
    val reply: String,
    val screenshot: File? = null
)

/**
 * Parses natural-language device commands the way the desktop app's skill
 * system does. Everything here runs on-device — no API key required for
 * the commands themselves.
 */
class StonicAgent(private val context: Context) {

    suspend fun handleIfDeviceCommand(text: String): AgentResult? = withContext(Dispatchers.IO) {
        val t = text.trim()
        val lower = t.lowercase()

        // ── Accessibility / system global actions ──────────────────────────
        if (lower in listOf("back", "go back", "peeche", "पीछे")) {
            return@withContext runA11y { it.goBack() } ?: a11yOff("pressed back")
        }
        if (lower in listOf("home", "ghar", "होम")) {
            return@withContext runA11y { it.goHome() } ?: a11yOff("went home")
        }
        if (lower in listOf("recents", "recent apps", "overview", "switch app")) {
            return@withContext runA11y { it.openRecents() } ?: a11yOff("opened recents")
        }
        if (lower in listOf("notifications", "notification", "panel")) {
            return@withContext runA11y { it.openNotifications() } ?: a11yOff("opened notifications")
        }
        if (lower in listOf("quick settings", "quick panel", "control center")) {
            return@withContext runA11y { it.openQuickSettings() } ?: a11yOff("opened quick settings")
        }
        if (lower in listOf("lock", "lock screen", "screen off")) {
            return@withContext runA11y { it.lockScreen() } ?: a11yOff("requested lock screen")
        }

        // ── Open app ───────────────────────────────────────────────────────
        val openMatch = OPEN_REGEX.find(lower)
        if (openMatch != null || lower.startsWith("open ") || lower.startsWith("launch ") ||
            lower.startsWith("kholo") || lower.startsWith("chalao") || lower.startsWith("खोलो")
        ) {
            val query = openMatch?.groupValues?.get(1)
                ?: t.removePrefixWord().lowercase()
            val label = withContext(Dispatchers.Main) {
                StonicAccessibilityService.get()?.openAppByName(query)
            }
            if (label != null) return@withContext AgentResult("Opened **$label**.")

            val apps = DeviceApps.search(context, query, limit = 6)
            if (apps.isNotEmpty()) {
                val launched = withContext(Dispatchers.Main) {
                    StonicAccessibilityService.get()?.launchApp(apps.first().packageName)
                } == true
                if (launched) return@withContext AgentResult("Opened **${apps.first().label}**.")
                // fallback: use intent without accessibility
                val intent = context.packageManager.getLaunchIntentForPackage(apps.first().packageName)
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    return@withContext AgentResult("Opened **${apps.first().label}**.")
                }
            }
            return@withContext AgentResult("I couldn't find an app matching \"$query\".")
        }

        // ── Screenshot ─────────────────────────────────────────────────────
        if (lower in listOf("screenshot", "screen capture", "take screenshot", "screen le lo")) {
            return@withContext takeScreenshot()
        }

        // ── Volume / media (Android Settings intents) ──────────────────────
        if (lower.contains("volume")) {
            val intent = Intent(Settings.ACTION_SOUND_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return@withContext AgentResult("Opened **Sound settings** — adjust the volume there.")
        }
        if (lower.contains("wifi")) {
            context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            return@withContext AgentResult("Opened **Wi-Fi settings**.")
        }
        if (lower.contains("bluetooth")) {
            context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            return@withContext AgentResult("Opened **Bluetooth settings**.")
        }
        if (lower.contains("battery")) {
            context.startActivity(Intent(Intent.ACTION_POWER_USAGE_SUMMARY).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            return@withContext AgentResult("Opened **Battery settings**.")
        }

        // ── Web search ─────────────────────────────────────────────────────
        if (lower.startsWith("search ") || lower.startsWith("google ") || lower.startsWith("web ")) {
            val q = t.removePrefixWord()
            val uri = Uri.parse("https://www.google.com/search?q=" + Uri.encode(q))
            context.startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            return@withContext AgentResult("Searching the web for **$q**.")
        }

        // ── Typing via accessibility ───────────────────────────────────────
        val typeMatch = TYPE_REGEX.find(lower)
        if (typeMatch != null) {
            val textToType = typeMatch.groupValues[1]
            val ok = withContext(Dispatchers.Main) {
                StonicAccessibilityService.get()?.typeIntoFocused(textToType)
            } == true
            return@withContext if (ok) AgentResult("Typed into the focused field.")
            else AgentResult("Tap a text field first and make sure Stonic Control is on in Accessibility settings.")
        }

        // ── Tap by text ────────────────────────────────────────────────────
        val tapMatch = TAP_REGEX.find(lower)
        if (tapMatch != null) {
            val label = tapMatch.groupValues[1]
            val ok = withContext(Dispatchers.Main) {
                StonicAccessibilityService.get()?.clickByText(label)
            } == true
            return@withContext if (ok) AgentResult("Tapped **$label**.")
            else AgentResult("I couldn't find a control labelled \"$label\".")
        }

        return@withContext null
    }

    private fun runA11y(block: (StonicAccessibilityService) -> Boolean): AgentResult? {
        val svc = StonicAccessibilityService.get() ?: return null
        val ok = block(svc)
        return if (ok) AgentResult("Done ✓")
        else AgentResult("The command was sent but the system rejected it.")
    }

    private fun a11yOff(action: String): AgentResult = AgentResult(
        "I $action, but full control needs **Stonic Control** enabled in " +
            "**Settings → Accessibility**. Tap the shield icon at the top to enable it."
    )

    private suspend fun takeScreenshot(): AgentResult {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return AgentResult("Screenshots require Android 11 or newer.")
        }
        val svc = StonicAccessibilityService.get()
            ?: return AgentResult("Screenshots need **Stonic Control** enabled in Accessibility settings.")
        val file = kotlinx.coroutines.suspendCancellableCoroutine<File?> { cont ->
            svc.screenshot { if (cont.isActive) cont.resume(it) {} }
        }
        return if (file != null) AgentResult("Screenshot captured.", screenshot = file)
        else AgentResult("Couldn't capture a screenshot right now.")
    }

    fun hasNotificationPermission(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        else true

    companion object {
        private val OPEN_REGEX = Regex("(?:open|launch|kholo|chalao|खोलो)\\s+(.+)")
        private val TYPE_REGEX = Regex("(?:type|likho|लिखो)\\s+(.+)")
        private val TAP_REGEX = Regex("(?:tap|click|press|dabao)\\s+(?:on\\s+)?(.+)")

        private fun String.removePrefixWord(): String =
            substringAfter(' ').trim().ifEmpty { this }

        // Convenience for coroutine resume without the structured import
        private inline fun <T> kotlinx.coroutines.CancellableContinuation<T>.resume(
            value: T,
            noinline onCancellation: ((cause: Throwable) -> Unit)?
        ) = this.resume(value) { onCancellation?.invoke(it) }
    }
}
