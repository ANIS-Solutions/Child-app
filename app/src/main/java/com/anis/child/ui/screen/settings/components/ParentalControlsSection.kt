package com.anis.child.ui.screen.settings.components

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.anis.child.ai.SessionState
import com.anis.child.ai.util.PermissionManager
import com.anis.child.R
import com.anis.child.ui.components.PermissionItem
import com.anis.child.ui.screen.ai.AiSessionViewModel
import com.anis.child.ui.theme.LocalAppColors
import androidx.compose.ui.res.stringResource

@Composable
fun ParentalControlsSection(
    onContentProtectionClick: () -> Unit,
    onSessionHistoryClick: () -> Unit,
    isAiFilteringEnabled: Boolean = false,
    onAiFilteringToggle: (Boolean) -> Unit = {},
) {
    val appColors = LocalAppColors.current
    val context = LocalContext.current
    val activity = context as? Activity
    val sessionViewModel: AiSessionViewModel = hiltViewModel()
    val sessionState by sessionViewModel.sessionState.collectAsState()

    val mediaProjectionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        sessionViewModel.setMediaProjectionResult(result.resultCode, result.data ?: Intent())
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    val overlaySettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ -> }

    var refreshKey by remember { mutableStateOf(0) }

    val hasNotification = remember { mutableStateOf(PermissionManager.hasNotificationPermission(context)) }
    val hasOverlay = remember { mutableStateOf(PermissionManager.hasOverlayPermission(context)) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = appColors.darkSurface.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SectionHeader(stringResource(R.string.parental_controls_section))
            SettingsNavRow(
                icon = Icons.Default.Security,
                title = stringResource(R.string.content_protection),
                description = stringResource(R.string.content_protection_desc),
                onClick = onContentProtectionClick
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = appColors.primary01,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.ai_content_filtering),
                        style = MaterialTheme.typography.bodyLarge,
                        color = appColors.textPrimary
                    )
                    Text(
                        text = stringResource(R.string.ai_filtering_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.textSecondary
                    )
                }
                Switch(
                    checked = isAiFilteringEnabled,
                    onCheckedChange = onAiFilteringToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = appColors.primary01,
                        checkedTrackColor = appColors.primary01.copy(alpha = 0.5f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(appColors.darkSurface.copy(alpha = 0.1f), MaterialTheme.shapes.medium)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (sessionState is SessionState.Active) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = appColors.success500,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                    }
                    Text(
                        text = when (val state = sessionState) {
                            is SessionState.Idle -> stringResource(R.string.session_idle)
                            is SessionState.Active -> stringResource(R.string.session_active, state.sessionId.toString())
                            is SessionState.PermissionRequired -> stringResource(R.string.session_permissions_required)
                            is SessionState.MediaProjectionRequired -> stringResource(R.string.session_screen_recording_needed)
                            is SessionState.NotificationPermissionRequired -> stringResource(R.string.session_notification_permission_needed)
                            is SessionState.Error -> stringResource(R.string.error)
                        },
                        color = when (sessionState) {
                            is SessionState.Idle -> appColors.textSecondary
                            is SessionState.Active -> appColors.success500
                            is SessionState.Error -> appColors.error500
                            else -> appColors.warning500
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (val state = sessionState) {
                is SessionState.Idle -> {
                    Button(
                        onClick = { activity?.let { sessionViewModel.startSession(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = appColors.primary01)
                    ) {
                        Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(20.dp))
                        Text(stringResource(R.string.start_monitoring), color = appColors.darkTextPrimary, modifier = Modifier.padding(start = 8.dp))
                    }
                }

                is SessionState.Active -> {
                    Button(
                        onClick = { sessionViewModel.stopSession() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = appColors.error500)
                    ) {
                        Icon(Icons.Default.Stop, null, modifier = Modifier.size(20.dp))
                        Text(stringResource(R.string.stop_monitoring), color = appColors.darkTextPrimary, modifier = Modifier.padding(start = 8.dp))
                    }
                }

                is SessionState.MediaProjectionRequired -> {
                    Text(
                        text = stringResource(R.string.screen_recording_needed_desc),
                        color = appColors.textSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { mediaProjectionLauncher.launch(state.intent) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = appColors.primary01)
                    ) {
                        Text(stringResource(R.string.grant_screen_recording), color = appColors.darkTextPrimary)
                    }
                }

                is SessionState.PermissionRequired -> {
                    Text(
                        text = stringResource(R.string.permissions_missing),
                        color = appColors.error500,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { sessionViewModel.clearPermissionState() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.back))
                    }
                }

                is SessionState.Error -> {
                    Text(
                        text = stringResource(R.string.error_with_message, state.message),
                        color = appColors.error500,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { sessionViewModel.clearPermissionState() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.retry))
                    }
                }

                is SessionState.NotificationPermissionRequired -> {
                    Text(
                        text = stringResource(R.string.notification_permission_needed),
                        color = appColors.textSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            PermissionItem(
                title = stringResource(R.string.permission_notification),
                description = if (hasNotification.value) stringResource(R.string.granted) else stringResource(R.string.notification_permission_desc),
                isGranted = hasNotification.value,
                onClick = {
                    if (activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            )

            PermissionItem(
                title = stringResource(R.string.permission_overlay),
                description = if (hasOverlay.value) stringResource(R.string.granted) else stringResource(R.string.overlay_permission_desc),
                isGranted = hasOverlay.value,
                onClick = {
                    if (activity != null) {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${activity.packageName}")
                        )
                        overlaySettingsLauncher.launch(intent)
                    }
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            SettingsNavRow(
                icon = Icons.Default.Timeline,
                title = stringResource(R.string.session_history),
                description = stringResource(R.string.session_history_desc),
                onClick = onSessionHistoryClick
            )
        }
    }
}
