package com.anis.child.content

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import com.anis.child.data.LogManager
import com.anis.child.data.LogType
import com.anis.child.data.ScreenTimeManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class AppBlockAccessibilityService : AccessibilityService() {

    @Inject lateinit var logManager: LogManager

    private var blockedPackageName: String? = null
    private var isForegroundService = false

    private val toggleReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_ENABLE -> {
                    val screenTimeManager = ScreenTimeManager(applicationContext)
                    CurrentHolder.screenTimeManager = screenTimeManager
                    logManager.log("Accessibility service activated", LogType.INFO)
                    startForegroundNotification()
                }
                ACTION_DISABLE -> {
                    hideOverlay()
                    stopForegroundNotification()
                    logManager.log("Accessibility service deactivated", LogType.INFO)
                }
                ACTION_CHECK_NOW -> {
                    checkCurrentApp()
                }
                ACTION_UPDATE_BLOCKED_APPS -> {
                    hideOverlay()
                    blockedPackageName = null
                    checkCurrentApp()
                }
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 100
        }
        serviceInfo = info

        createNotificationChannel()

        val filter = IntentFilter().apply {
            addAction(ACTION_ENABLE)
            addAction(ACTION_DISABLE)
            addAction(ACTION_CHECK_NOW)
            addAction(ACTION_UPDATE_BLOCKED_APPS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(toggleReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(toggleReceiver, filter)
        }

        val screenTimeManager = ScreenTimeManager(applicationContext)
        CurrentHolder.screenTimeManager = screenTimeManager

        logManager.log("Accessibility service connected", LogType.SUCCESS)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                val packageName = event.packageName?.toString() ?: return
                if (packageName != blockedPackageName) {
                    handleForegroundAppChanged(packageName)
                }
            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                val packageName = event.packageName?.toString() ?: return
                if (packageName == blockedPackageName) {
                    ensureOverlayVisible()
                }
            }
        }
    }

    override fun onInterrupt() {
        logManager.log("Accessibility service interrupted", LogType.ERROR)
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(toggleReceiver) } catch (_: Exception) {}
        hideOverlay()
        stopForegroundNotification()
        logManager.log("Accessibility service destroyed", LogType.INFO)
    }

    private fun handleForegroundAppChanged(pkg: String) {
        if (pkg == packageName || pkg.isEmpty()) {
            hideOverlay()
            blockedPackageName = null
            return
        }

        val sm = CurrentHolder.screenTimeManager ?: return
        val isBlocked = try {
            runBlocking { sm.isAppBlocked(pkg) }
        } catch (_: Exception) { false }

        if (isBlocked) {
            blockedPackageName = pkg
            showOverlay(pkg)
            logManager.log("Blocked app via accessibility: $pkg", LogType.ERROR)
        } else {
            if (blockedPackageName != null) {
                hideOverlay()
                blockedPackageName = null
            }
        }
    }

    private fun checkCurrentApp() {
        val sm = CurrentHolder.screenTimeManager ?: return
        val currentApp = sm.getCurrentForegroundApp()
        if (currentApp.isNotEmpty()) {
            handleForegroundAppChanged(currentApp)
        }
    }

    private fun showOverlay(packageName: String) {
        BlockingOverlayManager.showOverlay(this, packageName, accessibilityOverlay = true)
    }

    private fun hideOverlay() {
        BlockingOverlayManager.hideOverlay()
    }

    private fun ensureOverlayVisible() {
        if (!BlockingOverlayManager.isShowing() && blockedPackageName != null) {
            BlockingOverlayManager.showOverlay(this, blockedPackageName!!, accessibilityOverlay = true)
        }
    }

    private fun startForegroundNotification() {
        if (isForegroundService) return
        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)
        isForegroundService = true
    }

    private fun stopForegroundNotification() {
        if (!isForegroundService) return
        stopForeground(STOP_FOREGROUND_REMOVE)
        isForegroundService = false
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Content Protection",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors and blocks restricted apps and content"
            }
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            Notification.Builder(this)
        }
        return builder
            .setContentTitle("ANIS Content Protection")
            .setContentText("Monitoring apps and blocking restricted content")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val ACTION_ENABLE = "com.anis.child.accessibility.ENABLE"
        const val ACTION_DISABLE = "com.anis.child.accessibility.DISABLE"
        const val ACTION_CHECK_NOW = "com.anis.child.accessibility.CHECK_NOW"
        const val ACTION_UPDATE_BLOCKED_APPS = "com.anis.child.accessibility.UPDATE_BLOCKED_APPS"
        private const val NOTIFICATION_ID = 1003
        private const val CHANNEL_ID = "content_protection_service"

        fun openAccessibilitySettings(context: Context) {
            val intent = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }

        fun sendEnable(context: Context) {
            context.sendBroadcast(Intent(ACTION_ENABLE).setPackage(context.packageName))
        }

        fun sendDisable(context: Context) {
            context.sendBroadcast(Intent(ACTION_DISABLE).setPackage(context.packageName))
        }

        fun sendCheckNow(context: Context) {
            context.sendBroadcast(Intent(ACTION_CHECK_NOW).setPackage(context.packageName))
        }

        fun sendUpdateBlockedApps(context: Context) {
            context.sendBroadcast(Intent(ACTION_UPDATE_BLOCKED_APPS).setPackage(context.packageName))
        }
    }
}

internal object CurrentHolder {
    var screenTimeManager: ScreenTimeManager? = null
}
