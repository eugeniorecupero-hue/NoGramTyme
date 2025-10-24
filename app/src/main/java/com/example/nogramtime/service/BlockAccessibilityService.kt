package com.example.nogramtime.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.example.nogramtime.data.AppDatabase
import com.example.nogramtime.data.ScheduleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Accessibility service that listens for window state changes. When the user
 * opens Instagram, the service checks whether a blocking rule is currently
 * active and, if so, displays a full‑screen overlay to prevent interaction.
 *
 * Android documentation notes that window state change events should be
 * inspected to understand how the UI changes【401119557551334†L1305-L1312】. The
 * event’s package name can be obtained via [AccessibilityEvent.getPackageName],
 * which returns the package name of the source【248230294453495†L811-L823】.
 */
class BlockAccessibilityService : AccessibilityService() {
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var repo: ScheduleRepository? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            packageNames = arrayOf("com.instagram.android")
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
        }
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg != "com.instagram.android") return
        // Check global suspension flag before performing any rule lookup. When
        // suspended the user has intentionally disabled the block via the
        // Settings screen.
        if (com.example.nogramtime.data.SettingsManager.isBlockingSuspended(applicationContext)) {
            return
        }
        // Lazily initialise the repository
        if (repo == null) {
            val dao = AppDatabase.getInstance(applicationContext).blockRuleDao()
            repo = ScheduleRepository(dao)
        }
        scope.launch {
            val shouldBlock = repo?.isInstagramBlockedNow() ?: false
            if (shouldBlock) {
                // Launch overlay activity. Use FLAG_ACTIVITY_NEW_TASK since
                // AccessibilityService runs outside of an activity context.
                val intent = Intent(this@BlockAccessibilityService, com.example.nogramtime.OverlayActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
    }

    override fun onInterrupt() {
        // Called when the system wants to interrupt the feedback this service is providing.
    }
}