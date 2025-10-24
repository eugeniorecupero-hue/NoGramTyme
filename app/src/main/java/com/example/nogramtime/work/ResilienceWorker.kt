package com.example.nogramtime.work

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.nogramtime.service.ForegroundMonitorService

/**
 * A periodic worker that runs in the background to ensure the accessibility
 * service and VPN (if enabled) remain active. It checks whether the
 * accessibility service is enabled in settings and restarts the foreground
 * service if necessary. Note that due to power restrictions this worker runs
 * infrequently and should not be relied upon for real‑time enforcement.
 */
class ResilienceWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        // Check whether the accessibility service is enabled
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        if (enabledServices == null || !enabledServices.contains(context.packageName)) {
            // The service might have been disabled; launching our monitor service
            context.startForegroundService(Intent(context, ForegroundMonitorService::class.java))
        }

        // Restart or stop the VPN service depending on the saved preference. The
        // VPN service automatically exits when disabled so calling stop on an
        // already stopped service is harmless.
        val vpnEnabled = com.example.nogramtime.data.SettingsManager.isVpnEnabled(context)
        if (vpnEnabled) {
            context.startService(Intent(context, com.example.nogramtime.service.BlockingVpnService::class.java))
        } else {
            context.stopService(Intent(context, com.example.nogramtime.service.BlockingVpnService::class.java))
        }
        return Result.success()
    }
}