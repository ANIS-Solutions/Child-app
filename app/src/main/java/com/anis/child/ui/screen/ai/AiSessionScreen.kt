package com.anis.child.ui.screen.ai

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anis.child.ai.SessionState
import com.anis.child.ui.theme.LocalAppColors

@Composable
fun AiSessionScreen(
    viewModel: AiSessionViewModel,
    onBack: () -> Unit
) {
    val appColors = LocalAppColors.current
    val context = LocalContext.current
    val activity = context as? Activity
    val sessionState by viewModel.sessionState.collectAsState()

    val mediaProjectionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.setMediaProjectionResult(result.resultCode, result.data ?: Intent())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(appColors.surface50)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = appColors.textPrimary)
            }
            Text(
                text = "AI Content Monitoring",
                color = appColors.textPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Session Status",
                style = MaterialTheme.typography.titleMedium,
                color = appColors.textPrimary
            )

            val currentState = sessionState
            StatusCard(currentState)

            Spacer(modifier = Modifier.height(8.dp))

            when (currentState) {
                is SessionState.Idle -> {
                    Button(
                        onClick = {
                            activity?.let { viewModel.startSession(it) }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = appColors.primary01)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Start Monitoring",
                            color = appColors.darkTextPrimary,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                is SessionState.Active -> {
                    Button(
                        onClick = { viewModel.stopSession() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = appColors.error500)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Stop Monitoring",
                            color = appColors.darkTextPrimary,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                is SessionState.PermissionRequired -> {
                    Text(
                        text = "Required permissions are missing. Please grant them in settings.",
                        color = appColors.error500,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(
                        onClick = { viewModel.clearPermissionState() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Back")
                    }
                }

                is SessionState.MediaProjectionRequired -> {
                    Text(
                        text = "Screen recording permission is needed for content analysis.",
                        color = appColors.textSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(
                        onClick = {
                            mediaProjectionLauncher.launch(currentState.intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = appColors.primary01)
                    ) {
                        Text(
                            text = "Grant Screen Recording",
                            color = appColors.darkTextPrimary
                        )
                    }
                }

                is SessionState.Error -> {
                    Text(
                        text = "Error: ${currentState.message}",
                        color = appColors.error500,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(
                        onClick = { viewModel.clearPermissionState() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Retry")
                    }
                }

                is SessionState.NotificationPermissionRequired -> {
                    Text(
                        text = "Notification permission is needed.",
                        color = appColors.textSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusCard(state: SessionState) {
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
