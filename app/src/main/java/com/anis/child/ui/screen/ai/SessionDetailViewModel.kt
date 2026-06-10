package com.anis.child.ui.screen.ai

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anis.child.ai.util.SessionExporter
import com.anis.child.data.local.AnalysisResultEntity
import com.anis.child.data.local.SessionEntity
import com.anis.child.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionDetailViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _session = MutableStateFlow<SessionEntity?>(null)
    val session: StateFlow<SessionEntity?> = _session.asStateFlow()

    private val _results = MutableStateFlow<List<AnalysisResultEntity>>(emptyList())
    val results: StateFlow<List<AnalysisResultEntity>> = _results.asStateFlow()

    private val _exportUri = MutableStateFlow<Uri?>(null)
    val exportUri: StateFlow<Uri?> = _exportUri.asStateFlow()

    fun loadSession(sessionId: Long) {
        viewModelScope.launch {
            sessionRepository.getSessionByIdFlow(sessionId).collect { sessionEntity ->
                _session.value = sessionEntity
            }
        }

        viewModelScope.launch {
            sessionRepository.getResultsForSessionDesc(sessionId).collect { resultsList ->
                _results.value = resultsList
            }
        }
    }

    fun exportSession() {
        viewModelScope.launch {
            val s = _session.value ?: return@launch
            val r = _results.value
            val uri = SessionExporter.exportSession(context, s, r)
            _exportUri.value = uri
        }
    }

    fun clearExportUri() {
        _exportUri.value = null
    }
}
