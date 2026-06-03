package com.anis.child.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import com.anis.child.R
import com.anis.child.data.LogManager
import com.anis.child.data.LogType
import com.anis.child.data.ScreenTimeManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AppRestrictionService : Service() {

    @Inject lateinit var screenTimeManager: ScreenTimeManager
    @Inject lateinit var logManager: LogManager

    private var overlayView: ViewGroup? = null
    private var windowManager: WindowManager? = null
    private var monitoringJob: Job? = null
    private var isRunning = false
    private var currentBlockedApp: String? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (isRunning) return START_STICKY
        isRunning = true

        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)

        monitoringJob = CoroutineScope(Dispatchers.IO).launch {
            while (isRunning) {
                checkForegroundApp()
                delay(POLL_INTERVAL_MS)
            }
        }

        logManager.log("Restriction service started", LogType.INFO)
        return START_STICKY
    }

    private suspend fun checkForegroundApp() {
        try {
            val foregroundApp = screenTimeManager.getCurrentForegroundApp()
            if (foregroundApp.isEmpty() || foregroundApp == packageName) {
                hideOverlay()
                currentBlockedApp = null
                return
            }

            val isBlocked = screenTimeManager.isAppBlocked(foregroundApp)
            if (isBlocked) {
                if (currentBlockedApp != foregroundApp) {
                    currentBlockedApp = foregroundApp
                    showOverlay(foregroundApp)
                    logManager.log("Blocked app detected: $foregroundApp", LogType.ERROR)
                }
            } else {
                if (currentBlockedApp != null) {
                    hideOverlay()
                    currentBlockedApp = null
                }
            }
        } catch (e: SecurityException) {
            logManager.log("Usage stats permission not granted", LogType.ERROR)
        } catch (_: Exception) { }
    }

    private fun showOverlay(packageName: String) {
        try {
            if (overlayView != null) hideOverlay()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!android.provider.Settings.canDrawOverlays(this)) return
            }

            val inflater = LayoutInflater.from(this)
            overlayView = inflater.inflate(R.layout.overlay_blocked_app, null) as ViewGroup

            val params = WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                android.graphics.PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.FILL
            }

            windowManager?.addView(overlayView, params)
        } catch (e: Exception) {
            logManager.log("Overlay error: ${e.message}", LogType.ERROR)
        }
    }

    private fun hideOverlay() {
        try {
            overlayView?.let { windowManager?.removeView(it) }
        } catch (_: Exception) { }
        overlayView = null
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        monitoringJob?.cancel()
        hideOverlay()
        logManager.log("Restriction service stopped", LogType.INFO)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "App Restrictions",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors and enforces app usage restrictions"
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
            .setContentTitle("ANIS Restriction Service")
            .setContentText("Monitoring app usage and enforcing limits")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 1002
        private const val CHANNEL_ID = "app_restriction_service"
        private const val POLL_INTERVAL_MS = 3000L

        fun start(context: android.content.Context) {
            val intent = Intent(context, AppRestrictionService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: android.content.Context) {
            context.stopService(Intent(context, AppRestrictionService::class.java))
        }
    }
}
