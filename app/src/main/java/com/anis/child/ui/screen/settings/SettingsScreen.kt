package com.anis.child.ui.screen.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Timeline
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anis.child.ui.screen.home.LogSection
import com.anis.child.ui.theme.AppColors

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onMonitoringChange: (Boolean) -> Unit,
    onScreenTimeClick: () -> Unit = {},
    onContentProtectionClick: () -> Unit = {},
    onAiSessionClick: () -> Unit = {},
    onLocationHistoryClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onChangePin: () -> Unit = {},
    onLogout: () -> Unit,
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.surface50)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = AppColors.textPrimary)
            }
            Text(
                text = "Settings",
                color = AppColors.textPrimary,
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
                isSendingApps = viewModel.isSendingApps
            )

            ParentalControlsSection(
                onScreenTimeClick = onScreenTimeClick,
                onContentProtectionClick = onContentProtectionClick,
                onAiSessionClick = onAiSessionClick,
            )

            ActivityHistorySection(
                onLocationHistoryClick = onLocationHistoryClick,
                onNotificationsClick = onNotificationsClick,
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

        LogSection(
            logManager = viewModel.logManager,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = AppColors.textSecondary,
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
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.darkSurface.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionHeader("Monitoring")

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = AppColors.primary01,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Location Monitoring",
                        style = MaterialTheme.typography.bodyLarge,
                        color = AppColors.textPrimary
                    )
                    Text(
                        text = "Share location with parent every hour",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.textSecondary
                    )
                }
                Switch(
                    checked = isMonitoringEnabled,
                    onCheckedChange = onMonitoringChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AppColors.primary01,
                        checkedTrackColor = AppColors.primary01.copy(alpha = 0.5f)
                    )
                )
            }

            ActionButton(
                icon = Icons.Default.LocationOn,
                label = "Send Location",
                isLoading = isSending,
                loadingLabel = "Sending Location...",
                onClick = onSendLocationClick
            )

            ActionButton(
                icon = Icons.Default.Apps,
                label = "Send Apps",
                isLoading = isSendingApps,
                loadingLabel = "Sending Apps...",
                onClick = onSendAppsClick
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
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.darkSurface.copy(alpha = 0.15f)
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = AppColors.textPrimary,
                strokeWidth = 2.dp
            )
            Text(
                text = loadingLabel,
                color = AppColors.textPrimary,
                modifier = Modifier.padding(start = 8.dp)
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = AppColors.textPrimary
            )
            Text(
                text = label,
                color = AppColors.textPrimary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun ParentalControlsSection(
    onScreenTimeClick: () -> Unit,
    onContentProtectionClick: () -> Unit,
    onAiSessionClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.darkSurface.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SectionHeader("Parental Controls")
            SettingsNavRow(
                icon = Icons.Default.Schedule,
                title = "Screen Time",
                description = "Manage daily usage limits and app timers",
                onClick = onScreenTimeClick
            )
            SettingsNavRow(
                icon = Icons.Default.Security,
                title = "Content Protection",
                description = "Block inappropriate content and websites",
                onClick = onContentProtectionClick
            )
            SettingsNavRow(
                icon = Icons.Default.PlayArrow,
                title = "AI Content Monitoring",
                description = "Monitor screen content with AI analysis",
                onClick = onAiSessionClick
            )
        }
    }
}

@Composable
private fun ActivityHistorySection(
    onLocationHistoryClick: () -> Unit,
    onNotificationsClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.darkSurface.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SectionHeader("Activity Logs")
            SettingsNavRow(
                icon = Icons.Default.Timeline,
                title = "Location History",
                description = "View past location reports",
                onClick = onLocationHistoryClick
            )
            SettingsNavRow(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                description = "Browse notification history",
                onClick = onNotificationsClick
            )
        }
    }
}

@Composable
private fun DeviceInfoSection(childId: String?, childName: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.darkSurface.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionHeader("Device")
            if (childName != null) {
                InfoRow("Name", childName)
            }
            if (childId != null) {
                InfoRow("Child ID", childId.take(16) + "...")
            }
            InfoRow("Device", Build.MODEL)
            InfoRow("Android", "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        }
    }
}

@Composable
private fun PermissionsSection() {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.darkSurface.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionHeader("Permissions")

            val permissions = listOf(
                Manifest.permission.CAMERA to "Camera",
                Manifest.permission.ACCESS_FINE_LOCATION to "Precise Location",
                Manifest.permission.ACCESS_COARSE_LOCATION to "Approximate Location",
                Manifest.permission.INTERNET to "Internet"
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.textSecondary,
            modifier = Modifier.weight(1f)
        )
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isGranted) AppColors.success500.copy(alpha = 0.15f)
            else AppColors.error500.copy(alpha = 0.15f)
        ) {
            Text(
                text = if (isGranted) "Granted" else "Denied",
                style = MaterialTheme.typography.labelSmall,
                color = if (isGranted) AppColors.success500 else AppColors.error500,
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.darkSurface.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SectionHeader("Account & Security")

            ActionButton(
                icon = Icons.Default.Person,
                label = "Get Child Info",
                isLoading = isFetchingChild,
                loadingLabel = "Fetching...",
                onClick = onGetMeClick
            )

            SettingsNavRow(
                icon = Icons.Default.Lock,
                title = "Change PIN",
                description = "Update your security PIN",
                onClick = onChangePin
            )

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.error500
                )
            ) {
                Text(
                    text = "Logout",
                    color = AppColors.darkTextPrimary,
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
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = AppColors.darkSurface.copy(alpha = 0.08f)
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
                tint = AppColors.primary01,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppColors.textPrimary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.textSecondary
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = AppColors.textDisabled,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.textSecondary,
            modifier = Modifier.weight(0.35f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.textPrimary
        )
    }
}
