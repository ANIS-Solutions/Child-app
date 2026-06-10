package com.anis.child.ai

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.projection.MediaProjectionManager
import android.os.BatteryManager
import android.os.Debug
import android.util.Log
import com.anis.child.ai.service.SessionCaptureService
import com.anis.child.ai.util.PermissionManager
import com.anis.child.ai.util.PermissionType
import com.anis.child.data.local.SessionEntity
import com.anis.child.data.repository.SessionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionRepository: SessionRepository
) {
    private val TAG = "SessionManager"
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Idle)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    private val _currentSession = MutableStateFlow<SessionEntity?>(null)
    val currentSession: StateFlow<SessionEntity?> = _currentSession.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    private var mediaProjectionResultCode: Int = -1
    private var mediaProjectionData: Intent? = null
    private var cpuTimeStartMs: Long = 0L
    private var sessionStartWallTime: Long = 0L

    var sessionIntervalMs: Int = 1000
    var blurTriggerThreshold: Int = 3

    suspend fun startSession(activity: Activity) {
        Log.d(TAG, "startSession called")

        val missingPermissions = PermissionManager.getMissingPermissions(context, activity)
        Log.d(TAG, "Missing permissions: ${missingPermissions.map { it.name }}")

        if (missingPermissions.isNotEmpty()) {
            _sessionState.value = SessionState.PermissionRequired(missingPermissions)
            return
        }

        val intent = PermissionManager.getMediaProjectionPermissionLauncherIntent(activity)
        if (intent != null) {
            _sessionState.value = SessionState.MediaProjectionRequired(intent)
        } else {
            Log.w(TAG, "No media projection intent returned!")
        }
    }

    fun setMediaProjectionResult(resultCode: Int, data: Intent) {
        Log.d(TAG, "setMediaProjectionResult: resultCode=$resultCode (RESULT_OK=${Activity.RESULT_OK})")

        mediaProjectionResultCode = resultCode
        mediaProjectionData = data

        val hasNotifPermission = PermissionManager.hasNotificationPermission(context)
        Log.d(TAG, "hasNotificationPermission: $hasNotifPermission")

        if (resultCode == Activity.RESULT_OK && data != null) {
            scope.launch { startCaptureSession() }
        } else {
            _sessionState.value = SessionState.Idle
        }
    }

    fun onNotificationPermissionGranted() {
        Log.d(TAG, "Notification permission granted")
        if (mediaProjectionResultCode != -1 && mediaProjectionData != null) {
            scope.launch { startCaptureSession() }
        }
    }

    private suspend fun startCaptureSession() {
        Log.d(TAG, "startCaptureSession called")

        val batteryStart = readBatteryLevel()
        cpuTimeStartMs = readProcessCpuTimeMs()
        sessionStartWallTime = System.currentTimeMillis()

        val intervalMs = sessionIntervalMs
        val blurThreshold = blurTriggerThreshold
        val sessionId = sessionRepository.createSession(intervalMs, batteryStart)
        Log.d(TAG, "Session created: id=$sessionId, blurThreshold=$blurThreshold, batteryStart=$batteryStart")

        _currentSession.value = sessionRepository.getSessionById(sessionId)
        _sessionState.value = SessionState.Active(sessionId)

        Log.d(TAG, "Starting SessionCaptureService with sessionId=$sessionId")

        SessionCaptureService.startService(
            context = context,
            sessionId = sessionId,
            intervalMs = intervalMs,
            blurTriggerThreshold = blurThreshold,
            resultCode = mediaProjectionResultCode,
            data = mediaProjectionData!!
        )
    }

    fun stopSession() {
        val currentState = _sessionState.value
        if (currentState is SessionState.Active) {
            SessionCaptureService.stopService(context)

            val batteryEnd = readBatteryLevel()
            val isCharging = readBatteryCharging()
            val cpuTimeEndMs = readProcessCpuTimeMs()
            val wallTimeEnd = System.currentTimeMillis()
            val ramPssMb = readRamPss()

            val cpuDeltaMs = cpuTimeEndMs - cpuTimeStartMs
            val wallDeltaMs = wallTimeEnd - sessionStartWallTime
            val cpuPercent = if (wallDeltaMs > 0) {
                (cpuDeltaMs.toDouble() / wallDeltaMs) * 100.0
            } else 0.0

            Log.d(TAG, "stopSession: batteryEnd=$batteryEnd, charging=$isCharging, cpuTime=${cpuDeltaMs}ms, cpuPercent=${"%.1f".format(cpuPercent)}, ramPssMb=${"%.1f".format(ramPssMb)}")

            scope.launch {
                sessionRepository.endSession(currentState.sessionId)
                sessionRepository.updateDeviceStats(
                    sessionId = currentState.sessionId,
                    batteryEnd = batteryEnd,
                    batteryCharging = isCharging,
                    cpuTimeMs = cpuDeltaMs,
                    cpuUsagePercent = cpuPercent,
                    ramPssMb = ramPssMb
                )
                _currentSession.value = null
                _sessionState.value = SessionState.Idle
            }
        }
    }

    fun onDestroy() {
        if (_sessionState.value is SessionState.Active) {
            stopSession()
        }
    }

    fun clearPermissionState() {
        if (_sessionState.value is SessionState.PermissionRequired) {
            _sessionState.value = SessionState.Idle
        }
    }

    fun clearMediaProjectionState() {
        if (_sessionState.value is SessionState.MediaProjectionRequired) {
            _sessionState.value = SessionState.Idle
        }
    }

    private fun readBatteryLevel(): Int {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        return batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 0
    }

    private fun readBatteryCharging(): Boolean {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val intent = context.registerReceiver(null, filter)
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
    }

    private fun readProcessCpuTimeMs(): Long {
        return try {
            val stat = File("/proc/self/stat").readText().split(" ")
            val utime = stat[13].toLong()
            val stime = stat[14].toLong()
            (utime + stime) * 10
        } catch (e: Exception) {
            0L
        }
    }

    private fun readRamPss(): Double {
        val memInfo = Debug.MemoryInfo()
        Debug.getMemoryInfo(memInfo)
        return memInfo.totalPss / 1024.0
    }
}

sealed class SessionState {
    data object Idle : SessionState()
    data class Active(val sessionId: Long) : SessionState()
    data class PermissionRequired(val missingPermissions: List<PermissionType>) : SessionState()
    data class MediaProjectionRequired(val intent: Intent) : SessionState()
    data object NotificationPermissionRequired : SessionState()
    data class Error(val message: String) : SessionState()
}
