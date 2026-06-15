package com.anis.child.ai.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.os.Build
import android.os.IBinder

import com.anis.child.MainActivity
import com.anis.child.content.BlockingOverlayManager
import com.anis.child.data.ContentFilterManager
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
class AiFilteringService : Service() {

    @Inject lateinit var preferenceManager: PreferenceManager
    @Inject lateinit var screenTimeManager: ScreenTimeManager
    @Inject lateinit var contentFilterManager: ContentFilterManager
    @Inject lateinit var logManager: LogManager

    private var monitoringJob: Job? = null
    private var shouldRun = false
    private var mediaProjectionGranted = false

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> screenOff = true
                Intent.ACTION_SCREEN_ON -> screenOff = false
            }
        }
    }

    @Volatile
    private var screenOff = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        registerReceiverSafe(screenReceiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                if (shouldRun) return START_STICKY
                shouldRun = true
                mediaProjectionGranted = false
                preferenceManager.isAiLockdownActive = true
                startForeground(NOTIFICATION_ID, buildFallbackNotification())
                startPolling()
                isRunning = true
                logManager.log("AI filtering service started (lockdown active)", LogType.INFO)
            }
            ACTION_STOP -> {
                if (!preferenceManager.isAiFilteringEnabled) {
                    stopInternal()
                }
                return START_NOT_STICKY
            }
            ACTION_GRANT_MEDIA_PROJECTION -> {
                val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, -1)
                val data: Intent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(EXTRA_DATA, Intent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(EXTRA_DATA)
                }
                if (resultCode == RESULT_OK && data != null) {
                    mediaProjectionGranted = true
                    preferenceManager.isAiLockdownActive = false
                    val notification = buildFullNotification()
                    val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    nm.notify(NOTIFICATION_ID, notification)
                    logManager.log("MediaProjection granted — full AI filtering active", LogType.SUCCESS)
                }
            }
            ACTION_GRANT_DENIED -> {
                logManager.log("MediaProjection denied — staying in lockdown", LogType.ERROR)
            }
        }
        return START_STICKY
    }

    private fun startPolling() {
        monitoringJob = CoroutineScope(Dispatchers.IO).launch {
            while (shouldRun) {
                if (!screenOff) {
                    checkAndBlock()
                }
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    private suspend fun checkAndBlock() {
        try {
            val pkg = screenTimeManager.getCurrentForegroundApp()
            if (pkg.isEmpty() || pkg == packageName) {
                BlockingOverlayManager.hideOverlay()
                return
            }

            if (preferenceManager.isAiLockdownActive) {
                if (isSystemApp(pkg) || pkg == "com.android.settings") {
                    BlockingOverlayManager.hideOverlay()
                } else {
                    BlockingOverlayManager.showOverlay(this, pkg, accessibilityOverlay = false)
                }
                return
            }

            if (!mediaProjectionGranted) {
                val rules = contentFilterManager.checkText(pkg)
                if (rules.isBlocked) {
                    if (isSystemApp(pkg)) {
                        BlockingOverlayManager.hideOverlay()
                    } else {
                        BlockingOverlayManager.showOverlay(this, pkg, accessibilityOverlay = false)
                    }
                }
            }
        } catch (e: SecurityException) {
            logManager.log("Usage stats permission not granted", LogType.ERROR)
        } catch (_: Exception) { }
    }

    private fun isSystemApp(pkg: String): Boolean {
        return try {
            packageManager.getApplicationInfo(pkg, 0).flags and ApplicationInfo.FLAG_SYSTEM != 0
        } catch (_: Exception) { false }
    }

    private fun stopInternal() {
        shouldRun = false
        monitoringJob?.cancel()
        mediaProjectionGranted = false
        preferenceManager.isAiLockdownActive = false
        BlockingOverlayManager.hideOverlay()
        isRunning = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        logManager.log("AI filtering service stopped", LogType.INFO)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        shouldRun = false
        monitoringJob?.cancel()
        try { unregisterReceiver(screenReceiver) } catch (_: Exception) {}
        BlockingOverlayManager.hideOverlay()
        if (preferenceManager.isAiFilteringEnabled) {
            startService(Intent(this, AiFilteringService::class.java).apply { action = ACTION_START })
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "AI Content Filtering",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors and filters content using AI analysis"
            }
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildFallbackNotification(): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_REQUEST_MEDIA_PROJECTION, true)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            Notification.Builder(this)
        }
        return builder
            .setContentTitle("AI Content Filtering")
            .setContentText("Grant screen permission to use apps")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun buildFullNotification(): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            Notification.Builder(this)
        }
        return builder
            .setContentTitle("AI Content Filtering")
            .setContentText("Full screen analysis active")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val ACTION_START = "com.anis.child.aifilter.START"
        const val ACTION_STOP = "com.anis.child.aifilter.STOP"
        const val ACTION_GRANT_MEDIA_PROJECTION = "com.anis.child.aifilter.GRANT"
        const val ACTION_GRANT_DENIED = "com.anis.child.aifilter.GRANT_DENIED"
        const val EXTRA_RESULT_CODE = "extra_result_code"
        const val EXTRA_DATA = "extra_data"
        const val EXTRA_REQUEST_MEDIA_PROJECTION = "com.anis.child.REQUEST_MEDIA_PROJECTION"

        private const val NOTIFICATION_ID = 1004
        private const val CHANNEL_ID = "ai_filtering_service"
        private const val POLL_INTERVAL_MS = 3000L
        private const val RESULT_OK = -1

        @Volatile
        var isRunning = false
            private set

        fun start(context: Context) {
            val intent = Intent(context, AiFilteringService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            isRunning = true
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, AiFilteringService::class.java).apply {
                action = ACTION_STOP
            })
            isRunning = false
        }

        fun grantMediaProjection(context: Context, resultCode: Int, data: Intent) {
            val intent = Intent(context, AiFilteringService::class.java).apply {
                action = ACTION_GRANT_MEDIA_PROJECTION
                putExtra(EXTRA_RESULT_CODE, resultCode)
                putExtra(EXTRA_DATA, data)
            }
            context.startService(intent)
        }

        fun grantDenied(context: Context) {
            val intent = Intent(context, AiFilteringService::class.java).apply {
                action = ACTION_GRANT_DENIED
            }
            context.startService(intent)
        }
    }
}
