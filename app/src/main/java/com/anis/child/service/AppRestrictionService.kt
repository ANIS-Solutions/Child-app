package com.anis.child.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import com.anis.child.content.AppBlocker
import com.anis.child.content.BlockingOverlayManager
import com.anis.child.data.LogManager
import com.anis.child.data.LogType
import com.anis.child.data.PreferenceManager
import com.anis.child.data.ScreenTimeManager
import com.anis.child.util.registerReceiverSafe
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AppRestrictionService : Service() {

    @Inject lateinit var screenTimeManager: ScreenTimeManager
    @Inject lateinit var logManager: LogManager
    @Inject lateinit var preferenceManager: PreferenceManager
    @Inject lateinit var appBlocker: AppBlocker

    private var monitoringJob: Job? = null
    private var isRunning = false

    private val overlayReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_HIDE_OVERLAY -> BlockingOverlayManager.hideOverlay()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        registerReceiverSafe(overlayReceiver, IntentFilter(ACTION_HIDE_OVERLAY))
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
            appBlocker.checkAndBlock(foregroundApp, accessibilityOverlay = false)
        } catch (e: SecurityException) {
            logManager.log("Usage stats permission not granted", LogType.ERROR)
        } catch (_: Exception) { }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        monitoringJob?.cancel()
        try { unregisterReceiver(overlayReceiver) } catch (_: Exception) {}
        BlockingOverlayManager.hideOverlay()
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
        const val ACTION_HIDE_OVERLAY = "com.anis.child.service.HIDE_OVERLAY"

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
