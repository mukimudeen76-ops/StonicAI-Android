package com.stonicai.app.accessibility

import android.content.Context
import android.content.Intent

data class InstalledApp(val packageName: String, val label: String)

object DeviceApps {

    fun installedLaunchableApps(context: Context): List<InstalledApp> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolved = pm.queryIntentActivities(intent, 0)
        return resolved
            .map { InstalledApp(it.activityInfo.packageName, it.loadLabel(pm).toString()) }
            .filter { it.packageName != context.packageName }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    fun search(context: Context, query: String, limit: Int = 12): List<InstalledApp> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return emptyList()
        return installedLaunchableApps(context)
            .filter { it.label.lowercase().contains(q) || it.packageName.lowercase().contains(q) }
            .take(limit)
    }
}
