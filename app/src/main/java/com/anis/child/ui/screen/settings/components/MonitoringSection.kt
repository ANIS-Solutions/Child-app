package com.anis.child.ui.screen.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anis.child.R
import androidx.compose.ui.res.stringResource
import com.anis.child.ui.theme.LocalAppColors

@Composable
fun MonitoringSection(
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
