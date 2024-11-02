package com.crowans.smartsearch

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.crowans.smartsearch.ui.screens.SearchScreen
import com.crowans.smartsearch.utils.PermissionUtils

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure window to handle system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Request permissions when activity is created
        PermissionUtils.checkPermissions(this)

        setContent {
            SearchScreen()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        PermissionUtils.handlePermissionResult(
            activity = this,
            requestCode = requestCode,
            grantResults = intArrayOf(),
            onOverlayGranted = {
                // Handle overlay permission granted
            },
            onOverlayDenied = {
                // Handle overlay permission denied
                finish()
            }
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionUtils.handlePermissionResult(
            activity = this,
            requestCode = requestCode,
            grantResults = grantResults,
            onContactsGranted = {
                // Handle contacts permission granted
            },
            onContactsDenied = {
                Toast.makeText(
                    this,
                    "Contacts permission is required for search functionality",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }
}