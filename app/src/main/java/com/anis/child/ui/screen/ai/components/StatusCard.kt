package com.anis.child.ui.screen.ai.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anis.child.ai.SessionState
import com.anis.child.ui.theme.LocalAppColors

@Composable
fun StatusCard(state: SessionState) {
    val appColors = LocalAppColors.current
    val (statusText, statusColor) = when (state) {
        is SessionState.Idle -> "Idle" to appColors.textSecondary
        is SessionState.Active -> "Active - Session #${state.sessionId}" to appColors.success500
        is SessionState.PermissionRequired -> "Permissions Required" to appColors.warning500
        is SessionState.MediaProjectionRequired -> "Screen Recording Needed" to appColors.warning500
        is SessionState.NotificationPermissionRequired -> "Notification Permission Needed" to appColors.warning500
        is SessionState.Error -> "Error" to appColors.error500
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(appColors.darkSurface.copy(alpha = 0.1f), MaterialTheme.shapes.medium)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state is SessionState.Active) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = appColors.success500,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.size(8.dp))
            }
            Text(
                text = statusText,
                color = statusColor,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
