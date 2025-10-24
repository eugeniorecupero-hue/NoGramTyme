package com.example.nogramtime.admin

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * Simple implementation of a [DeviceAdminReceiver]. This receiver is declared
 * in the manifest and allows the application to become a device administrator.
 * When enabled the application cannot be uninstalled until the user first
 * disables it. See the Android Enterprise documentation for more details.
 */
class AdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Toast.makeText(context, "Admin attivato", Toast.LENGTH_SHORT).show()
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(context, "Admin disattivato", Toast.LENGTH_SHORT).show()
    }
}