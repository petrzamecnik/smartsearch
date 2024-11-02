package com.crowans.smartsearch.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.crowans.smartsearch.data.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppsRepository(private val context: Context) {
    suspend fun searchApps(query: String): List<AppInfo> = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        // Get only launchable apps
        val launchableApps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                mainIntent,
                PackageManager.ResolveInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(mainIntent, 0)
        }

        return@withContext launchableApps
            .filter { resolveInfo ->
                val appLabel = resolveInfo.loadLabel(packageManager).toString()
                appLabel.contains(query, ignoreCase = true)
            }
            .map { resolveInfo ->
                AppInfo(
                    packageName = resolveInfo.activityInfo.packageName,
                    appName = resolveInfo.loadLabel(packageManager).toString(),
                    icon = resolveInfo.loadIcon(packageManager)
                )
            }
            .sortedBy { it.appName }
    }
}