package com.anis.child.ui.screen.ai

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anis.child.ai.SessionManager
import com.anis.child.ai.SessionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiSessionViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    val sessionState: StateFlow<SessionState> = sessionManager.sessionState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SessionState.Idle)

    fun startSession(activity: Activity) {
        viewModelScope.launch {
            sessionManager.startSession(activity)
        }
    }

    fun stopSession() {
        sessionManager.stopSession()
    }

    fun setMediaProjectionResult(resultCode: Int, data: Intent) {
        sessionManager.setMediaProjectionResult(resultCode, data)
    }

    fun clearPermissionState() {
        sessionManager.clearPermissionState()
    }

    fun clearMediaProjectionState() {
        sessionManager.clearMediaProjectionState()
    }

    override fun onCleared() {
        super.onCleared()
        sessionManager.onDestroy()
    }
}
