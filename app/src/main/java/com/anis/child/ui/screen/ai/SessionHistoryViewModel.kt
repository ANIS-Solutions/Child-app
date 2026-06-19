package com.anis.child.ui.screen.ai

import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anis.child.data.PreferenceManager
import com.anis.child.data.local.SessionSyncDao
import com.anis.child.data.local.SessionSyncEntity
import com.anis.child.data.repository.SessionRepository
import com.anis.child.ml.CondensationEngine
import com.anis.child.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SessionHistoryViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val sessionSyncDao: SessionSyncDao,
    private val apiService: ApiService,
    private val preferenceManager: PreferenceManager,
    @ApplicationContext private val context: android.content.Context
) : ViewModel() {

    val sessions: StateFlow<List<com.anis.child.data.local.SessionEntity>> = sessionRepository.getAllSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _unsyncedCount = MutableStateFlow(0)
    val unsyncedCount: StateFlow<Int> = _unsyncedCount.asStateFlow()

    private val _voteState = MutableStateFlow(VoteAllState())
    val voteState: StateFlow<VoteAllState> = _voteState.asStateFlow()

    private val condensationEngine = CondensationEngine()

    init {
        viewModelScope.launch {
            combine(
                sessionRepository.getAllSessions(),
                sessionSyncDao.getAllSyncs()
            ) { sessions, syncs ->
                val syncedIds = syncs.flatMap { s ->
                    try {
                        val arr = org.json.JSONArray(s.syncedSessionIds)
                        (0 until arr.length()).map { arr.getLong(it) }
                    } catch (_: Exception) { emptyList() }
                }.toSet()
                sessions.count { it.status == "COMPLETED" && it.id !in syncedIds }
            }.collect { count ->
                _unsyncedCount.value = count
            }
        }
    }

    fun voteAllSessions() {
        if (_voteState.value.status == VoteStatus.Voting) return
        _voteState.value = VoteAllState(status = VoteStatus.Voting)
        viewModelScope.launch {
            try {
                val unsyncedSessions = sessionRepository.getCompletedUnsyncedSessions(Int.MAX_VALUE)
                if (unsyncedSessions.isEmpty()) {
                    _voteState.value = VoteAllState(
                        status = VoteStatus.Idle,
                        error = "No unsynced sessions to vote on"
                    )
                    return@launch
                }

                val sessionIds = unsyncedSessions.map { it.id }
                val allResults = withContext(Dispatchers.IO) {
                    sessionRepository.getAllResultsWithEmbeddingsForSessions(sessionIds)
                }

                val pool = withContext(Dispatchers.IO) {
                    val keyframes = unsyncedSessions.flatMap { session ->
                        sessionRepository.getKeyframeResults(session.id)
                            .filter { it.imagePath != null && it.embedding != null && File(it.imagePath).exists() }
                    }
                    if (keyframes.size >= 2) keyframes
                    else allResults.filter { it.imagePath != null && it.embedding != null && File(it.imagePath).exists() }
                }
                if (pool.size < 2) {
                    _voteState.value = VoteAllState(
                        status = VoteStatus.Idle,
                        error = "Not enough results with existing images (need at least 2)"
                    )
                    return@launch
                }

                val MAX_FOR_VOTING = 1000
                val resultsForVoting = if (pool.size > MAX_FOR_VOTING) {
                    val step = pool.size / MAX_FOR_VOTING
                    pool.filterIndexed { i, _ -> i % step == 0 }.take(MAX_FOR_VOTING)
                } else pool

                val embeddings = resultsForVoting.map { result ->
                    val arr = JSONArray(result.embedding!!)
                    FloatArray(arr.length()) { i -> arr.getDouble(i).toFloat() }
                }

                val k = minOf(maxOf(embeddings.size / 5, 3), 10, embeddings.size)
                val indices = condensationEngine.extractKeyframes(embeddings, k)
                val selectedResults = indices.map { resultsForVoting[it] }

                val allEmbeddingsForUpload = withContext(Dispatchers.IO) {
                    sessionRepository.getResultsWithEmbeddingsForSessions(sessionIds, 50)
                }

                _voteState.value = VoteAllState(
                    status = VoteStatus.Previewing,
                    selectedResults = selectedResults,
                    allEmbeddings = allEmbeddingsForUpload,
                    selectedSessionIds = sessionIds
                )
            } catch (e: Exception) {
                _voteState.value = VoteAllState(
                    status = VoteStatus.Idle,
                    error = e.message
                )
            }
        }
    }

    fun sendVotedData() {
        val current = _voteState.value
        if (current.status != VoteStatus.Previewing || current.selectedResults.isEmpty()) return
        _voteState.value = current.copy(status = VoteStatus.Sending, error = null)

        viewModelScope.launch {
            try {
                val childId = preferenceManager.childId
                if (childId == null) {
                    _voteState.value = current.copy(status = VoteStatus.Previewing, error = "Child ID not found")
                    return@launch
                }

                val allResults = current.allEmbeddings
                val selectedResults = current.selectedResults
                val sessionIds = current.selectedSessionIds

                val imageHighlightsArr = JSONArray()
                val listOfEmbeddingArr = JSONArray()
                val imageParts = mutableListOf<MultipartBody.Part>()
                val keepImagePaths = mutableListOf<String>()

                for (result in selectedResults) {
                    val embJson = result.embedding?.let {
                        try { JSONArray(it) } catch (_: Exception) { null }
                    } ?: continue

                    val embList = JSONArray()
                    for (i in 0 until embJson.length()) {
                        embList.put(embJson.getDouble(i))
                    }

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

                for (result in allResults) {
                    val embJson = result.embedding?.let {
                        try { JSONArray(it) } catch (_: Exception) { null }
                    } ?: continue
                    val embList = JSONArray()
                    for (i in 0 until embJson.length()) {
                        embList.put(embJson.getDouble(i))
                    }
                    listOfEmbeddingArr.put(embList)
                }

                val textPlain = "text/plain".toMediaTypeOrNull()
                val childIdBody = childId.toRequestBody(textPlain)
                val totalSessionsBody = sessionIds.size.toString().toRequestBody(textPlain)
                val highlightsBody = imageHighlightsArr.toString().toRequestBody(textPlain)
                val embeddingsBody = listOfEmbeddingArr.toString().toRequestBody(textPlain)

                val response = apiService.syncSessions(
                    childIdBody, totalSessionsBody, highlightsBody, embeddingsBody,
                    imageParts.toTypedArray()
                )
                if (response.isSuccessful) {
                    val syncEntity = SessionSyncEntity(
                        syncTimestamp = System.currentTimeMillis(),
                        childId = childId,
                        sessionCount = sessionIds.size,
                        imageCount = imageParts.size,
                        embeddingCount = listOfEmbeddingArr.length(),
                        syncedSessionIds = JSONArray(sessionIds).toString(),
                        selectedImagePaths = JSONArray(keepImagePaths).toString(),
                        serializedEmbeddings = listOfEmbeddingArr.toString(),
                        imageHighlightsJson = imageHighlightsArr.toString()
                    )
                    sessionRepository.insertSyncRecord(syncEntity)
                    sessionRepository.deleteSessionsKeepImages(context, sessionIds, keepImagePaths)

                    _voteState.value = VoteAllState(status = VoteStatus.Sent)
                } else {
                    _voteState.value = current.copy(status = VoteStatus.Previewing, error = "Server error: ${response.message()}")
                }
            } catch (e: Exception) {
                _voteState.value = current.copy(status = VoteStatus.Previewing, error = e.message)
            }
        }
    }

    fun resetVoteState() {
        _voteState.value = VoteAllState()
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            sessionRepository.deleteSession(sessionId)
        }
    }
}
