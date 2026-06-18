package com.anis.child.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import com.anis.child.content.AppBlocker
import com.anis.child.content.BlockingOverlayManager
import com.anis.child.data.AppUsageRequest
import com.anis.child.data.LogManager
import com.anis.child.data.LogType
import com.anis.child.data.PreferenceManager
import com.anis.child.data.ScreenTimeManager
import com.anis.child.network.ApiResult
import com.anis.child.network.ApiService
import com.anis.child.network.safeApiCall
import com.anis.child.util.registerReceiverSafe
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@AndroidEntryPoint
class AppRestrictionService : Service() {

    @Inject lateinit var screenTimeManager: ScreenTimeManager
    @Inject lateinit var logManager: LogManager
    @Inject lateinit var preferenceManager: PreferenceManager
    @Inject lateinit var appBlocker: AppBlocker
    @Inject lateinit var apiService: ApiService

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var monitoringJob: Job? = null
    private var usageSyncJob: Job? = null
    @Volatile
    private var _isRunning = false
    @Volatile
    private var lastHeartbeatMs = 0L
    private val appUsageSyncMap = ConcurrentHashMap<String, Long>()
    private var lastForegroundApp: String? = null

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
        if (_isRunning) {
            startForeground(NOTIFICATION_ID, buildNotification())
            return START_STICKY
        }
        _isRunning = true
        isRunning = true

        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)

        lastHeartbeatMs = System.currentTimeMillis()

        monitoringJob = serviceScope.launch {
            while (isActive && _isRunning) {
                lastHeartbeatMs = System.currentTimeMillis()
                checkForegroundApp()
                delay(POLL_INTERVAL_MS)
            }
        }

        usageSyncJob = serviceScope.launch {
            while (isActive && _isRunning) {
                delay(USAGE_SYNC_INTERVAL_MS)
                sendUsage()
            }
        }

        logManager.log("Restriction service started", LogType.INFO)
        return START_STICKY
    }

    private fun isSystemApp(packageName: String): Boolean {
        return try {
            val info = packageManager.getApplicationInfo(packageName, 0)
            (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    private suspend fun checkForegroundApp() {
        try {
            val foregroundApp = screenTimeManager.getCurrentForegroundApp()
            if (foregroundApp.isNotEmpty()) {
                if (foregroundApp == lastForegroundApp) {
                    screenTimeManager.recordForegroundTime(foregroundApp, POLL_INTERVAL_MS)
                    if (!isSystemApp(foregroundApp)) {
                        appUsageSyncMap[foregroundApp] =
                            (appUsageSyncMap[foregroundApp] ?: 0L) + POLL_INTERVAL_MS
                    }
                }
                lastForegroundApp = foregroundApp
                appBlocker.checkAndBlock(foregroundApp, accessibilityOverlay = false)
            } else {
                lastForegroundApp = null
            }
        } catch (e: SecurityException) {
            logManager.log("Usage stats permission not granted", LogType.ERROR)
        } catch (_: Exception) { }
    }

    private suspend fun sendUsage() {
        if (appUsageSyncMap.isEmpty()) return
        val now = System.currentTimeMillis()
        val snapshot = appUsageSyncMap.toMap()
        appUsageSyncMap.clear()
        for ((pkg, accumulatedMs) in snapshot) {
            if (accumulatedMs < 1000L || isSystemApp(pkg)) continue
            val seconds = (accumulatedMs / 1000L).toInt()
            when (val result = safeApiCall {
                apiService.sendAppUsage(
                    packageName = pkg,
                    request = AppUsageRequest(
                        duration = seconds,
                        timestamp = now,
                        isLive = true
                    )
                )
            }) {
                is ApiResult.Success -> {
                    logManager.log("Usage synced: $pkg ${seconds}s", LogType.INFO)
                }
                else -> {}
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        _isRunning = false
        isRunning = false
        serviceScope.cancel()
        try { unregisterReceiver(overlayReceiver) } catch (_: Exception) {}
        BlockingOverlayManager.hideOverlay()
        logManager.log("Restriction service stopped", LogType.INFO)
        super.onDestroy()
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
        @Volatile
        var isRunning = false
            private set

        private const val NOTIFICATION_ID = 1002
        private const val CHANNEL_ID = "app_restriction_service"
        private const val POLL_INTERVAL_MS = 3000L
        private const val USAGE_SYNC_INTERVAL_MS = 30_000L
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
