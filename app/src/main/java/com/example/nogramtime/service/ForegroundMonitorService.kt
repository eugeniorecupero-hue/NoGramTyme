package com.example.nogramtime.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.nogramtime.R

/**
 * Foreground service that maintains a persistent notification while the app is
 * running. Keeping a foreground service improves reliability of the
 * accessibility service and reduces the likelihood of the system killing the
 * process under memory pressure. The service itself does very little – it
 * simply keeps the notification visible.
 */
class ForegroundMonitorService : Service() {
    private val channelId = "nogram_monitor"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification = buildNotification()
        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Nothing to do here; the service stays running until explicitly stopped.
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        // This is a started service, not a bound service.
        return null
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelId,
            "NoGram Time monitor",
            NotificationManager.IMPORTANCE_LOW
        )
        channel.description = "Mantiene attivo il blocco di Instagram"
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("NoGram Time attivo")
            .setContentText("Il blocco di Instagram è attivo.")
            .setOngoing(true)
            .build()
    }
}