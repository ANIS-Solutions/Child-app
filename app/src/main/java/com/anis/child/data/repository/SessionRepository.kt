package com.anis.child.data.repository

import android.util.Log
import com.anis.child.data.local.AnalysisResultDao
import com.anis.child.data.local.AnalysisResultEntity
import com.anis.child.data.local.SessionDao
import com.anis.child.data.local.SessionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepository @Inject constructor(
    private val sessionDao: SessionDao,
    private val analysisResultDao: AnalysisResultDao
) {
    private val TAG = "SessionRepository"

    fun getAllSessions(): Flow<List<SessionEntity>> = sessionDao.getAllSessions()

    suspend fun getSessionById(sessionId: Long): SessionEntity? = sessionDao.getSessionById(sessionId)

    fun getSessionByIdFlow(sessionId: Long): Flow<SessionEntity?> = sessionDao.getSessionByIdFlow(sessionId)

    suspend fun getActiveSession(): SessionEntity? = sessionDao.getActiveSession()

    suspend fun createSession(intervalMs: Int = 1000, batteryStart: Int = 0): Long {
        val session = SessionEntity(
            startTime = System.currentTimeMillis(),
            intervalMs = intervalMs,
            status = "ACTIVE",
            batteryStart = batteryStart
        )
        return sessionDao.insertSession(session)
    }

    suspend fun endSession(sessionId: Long) {
        val totalCaptures = analysisResultDao.getResultCountForSession(sessionId)
        val blockedCount = analysisResultDao.getBlockedCountForSession(sessionId)
        val safeCount = analysisResultDao.getSafeCountForSession(sessionId)
        sessionDao.endSession(
            sessionId = sessionId,
            endTime = System.currentTimeMillis(),
            totalCaptures = totalCaptures,
            blockedCount = blockedCount,
            safeCount = safeCount
        )
    }

    suspend fun updateDeviceStats(
        sessionId: Long,
        batteryEnd: Int,
        batteryCharging: Boolean,
        cpuTimeMs: Long,
        cpuUsagePercent: Double,
        ramPssMb: Double
    ) {
        sessionDao.updateDeviceStats(sessionId, batteryEnd, batteryCharging, cpuTimeMs, cpuUsagePercent, ramPssMb)
    }

    suspend fun deleteSession(sessionId: Long) {
        analysisResultDao.deleteResultsForSession(sessionId)
        sessionDao.deleteSession(sessionId)
    }

    fun getResultsForSession(sessionId: Long): Flow<List<AnalysisResultEntity>> =
        analysisResultDao.getResultsForSession(sessionId)

    fun getResultsForSessionDesc(sessionId: Long): Flow<List<AnalysisResultEntity>> =
        analysisResultDao.getResultsForSessionDesc(sessionId)

    suspend fun addAnalysisResult(
        sessionId: Long,
        analysisResult: String,
        decision: String,
        ocrTimeMs: Double = 0.0,
        onnxTimeMs: Double = 0.0,
        threatDetails: String = "",
        imagePath: String? = null
    ): Long {
        Log.d(TAG, "addAnalysisResult: sessionId=$sessionId, decision=$decision, imagePath=$imagePath")
        val result = AnalysisResultEntity(
            sessionId = sessionId,
            timestamp = System.currentTimeMillis(),
            analysisResult = analysisResult,
            decision = decision,
            ocrTimeMs = ocrTimeMs,
            onnxTimeMs = onnxTimeMs,
            threatDetails = threatDetails,
            imagePath = imagePath
        )
        return analysisResultDao.insert(result)
    }

    suspend fun addAnalysisResultEntity(result: AnalysisResultEntity): Long {
        return analysisResultDao.insert(result)
    }

    suspend fun addAnalysisResultsBatch(results: List<AnalysisResultEntity>) {
        Log.d(TAG, "Batch inserting ${results.size} results")
        analysisResultDao.insertResults(results)
    }
}
