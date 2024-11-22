package com.crowans.smartsearch.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.collection.LruCache
import com.crowans.smartsearch.data.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class AppsRepository(private val context: Context) {
    private val appsCache = LruCache<String, List<AppInfo>>(50)
    private val mutex = Mutex()
    private var cachedApps: List<AppInfo>? = null

    init {
        // Preload apps in background
        preloadApps()
    }

    private fun preloadApps() {
        Thread {
            loadAllApps()
        }.apply {
            priority = Thread.MIN_PRIORITY
            start()
        }
    }

    private fun loadAllApps(): List<AppInfo> {
        val mainIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val packageManager = context.packageManager
        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                mainIntent,
                PackageManager.ResolveInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(mainIntent, 0)
        }

        return resolveInfos.map { resolveInfo ->
            AppInfo(
                packageName = resolveInfo.activityInfo.packageName,
                appName = resolveInfo.loadLabel(packageManager).toString(),
                icon = resolveInfo.loadIcon(packageManager)
            )
        }.sortedBy { it.appName }
    }

    suspend fun searchApps(query: String): List<AppInfo> = withContext(Dispatchers.IO) {
        if (query.length < 2) return@withContext emptyList()

        val normalizedQuery = query.trim().lowercase()

        mutex.withLock {
            // Try cache first
            appsCache.get(normalizedQuery) ?: run {
                // If not in cache, search in memory
                val apps = cachedApps ?: loadAllApps().also {
                    cachedApps = it
                }

                apps.filter { app ->
                    app.appName.lowercase().contains(normalizedQuery)
                }.sortedWith(
                    compareBy<AppInfo> {
                        !it.appName.lowercase().startsWith(normalizedQuery)
                    }.thenBy {
                        it.appName
                    }
                ).take(10) // Limit results
                    .also {
                        appsCache.put(normalizedQuery, it)
                    }
            }
        }
    }
}