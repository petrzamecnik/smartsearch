package com.crowans.smartsearch.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

object PermissionUtils {
    private const val OVERLAY_PERMISSION_REQUEST_CODE = 1234

    fun checkOverlayPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(activity)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${activity.packageName}")
            )
            activity.startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE)
        }
    }

    fun handleOverlayPermissionResult(activity: Activity, requestCode: Int) {
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(activity)) {
                // Permission denied, handle accordingly
                activity.finish()
            }
            // Permission granted, continue with overlay
        }
    }
}