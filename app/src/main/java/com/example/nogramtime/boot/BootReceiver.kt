package com.example.nogramtime.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.nogramtime.service.ForegroundMonitorService
import com.example.nogramtime.work.ResilienceWorker
import java.util.concurrent.TimeUnit

/**
 * Receiver that runs on device boot. It starts the foreground monitor service
 * and schedules the resilience worker to periodically verify that required
 * services are running. This ensures the accessibility service and VPN stay
 * active even after device reboot.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Start the foreground service which in turn ensures our accessibility
            // and VPN services remain active.
            context.startForegroundService(Intent(context, ForegroundMonitorService::class.java))

            // Start the VPN service if it was previously enabled. The VPN
            // service will exit immediately if disabled.
            if (com.example.nogramtime.data.SettingsManager.isVpnEnabled(context)) {
                context.startService(Intent(context, com.example.nogramtime.service.BlockingVpnService::class.java))
            }

            // Schedule a periodic worker that will restart services if needed.
            val workRequest = PeriodicWorkRequestBuilder<ResilienceWorker>(
                15, TimeUnit.MINUTES
            ).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "nogram_resilience",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}