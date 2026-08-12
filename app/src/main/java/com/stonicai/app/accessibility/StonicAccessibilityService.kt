package com.stonicai.app.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.accessibilityservice.TakeScreenshotCallback
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Path
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors

class StonicAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.i(TAG, "Stonic Accessibility connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) { /* no-op */ }

    override fun onInterrupt() { /* no-op */ }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    // ── Global actions (work on every API level we support) ───────────────
    fun goBack(): Boolean = performGlobalAction(GLOBAL_ACTION_BACK)
    fun goHome(): Boolean = performGlobalAction(GLOBAL_ACTION_HOME)
    fun openRecents(): Boolean = performGlobalAction(GLOBAL_ACTION_RECENTS)
    fun openNotifications(): Boolean = performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
    fun openQuickSettings(): Boolean = performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
    fun openPowerDialog(): Boolean = performGlobalAction(GLOBAL_ACTION_POWER_DIALOG)

    fun lockScreen(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
        }
        return performGlobalAction(GLOBAL_ACTION_POWER_DIALOG)
    }

    // ── App launching / stopping ──────────────────────────────────────────
    fun launchApp(packageName: String): Boolean {
        return try {
            val intent = packageManager.getLaunchIntentForPackage(packageName) ?: return false
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            true
        } catch (t: Throwable) {
            Log.e(TAG, "launchApp failed", t)
            false
        }
    }

    fun openAppByName(query: String): String? {
        val apps = DeviceApps.installedLaunchableApps(this)
        val q = query.trim().lowercase()
        val match = apps.firstOrNull {
            it.label.lowercase().contains(q) || it.packageName.contains(q)
        } ?: return null
        return if (launchApp(match.packageName)) match.label else null
    }

    // ── Gestures (tap / swipe) ────────────────────────────────────────────
    fun tap(x: Float, y: Float): Boolean {
        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0, 60)
        return dispatchGesture(GestureDescription.Builder().addStroke(stroke).build(), null, null)
    }

    fun swipe(x1: Float, y1: Float, x2: Float, y2: Float, durationMs: Long = 300): Boolean {
        val path = Path().apply { moveTo(x1, y1); lineTo(x2, y2) }
        val stroke = GestureDescription.StrokeDescription(path, 0, durationMs)
        return dispatchGesture(GestureDescription.Builder().addStroke(stroke).build(), null, null)
    }

    // ── Text input into the currently focused field ───────────────────────
    fun typeIntoFocused(text: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT) ?: return false
        val args = android.os.Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        }
        return focused.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
    }

    // ── Find and click a UI element by its visible text ───────────────────
    fun clickByText(text: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val nodes = root.findAccessibilityNodeInfosByText(text)
        for (n in nodes) {
            var node: AccessibilityNodeInfo? = n
            while (node != null) {
                if (node.isClickable) {
                    return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
                node = node.parent
            }
        }
        return false
    }

    // ── Screenshot (Android 11 / API 30+, no overlay permission needed) ──
    @RequiresApi(Build.VERSION_CODES.R)
    fun screenshot(callback: (File?) -> Unit) {
        val executor = Executors.newSingleThreadExecutor()
        takeScreenshot(
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            executor,
            object : TakeScreenshotCallback {
                override fun onSuccess(screenshot: android.accessibilityservice.AccessibilityScreenshotResult) {
                    val bitmap = screenshot.hardwareBuffer.use { hwBuffer ->
                        Bitmap.wrapHardwareBuffer(hwBuffer, screenshot.colorSpace)
                            ?.copy(Bitmap.Config.ARGB_8888, false)
                    }
                    if (bitmap == null) { callback(null); return }
                    val out = File(cacheDir, "shot_${System.currentTimeMillis()}.png")
                    FileOutputStream(out).use { bitmap.compress(Bitmap.CompressFormat.PNG, 90, it) }
                    callback(out)
                }

                override fun onFailure(errorCode: Int) {
                    Log.e(TAG, "Screenshot failed: $errorCode")
                    callback(null)
                }
            }
        )
    }

    companion object {
        private const val TAG = "StonicA11y"

        @Volatile
        private var instance: StonicAccessibilityService? = null

        fun get(): StonicAccessibilityService? = instance
        fun isReady(): Boolean = instance != null

        fun isConnectedTo(packageName: String): Boolean =
            instance?.packageName == packageName
    }
}

private inline fun <T : AutoCloseable?, R> T.use(block: (T) -> R): R {
    var exception: Throwable? = null
    try { return block(this) }
    catch (e: Throwable) { exception = e; throw e }
    finally {
        when {
            this == null -> {}
            exception == null -> close()
            else -> try { close() } catch (_: Throwable) {}
        }
    }
}
