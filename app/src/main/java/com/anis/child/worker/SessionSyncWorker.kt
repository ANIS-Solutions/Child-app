package com.anis.child.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.anis.child.data.PreferenceManager
import com.anis.child.data.local.AnalysisResultEntity
import com.anis.child.data.local.SessionSyncEntity
import com.anis.child.data.repository.SessionRepository
import com.anis.child.ml.CondensationEngine
import com.anis.child.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

class SessionSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val sessionRepository: SessionRepository,
    private val apiService: ApiService,
    private val preferenceManager: PreferenceManager
) : CoroutineWorker(appContext, workerParams) {

    private val condensationEngine = CondensationEngine()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val childId = preferenceManager.childId ?: return@withContext Result.failure()
            val sessions = sessionRepository.getCompletedUnsyncedSessions(Int.MAX_VALUE)
            if (sessions.isEmpty()) return@withContext Result.success()
            val sessionIds = sessions.map { it.id }

            val allResults = sessionRepository.getAllResultsWithEmbeddingsForSessions(sessionIds)
            if (allResults.size < 2) return@withContext Result.success()

            val pool = buildCandidatePool(sessions, allResults)
            if (pool.size < 2) return@withContext Result.success()

            val embeddings = pool.map { r ->
                val arr = JSONArray(r.embedding!!)
                FloatArray(arr.length()) { i -> arr.getDouble(i).toFloat() }
            }
            val k = minOf(maxOf(embeddings.size / 10, 3), 10, embeddings.size)
            val indices = condensationEngine.extractKeyframes(embeddings, k)
            val selectedResults = indices.map { pool[it] }

            val allEmbeddingsForUpload = sessionRepository.getResultsWithEmbeddingsForSessions(sessionIds, 50)

            val imageHighlightsArr = JSONArray()
            val listOfEmbeddingArr = JSONArray()
            val imageParts = mutableListOf<MultipartBody.Part>()
            val keepImagePaths = mutableListOf<String>()

            for (result in selectedResults) {
                val embJson = result.embedding?.let {
                    try { JSONArray(it) } catch (_: Exception) { null }
                } ?: continue

                val embList = JSONArray()
                for (i in 0 until embJson.length()) embList.put(embJson.getDouble(i))

                val highlight = JSONObject()
                highlight.put("resultId", result.id)
                highlight.put("sessionId", result.sessionId)
                highlight.put("timestamp", result.timestamp)
                highlight.put("embedding", embList)
                imageHighlightsArr.put(highlight)

                val imagePath = result.imagePath
                if (imagePath != null) {
                    keepImagePaths.add(imagePath)
                    val imageFile = File(imagePath)
                    if (imageFile.exists()) {
                        val fileName = "${result.id}_${result.sessionId}.png"
                        val mediaType = "image/png".toMediaTypeOrNull()
                        val requestBody = imageFile.readBytes().toRequestBody(mediaType)
                        imageParts.add(MultipartBody.Part.createFormData("images", fileName, requestBody))
                    }
                }
            }

            for (result in allEmbeddingsForUpload) {
                val embJson = result.embedding?.let {
                    try { JSONArray(it) } catch (_: Exception) { null }
                } ?: continue
                val embList = JSONArray()
                for (i in 0 until embJson.length()) embList.put(embJson.getDouble(i))
                listOfEmbeddingArr.put(embList)
            }

            val textPlain = "text/plain".toMediaTypeOrNull()
            val response = apiService.syncSessions(
                childId.toRequestBody(textPlain),
                sessions.size.toString().toRequestBody(textPlain),
                imageHighlightsArr.toString().toRequestBody(textPlain),
                listOfEmbeddingArr.toString().toRequestBody(textPlain),
                imageParts.toTypedArray()
            )

            if (response.isSuccessful) {
                val syncEntity = SessionSyncEntity(
                    syncTimestamp = System.currentTimeMillis(),
                    childId = childId,
                    sessionCount = sessions.size,
                    imageCount = imageParts.size,
                    embeddingCount = listOfEmbeddingArr.length(),
                    syncedSessionIds = JSONArray(sessionIds).toString(),
                    selectedImagePaths = JSONArray(keepImagePaths).toString(),
                    serializedEmbeddings = listOfEmbeddingArr.toString(),
                    imageHighlightsJson = imageHighlightsArr.toString()
                )
                sessionRepository.insertSyncRecord(syncEntity)
                sessionRepository.deleteSessionsKeepImages(applicationContext, sessionIds, keepImagePaths)
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            if (runAttemptCount < MAX_RETRY_ATTEMPTS) Result.retry() else Result.failure()
        }
    }

    private suspend fun buildCandidatePool(
        sessions: List<com.anis.child.data.local.SessionEntity>,
        allResults: List<AnalysisResultEntity>
    ): List<AnalysisResultEntity> {
        val keyframes = sessions.flatMap { session ->
            sessionRepository.getKeyframeResults(session.id)
                .filter { it.imagePath != null && it.embedding != null && File(it.imagePath).exists() }
        }
        if (keyframes.size >= 2) {
            val cap = 1000
            return if (keyframes.size > cap) {
                val step = keyframes.size / cap
                keyframes.filterIndexed { i, _ -> i % step == 0 }.take(cap)
            } else keyframes
        }
        val valid = allResults.filter { it.imagePath != null && it.embedding != null && File(it.imagePath).exists() }
        val cap = 1000
        return if (valid.size > cap) {
            val step = valid.size / cap
            valid.filterIndexed { i, _ -> i % step == 0 }.take(cap)
        } else valid
    }

    companion object {
        private const val MAX_RETRY_ATTEMPTS = 3
        const val WORK_NAME = "session_sync"

        fun enqueue(context: Context) {
            val workRequest = OneTimeWorkRequestBuilder<SessionSyncWorker>()
                .setInitialDelay(1, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}
