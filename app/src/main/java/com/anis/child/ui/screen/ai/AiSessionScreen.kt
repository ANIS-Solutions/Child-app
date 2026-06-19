package com.anis.child.ui.screen.ai

import com.anis.child.ui.screen.ai.components.*
import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
        AiSessionTopBar(onBack = onBack)

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
                    StartMonitoringButton(
                        onClick = {
                            activity?.let { viewModel.startSession(it) }
                        }
                    )
                }

                is SessionState.Active -> {
                    StopMonitoringButton(
                        onClick = { viewModel.stopSession() }
                    )
                }

                is SessionState.PermissionRequired -> {
                    PermissionRequiredMessage(
                        onBack = { viewModel.clearPermissionState() }
                    )
                }

                is SessionState.MediaProjectionRequired -> {
                    MediaProjectionRequest(
                        onGrant = {
                            mediaProjectionLauncher.launch(currentState.intent)
                        }
                    )
                }

                is SessionState.Error -> {
                    ErrorStateMessage(
                        message = currentState.message,
                        onRetry = { viewModel.clearPermissionState() }
                    )
                }

                is SessionState.NotificationPermissionRequired -> {
                    NotificationPermissionMessage()
                }
            }
        }
    }
}
