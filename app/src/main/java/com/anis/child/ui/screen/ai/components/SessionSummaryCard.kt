package com.anis.child.ui.screen.ai.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anis.child.data.local.SessionEntity
import com.anis.child.R
import androidx.compose.ui.res.stringResource
import com.anis.child.ui.theme.LocalAppColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SessionSummaryCard(session: SessionEntity) {
    val appColors = LocalAppColors.current
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = appColors.primary01.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.session_number, session.id),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = appColors.textPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.started),
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.textSecondary
                    )
                    Text(
                        text = dateFormat.format(Date(session.startTime)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = appColors.textPrimary
                    )
                }

                if (session.endTime != null) {
                    Column {
                        Text(
                            text = stringResource(R.string.ended),
                            style = MaterialTheme.typography.bodySmall,
                            color = appColors.textSecondary
                        )
                        Text(
                            text = dateFormat.format(Date(session.endTime)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = appColors.textPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SessionStatItem(label = stringResource(R.string.interval_label), value = "${session.intervalMs}ms")
                SessionStatItem(label = stringResource(R.string.total), value = "${session.totalCaptures}")
                SessionStatItem(label = stringResource(R.string.blocked), value = "${session.blockedCount}")
                SessionStatItem(label = stringResource(R.string.safe), value = "${session.safeCount}")
            }

            if (session.endTime != null && session.batteryStart > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.device_usage),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = appColors.textSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val batteryStr = "${session.batteryStart}% \u2192 ${session.batteryEnd}%"
                    val batteryDelta = session.batteryStart - session.batteryEnd
                    val batteryLabel = if (session.batteryCharging) stringResource(R.string.battery_charging) else stringResource(R.string.battery)
                    SessionStatItem(
                        label = batteryLabel,
                        value = if (batteryDelta > 0) "$batteryStr (-$batteryDelta%)" else batteryStr
                    )
                    SessionStatItem(
                        label = stringResource(R.string.cpu),
                        value = "${"%.1f".format(session.cpuUsagePercent)}%"
                    )
                    SessionStatItem(
                        label = stringResource(R.string.ram),
                        value = "${"%.0f".format(session.ramPssMb)} MB"
                    )
                }
            }
        }
    }
}
