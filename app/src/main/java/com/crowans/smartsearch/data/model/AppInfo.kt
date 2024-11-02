package com.crowans.smartsearch.data.model

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.crowans.smartsearch.data.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: android.graphics.drawable.Drawable?
)

class AppsRepository(private val context: Context) {
    suspend fun searchApps(query: String): List<AppInfo> = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager
        val applications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        applications
            .filter { app ->
                val appName = app.loadLabel(packageManager).toString()
                appName.contains(query, ignoreCase = true) &&
                        isUserApp(app)
            }
            .map { app ->
                AppInfo(
                    packageName = app.packageName,
                    appName = app.loadLabel(packageManager).toString(),
                    icon = app.loadIcon(packageManager)
                )
            }
            .sortedBy { it.appName }
    }

    private fun isUserApp(appInfo: ApplicationInfo): Boolean {
        return appInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
    }
}