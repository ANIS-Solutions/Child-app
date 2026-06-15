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
import com.anis.child.ai.util.ImageStorageManager
import com.anis.child.ai.util.PermissionManager
import com.anis.child.ai.util.PermissionType
import com.anis.child.data.local.SessionEntity
import com.anis.child.worker.SessionSyncWorker
import com.anis.child.data.repository.SessionRepository
import com.anis.child.ml.CondensationEngine

import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
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
    var autoRotateMaxCaptures: Int = SessionCaptureService.DEFAULT_AUTO_ROTATE_MAX_CAPTURES

    private val condensationEngine = CondensationEngine()

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
            data = mediaProjectionData!!,
            autoRotateMaxCaptures = autoRotateMaxCaptures
        )
    }

    fun stopSession() {
        val currentState = _sessionState.value
        if (currentState is SessionState.Active) {
            SessionCaptureService.stopService(context)

            val (batteryEnd, isCharging, cpuDeltaMs, cpuPercent, ramPssMb) = readDeviceStats()

            scope.launch {
                finishSession(
                    sessionId = currentState.sessionId,
                    batteryEnd = batteryEnd,
                    isCharging = isCharging,
                    cpuDeltaMs = cpuDeltaMs,
                    cpuPercent = cpuPercent,
                    ramPssMb = ramPssMb
                )
                _currentSession.value = null
                _sessionState.value = SessionState.Idle
            }
        }
    }

    suspend fun handleAutoRotate(oldSessionId: Long): Long {
        Log.d(TAG, "handleAutoRotate: ending session $oldSessionId, creating new session")

        val (batteryEnd, isCharging, cpuDeltaMs, cpuPercent, ramPssMb) = readDeviceStats()

        finishSession(
            sessionId = oldSessionId,
            batteryEnd = batteryEnd,
            isCharging = isCharging,
            cpuDeltaMs = cpuDeltaMs,
            cpuPercent = cpuPercent,
            ramPssMb = ramPssMb
        )

        val batteryStart = readBatteryLevel()
        cpuTimeStartMs = readProcessCpuTimeMs()
        sessionStartWallTime = System.currentTimeMillis()

        val newSessionId = sessionRepository.createSession(sessionIntervalMs, batteryStart)
        Log.d(TAG, "handleAutoRotate: created new session id=$newSessionId")

        _currentSession.value = sessionRepository.getSessionById(newSessionId)
        _sessionState.value = SessionState.Active(newSessionId)

        return newSessionId
    }

    private fun readDeviceStats(): DeviceStats {
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

        Log.d(TAG, "deviceStats: batteryEnd=$batteryEnd, charging=$isCharging, cpuTime=${cpuDeltaMs}ms, cpuPercent=${"%.1f".format(cpuPercent)}, ramPssMb=${"%.1f".format(ramPssMb)}")
        return DeviceStats(batteryEnd, isCharging, cpuDeltaMs, cpuPercent, ramPssMb)
    }

    private suspend fun finishSession(
        sessionId: Long,
        batteryEnd: Int,
        isCharging: Boolean,
        cpuDeltaMs: Long,
        cpuPercent: Double,
        ramPssMb: Double
    ) {
        sessionRepository.endSession(sessionId)

        runKeyframeVoting(sessionId)

        moveKeyframeImagesToPermanent(sessionId)

        ImageStorageManager.clearSessionCache(context, sessionId)

        sessionRepository.updateDeviceStats(
            sessionId = sessionId,
            batteryEnd = batteryEnd,
            batteryCharging = isCharging,
            cpuTimeMs = cpuDeltaMs,
            cpuUsagePercent = cpuPercent,
            ramPssMb = ramPssMb
        )

        val unsyncedCount = sessionRepository.getUnsyncedCompletedSessionCount()
        if (unsyncedCount > 0 && unsyncedCount % 5 == 0) {
            Log.d(TAG, "Auto-triggering session sync ($unsyncedCount unsynced sessions)")
            SessionSyncWorker.enqueue(context)
        }
    }

    private suspend fun moveKeyframeImagesToPermanent(sessionId: Long) {
        try {
            val session = sessionRepository.getSessionById(sessionId) ?: return
            val idsJson = session.keyframeIndices ?: return
            val resultIds = try {
                JSONArray(idsJson).let { arr ->
                    (0 until arr.length()).map { arr.getLong(it) }
                }
            } catch (e: Exception) {
                emptyList()
            }
            if (resultIds.isEmpty()) return

            val results = sessionRepository.getKeyframeResults(sessionId)
            for (result in results) {
                val moved = ImageStorageManager.moveImageToPermanent(context, sessionId, result.timestamp)
                if (moved) {
                    val newPath = ImageStorageManager.getPermanentImagePath(context, sessionId, result.timestamp)
                    sessionRepository.updateResultImagePath(result.id, newPath)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to move keyframe images to permanent storage", e)
        }
    }

    private data class DeviceStats(
        val batteryEnd: Int,
        val isCharging: Boolean,
        val cpuDeltaMs: Long,
        val cpuPercent: Double,
        val ramPssMb: Double
    )

    fun onDestroy() {
        if (_sessionState.value is SessionState.Active) {
            stopSession()
        }
    }

    private suspend fun runKeyframeVoting(sessionId: Long) {
        try {
            val results = sessionRepository.getOrderedResultsWithEmbeddings(sessionId)
            if (results.size < 2) {
                Log.d(TAG, "Not enough results with embeddings for keyframe voting: ${results.size}")
                return
            }

            val embeddings = results.map { result ->
                val arr = JSONArray(result.embedding!!)
                FloatArray(arr.length()) { i -> arr.getDouble(i).toFloat() }
            }

            val k = minOf(maxOf(embeddings.size / 10, 3), 10, embeddings.size)
            val listIndices = condensationEngine.extractKeyframes(embeddings, k)

            val resultIds = listIndices.map { results[it].id }
            val idsJson = JSONArray(resultIds).toString()
            sessionRepository.updateKeyframeIndices(sessionId, idsJson)

            Log.d(TAG, "Keyframe voting completed: selected $k frames from ${embeddings.size} total, resultIds=$idsJson")
        } catch (e: Exception) {
            Log.e(TAG, "Keyframe voting failed", e)
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
