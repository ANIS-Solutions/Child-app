package com.anis.child.data.repository

import android.content.Context
import android.util.Log
import com.anis.child.data.local.AnalysisResultDao
import com.anis.child.data.local.AnalysisResultEntity
import com.anis.child.data.local.SessionDao
import com.anis.child.data.local.SessionEntity
import com.anis.child.data.local.SessionSyncDao
import com.anis.child.data.local.SessionSyncEntity
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepository @Inject constructor(
    private val sessionDao: SessionDao,
    private val analysisResultDao: AnalysisResultDao,
    private val sessionSyncDao: SessionSyncDao
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

    suspend fun getOrderedResultsWithEmbeddings(sessionId: Long): List<AnalysisResultEntity> {
        return analysisResultDao.getResultsForSessionOnce(sessionId)
            .filter { it.embedding != null }
    }

    suspend fun updateKeyframeIndices(sessionId: Long, indicesJson: String?) {
        sessionDao.updateKeyframeIndices(sessionId, indicesJson)
    }

    suspend fun updateResultImagePath(resultId: Long, newPath: String) {
        analysisResultDao.updateImagePath(resultId, newPath)
    }

    suspend fun getKeyframeResults(sessionId: Long): List<AnalysisResultEntity> {
        val session = sessionDao.getSessionById(sessionId) ?: return emptyList()
        val idsJson = session.keyframeIndices ?: return emptyList()
        val ids = try {
            JSONArray(idsJson).let { arr ->
                (0 until arr.length()).map { arr.getLong(it) }
            }
        } catch (e: Exception) {
            emptyList()
        }
        if (ids.isEmpty()) return emptyList()
        return analysisResultDao.getResultsByIds(ids)
    }

    suspend fun getCompletedUnsyncedSessions(limit: Int = 5): List<SessionEntity> {
        val allCompleted = sessionDao.getCompletedSessionsAsc()
        val syncedIdsList = sessionSyncDao.getAllSyncedSessionIdsList()
        val syncedIds = syncedIdsList.flatMap { jsonStr ->
            try {
                val arr = JSONArray(jsonStr)
                (0 until arr.length()).map { arr.getLong(it) }
            } catch (_: Exception) { emptyList() }
        }.toSet()
        return allCompleted.filter { it.id !in syncedIds }.take(limit)
    }

    suspend fun getResultsWithEmbeddingsForSessions(
        sessionIds: List<Long>, limit: Int = 50
    ): List<AnalysisResultEntity> {
        return analysisResultDao.getResultsWithEmbeddingsForSessions(sessionIds, limit)
    }

    suspend fun getAllResultsWithEmbeddingsForSessions(
        sessionIds: List<Long>
    ): List<AnalysisResultEntity> {
        return analysisResultDao.getAllResultsWithEmbeddingsForSessions(sessionIds)
    }

    suspend fun deleteSessionsKeepImages(
        context: Context,
        sessionIds: List<Long>,
        keepImagePaths: List<String>
    ) {
        val keepSet = keepImagePaths.toSet()
        for (sid in sessionIds) {
            val results = analysisResultDao.getResultsForSessionOnce(sid)
            for (r in results) {
                val path = r.imagePath
                if (path != null && path !in keepSet) {
                    try { File(path).delete() } catch (_: Exception) {}
                }
            }
            val sessionDir = File(context.filesDir, "session_images/$sid")
            if (sessionDir.exists()) {
                val remaining = sessionDir.listFiles()?.filter { it.absolutePath in keepSet }
                if (remaining.isNullOrEmpty()) {
                    sessionDir.deleteRecursively()
                }
            }
            analysisResultDao.deleteResultsForSession(sid)
            sessionDao.deleteSession(sid)
        }
    }

    suspend fun insertSyncRecord(sync: SessionSyncEntity) {
        sessionSyncDao.insert(sync)
    }

    suspend fun getUnsyncedCompletedSessionCount(): Int {
        val allCompleted = sessionDao.getCompletedSessionsAsc()
        val syncedIdsList = sessionSyncDao.getAllSyncedSessionIdsList()
        val syncedIds = syncedIdsList.flatMap { jsonStr ->
            try {
                val arr = JSONArray(jsonStr)
                (0 until arr.length()).map { arr.getLong(it) }
            } catch (_: Exception) { emptyList() }
        }.toSet()
        return allCompleted.count { it.id !in syncedIds }
    }
}
