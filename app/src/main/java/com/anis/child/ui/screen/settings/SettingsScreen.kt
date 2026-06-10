package com.anis.child.ui.screen.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anis.child.data.LogManager
import com.anis.child.ui.screen.home.LogSection
import com.anis.child.ui.theme.AppColors
import com.anis.child.util.resolveDeviceId

@Composable
fun SettingsScreen(
    logManager: LogManager,
    isMonitoringEnabled: Boolean,
    childId: String?,
    onMonitoringChange: (Boolean) -> Unit,
    onSendLocationClick: () -> Unit,
    isSending: Boolean,
    onSendAppsClick: () -> Unit,
    isSendingApps: Boolean,
    onGetMeClick: () -> Unit,
    isFetchingChild: Boolean,
    onScreenTimeClick: () -> Unit = {},
    onContentProtectionClick: () -> Unit = {},
    onLocationHistoryClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onBack: () -> Unit,
    onChangePin: () -> Unit = {},
    onLogout: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.surface50)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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

        SettingsToggleRow(
            title = "Location Monitoring",
            description = "Share location with parent every hour",
            isChecked = isMonitoringEnabled,
            onCheckedChange = onMonitoringChange
        )

        PermissionsSection()

        DeviceInfoSection(childId = childId)

        Button(
            onClick = onScreenTimeClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.primary01
            )
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Screen Time",
                color = AppColors.darkTextPrimary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Button(
            onClick = onContentProtectionClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.primary01
            )
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Content Protection",
                color = AppColors.darkTextPrimary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Button(
            onClick = onLocationHistoryClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.primary01
            )
        ) {
            Icon(
                imageVector = Icons.Default.Timeline,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Location History",
                color = AppColors.darkTextPrimary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Button(
            onClick = onNotificationsClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.primary01
            )
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Notifications",
                color = AppColors.darkTextPrimary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Button(
            onClick = onSendLocationClick,
            enabled = !isSending,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.primary01
            )
        ) {
            if (isSending) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = AppColors.darkTextPrimary,
                    strokeWidth = 2.dp
                )
                Text(
                    text = "Sending Location...",
                    color = AppColors.darkTextPrimary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Send Location",
                    color = AppColors.darkTextPrimary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        Button(
            onClick = onSendAppsClick,
            enabled = !isSendingApps,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.primary01
            )
        ) {
            if (isSendingApps) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = AppColors.darkTextPrimary,
                    strokeWidth = 2.dp
                )
                Text(
                    text = "Sending Apps...",
                    color = AppColors.darkTextPrimary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Send Apps",
                    color = AppColors.darkTextPrimary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        Button(
            onClick = onGetMeClick,
            enabled = !isFetchingChild,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.primary01
            )
        ) {
            if (isFetchingChild) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = AppColors.darkTextPrimary,
                    strokeWidth = 2.dp
                )
                Text(
                    text = "Fetching...",
                    color = AppColors.darkTextPrimary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Get Me",
                    color = AppColors.darkTextPrimary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        Button(
            onClick = onChangePin,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.primary01
            )
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Change PIN",
                color = AppColors.darkTextPrimary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.error500
            )
        ) {
            Text(
                text = "Logout",
                color = AppColors.darkTextPrimary
            )
        }
    }

        LogSection(
            logManager = logManager,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PermissionsSection() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                AppColors.darkSurface.copy(alpha = 0.1f),
                MaterialTheme.shapes.medium
            )
            .padding(16.dp)
    ) {
        Text(
            text = "Permissions",
            style = MaterialTheme.typography.bodyLarge,
            color = AppColors.textPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        val permissions = listOf(
            Manifest.permission.CAMERA to "Camera",
            Manifest.permission.ACCESS_FINE_LOCATION to "Precise Location",
            Manifest.permission.ACCESS_COARSE_LOCATION to "Approximate Location",
            Manifest.permission.INTERNET to "Internet"
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            permissions.forEach { (permission, label) ->
                val isGranted = context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
                PermissionRow(
                    name = label,
                    isGranted = isGranted
                )
            }
        }
    }
}

@Composable
private fun PermissionRow(
    name: String,
    isGranted: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.textSecondary
        )
        Icon(
            imageVector = if (isGranted) Icons.Default.Check else Icons.Default.Close,
            contentDescription = if (isGranted) "Granted" else "Denied",
            tint = if (isGranted) AppColors.success500 else AppColors.error500
        )
    }
}

@Composable
private fun DeviceInfoSection(childId: String?) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                AppColors.darkSurface.copy(alpha = 0.1f),
                MaterialTheme.shapes.medium
            )
            .padding(16.dp)
    ) {
        Text(
            text = "Device Information",
            style = MaterialTheme.typography.bodyLarge,
            color = AppColors.textPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (childId != null) {
                InfoRowScrollable("Child ID", childId)
            }
            InfoRow("Device", Build.MODEL)
            InfoRow("Android Version", Build.VERSION.RELEASE)
            InfoRow("Android ID", context.resolveDeviceId())
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.textSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.textPrimary
        )
    }
}

@Composable
private fun InfoRowScrollable(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.textSecondary
        )
        Text(
            text = value.take(8) + "...",
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace
            ),
            color = AppColors.textPrimary,
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(end = 8.dp)
        )
    }
}

@Composable
private fun Spacer(modifier: Modifier) {
    androidx.compose.foundation.layout.Box(modifier = modifier)
}

@Composable
private fun SettingsToggleRow(
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                AppColors.darkSurface.copy(alpha = 0.1f),
                MaterialTheme.shapes.medium
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
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
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AppColors.primary01,
                checkedTrackColor = AppColors.primary01.copy(alpha = 0.5f)
            )
        )
    }
}