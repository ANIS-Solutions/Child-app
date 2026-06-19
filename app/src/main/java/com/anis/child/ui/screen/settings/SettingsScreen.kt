package com.anis.child.ui.screen.settings

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.anis.child.ai.SessionState
import com.anis.child.ai.util.PermissionManager
import com.anis.child.R
import com.anis.child.ui.components.PermissionItem
import com.anis.child.ui.screen.ai.AiSessionViewModel
import com.anis.child.ui.theme.LocalAppColors

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onMonitoringChange: (Boolean) -> Unit,
    onContentProtectionClick: () -> Unit = {},
    onSessionHistoryClick: () -> Unit = {},
    onLogsClick: () -> Unit = {},
    onChangePin: () -> Unit = {},
    onLogout: () -> Unit,
) {
    val appColors = LocalAppColors.current
    val context = LocalContext.current
    var showDisableAiDialog by remember { mutableStateOf(false) }

    if (showDisableAiDialog) {
        AlertDialog(
            onDismissRequest = { showDisableAiDialog = false },
            title = { Text(stringResource(R.string.disable_ai_dialog_title)) },
            text = {
                Text(stringResource(R.string.disable_ai_dialog_text))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.confirmDisableAiFiltering()
                        showDisableAiDialog = false
                    }
                ) {
                    Text(stringResource(R.string.disable), color = appColors.error500)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisableAiDialog = false }) {
                    Text(stringResource(R.string.keep_enabled))
                }
            }
        )
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), tint = appColors.textPrimary)
            }
            Text(
                text = stringResource(R.string.settings),
                color = appColors.textPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MonitoringSection(
                isMonitoringEnabled = viewModel.isMonitoringEnabled,
                onMonitoringChange = onMonitoringChange,
                onSendLocationClick = { viewModel.sendCurrentLocation() },
                isSending = viewModel.isSending,
                onSendAppsClick = { viewModel.sendInstalledApps() },
                isSendingApps = viewModel.isSendingApps,
                isSendingUsage = viewModel.isSendingUsage,
                onSendUsageClick = { viewModel.sendDailyUsage() }
            )

            ParentalControlsSection(
                onContentProtectionClick = onContentProtectionClick,
                onSessionHistoryClick = onSessionHistoryClick,
                isAiFilteringEnabled = viewModel.isAiFilteringEnabled,
                onAiFilteringToggle = { enabled ->
                    if (enabled) {
                        viewModel.onAiFilteringToggled(true)
                    } else {
                        showDisableAiDialog = true
                    }
                }
            )

            ActivityHistorySection(
                onLogsClick = onLogsClick,
            )

            DeviceInfoSection(
                childId = viewModel.childId,
                childName = viewModel.childName,
            )

            PermissionsSection()

            AccountSection(
                onGetMeClick = { viewModel.fetchChildMe() },
                isFetchingChild = viewModel.isFetchingChild,
                onChangePin = onChangePin,
                onLogout = onLogout
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    val appColors = LocalAppColors.current
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = appColors.textSecondary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 4.dp)
    )
}

@Composable
private fun MonitoringSection(
    isMonitoringEnabled: Boolean,
    onMonitoringChange: (Boolean) -> Unit,
    onSendLocationClick: () -> Unit,
    isSending: Boolean,
    onSendAppsClick: () -> Unit,
    isSendingApps: Boolean,
    isSendingUsage: Boolean = false,
    onSendUsageClick: () -> Unit = {},
) {
    val appColors = LocalAppColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = appColors.darkSurface.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionHeader(stringResource(R.string.monitoring_section))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = appColors.primary01,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.location_monitoring),
                        style = MaterialTheme.typography.bodyLarge,
                        color = appColors.textPrimary
                    )
                    Text(
                        text = stringResource(R.string.location_monitoring_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.textSecondary
                    )
                }
                Switch(
                    checked = isMonitoringEnabled,
                    onCheckedChange = onMonitoringChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = appColors.primary01,
                        checkedTrackColor = appColors.primary01.copy(alpha = 0.5f)
                    )
                )
            }

            ActionButton(
                icon = Icons.Default.LocationOn,
                label = stringResource(R.string.send_location),
                isLoading = isSending,
                loadingLabel = stringResource(R.string.sending_location),
                onClick = onSendLocationClick
            )

            ActionButton(
                icon = Icons.Default.Apps,
                label = stringResource(R.string.send_apps),
                isLoading = isSendingApps,
                loadingLabel = stringResource(R.string.sending_apps),
                onClick = onSendAppsClick
            )

            ActionButton(
                icon = Icons.Default.Schedule,
                label = stringResource(R.string.send_daily_usage),
                isLoading = isSendingUsage,
                loadingLabel = stringResource(R.string.sending_daily_usage),
                onClick = onSendUsageClick
            )
        }
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    isLoading: Boolean,
    loadingLabel: String,
    onClick: () -> Unit,
) {
    val appColors = LocalAppColors.current
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = appColors.darkSurface.copy(alpha = 0.15f)
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = appColors.textPrimary,
                strokeWidth = 2.dp
            )
            Text(
                text = loadingLabel,
                color = appColors.textPrimary,
                modifier = Modifier.padding(start = 8.dp)
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = appColors.textPrimary
            )
            Text(
                text = label,
                color = appColors.textPrimary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun ParentalControlsSection(
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

    fun refreshPermissions() {
        refreshKey++
        hasNotification.value = PermissionManager.hasNotificationPermission(context)
        hasOverlay.value = PermissionManager.hasOverlayPermission(context)
    }

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

@Composable
private fun ActivityHistorySection(
    onLogsClick: () -> Unit,
) {
    val appColors = LocalAppColors.current
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
            SectionHeader(stringResource(R.string.activity_logs_section))
            SettingsNavRow(
                icon = Icons.Default.Apps,
                title = stringResource(R.string.app_logs),
                description = stringResource(R.string.app_logs_desc),
                onClick = onLogsClick
            )
        }
    }
}

