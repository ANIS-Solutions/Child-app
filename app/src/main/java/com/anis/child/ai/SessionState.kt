package com.anis.child.ai

import android.content.Intent
import com.anis.child.ai.util.PermissionType

sealed class SessionState {
    data object Idle : SessionState()
    data class Active(val sessionId: Long) : SessionState()
    data class PermissionRequired(val missingPermissions: List<PermissionType>) : SessionState()
    data class MediaProjectionRequired(val intent: Intent) : SessionState()
    data object NotificationPermissionRequired : SessionState()
    data class Error(val message: String) : SessionState()
}