@Composable
private fun DeviceInfoSection(childId: String?, childName: String?) {
    val appColors = LocalAppColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = appColors.darkSurface.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionHeader(stringResource(R.string.device_section))
            if (childName != null) {
                InfoRow(stringResource(R.string.device_name), childName)
            }
            if (childId != null) {
                InfoRow(stringResource(R.string.child_id), childId.take(16) + "...")
            }
            InfoRow(stringResource(R.string.device_model), Build.MODEL)
            InfoRow(stringResource(R.string.android_version), "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        }
    }
}

@Composable
private fun PermissionsSection() {
    val appColors = LocalAppColors.current
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = appColors.darkSurface.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionHeader(stringResource(R.string.permissions_section))

            val permissions = listOf(
                Manifest.permission.CAMERA to stringResource(R.string.permission_camera),
                Manifest.permission.ACCESS_FINE_LOCATION to stringResource(R.string.permission_precise_location),
                Manifest.permission.ACCESS_COARSE_LOCATION to stringResource(R.string.permission_approx_location),
                Manifest.permission.INTERNET to stringResource(R.string.permission_internet)
            )

            permissions.forEach { (permission, label) ->
                val isGranted = context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
                PermissionRow(name = label, isGranted = isGranted)
            }
        }
    }
}

@Composable
private fun PermissionRow(name: String, isGranted: Boolean) {
    val appColors = LocalAppColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = appColors.textSecondary,
            modifier = Modifier.weight(1f)
        )
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isGranted) appColors.success500.copy(alpha = 0.15f)
            else appColors.error500.copy(alpha = 0.15f)
        ) {
            Text(
                text = if (isGranted) stringResource(R.string.granted) else stringResource(R.string.denied),
                style = MaterialTheme.typography.labelSmall,
                color = if (isGranted) appColors.success500 else appColors.error500,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun AccountSection(
    onGetMeClick: () -> Unit,
    isFetchingChild: Boolean,
    onChangePin: () -> Unit,
    onLogout: () -> Unit,
) {
    val appColors = LocalAppColors.current
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
            SectionHeader(stringResource(R.string.account_security_section))

            ActionButton(
                icon = Icons.Default.Person,
                label = stringResource(R.string.get_child_info),
                isLoading = isFetchingChild,
                loadingLabel = stringResource(R.string.fetching),
                onClick = onGetMeClick
            )

            SettingsNavRow(
                icon = Icons.Default.Lock,
                title = stringResource(R.string.change_pin),
                description = stringResource(R.string.change_pin_desc),
                onClick = onChangePin
            )

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = appColors.error500
                )
            ) {
                Text(
                    text = stringResource(R.string.logout),
                    color = appColors.darkTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SettingsNavRow(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    val appColors = LocalAppColors.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = appColors.darkSurface.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = appColors.primary01,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = appColors.textPrimary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = appColors.textSecondary
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = appColors.textDisabled,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    val appColors = LocalAppColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = appColors.textSecondary,
            modifier = Modifier.weight(0.35f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = appColors.textPrimary
        )
    }
}
