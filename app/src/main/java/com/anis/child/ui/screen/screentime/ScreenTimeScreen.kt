package com.anis.child.ui.screen.screentime

import com.anis.child.util.formatDuration
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anis.child.data.AppUsageInfo
import com.anis.child.data.ScreenTimeSummary
import com.anis.child.ui.theme.LocalAppColors

@Composable
fun ScreenTimeScreen(
    viewModel: ScreenTimeViewModel,
    onBack: () -> Unit
) {
    val appColors = LocalAppColors.current
    val uiState by viewModel.uiState.collectAsState()

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
                Icon(Icons.Default.ArrowBack, "Back", tint = appColors.textPrimary)
            }
            Text(
                text = "Screen Time",
                color = appColors.textPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = appColors.primary01)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!uiState.hasUsagePermission) {
                    item {
                        PermissionBanner(onOpenSettings = { viewModel.openUsageStatsSettings() })
                    }
                }

                uiState.summary?.let { summary ->
                    item {
                        SummaryCard(summary = summary)
                    }

                    item {
                        TimeStatusRow(summary = summary)
                    }
                }

                item {
                    Text(
                        text = "App Usage Today",
                        style = MaterialTheme.typography.titleMedium,
                        color = appColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (uiState.appUsage.isEmpty()) {
                    item {
                        Text(
                            text = "No usage data available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = appColors.textSecondary,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                } else {
                    items(uiState.appUsage.take(10)) { app ->
                        AppUsageRow(app = app)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Restriction Controls",
                        style = MaterialTheme.typography.titleMedium,
                        color = appColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    RestrictionControls(
                        onStartService = { viewModel.startRestrictionService() },
                        onStopService = { viewModel.stopRestrictionService() }
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionBanner(onOpenSettings: () -> Unit) {
    val appColors = LocalAppColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = appColors.warning500.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, null, tint = appColors.warning500, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Usage Access Required",
                    style = MaterialTheme.typography.bodyLarge,
                    color = appColors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "ANIS needs usage access permission to track screen time and enforce app restrictions.",
                style = MaterialTheme.typography.bodySmall,
                color = appColors.textSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onOpenSettings,
                colors = ButtonDefaults.buttonColors(containerColor = appColors.primary01)
            ) {
                Text("Grant Permission", color = appColors.darkTextPrimary)
            }
        }
    }
}

@Composable
private fun SummaryCard(summary: ScreenTimeSummary) {
    val appColors = LocalAppColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = appColors.darkSurface.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Today's Usage",
                style = MaterialTheme.typography.bodyLarge,
                color = appColors.textPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("Used", "${summary.todayTotalMinutes}m", appColors.primary01)
                if (summary.dailyLimitMinutes > 0) {
                    StatItem("Limit", "${summary.dailyLimitMinutes}m", appColors.warning500)
                    StatItem("Remaining", "${summary.remainingMinutes}m",
                        if (summary.isLimitReached) appColors.error500 else appColors.success500)
                }
            }

            if (summary.dailyLimitMinutes > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                val progress = (summary.todayTotalMinutes.toFloat() / summary.dailyLimitMinutes).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = if (summary.isLimitReached) appColors.error500 else appColors.primary01,
                    trackColor = appColors.textDisabled.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
private fun TimeStatusRow(summary: ScreenTimeSummary) {
    val appColors = LocalAppColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatusChip(
            modifier = Modifier.weight(1f),
            label = if (summary.isLimitReached) "Limit Reached" else "Within Limit",
            icon = if (summary.isLimitReached) Icons.Default.Block else Icons.Default.CheckCircle,
            color = if (summary.isLimitReached) appColors.error500 else appColors.success500
        )
        StatusChip(
            modifier = Modifier.weight(1f),
            label = if (summary.isBedtime) "Bedtime" else "Not Bedtime",
            icon = Icons.Default.Schedule,
            color = if (summary.isBedtime) appColors.warning500 else appColors.success500
        )
        StatusChip(
            modifier = Modifier.weight(1f),
            label = if (summary.isStudyHours) "Study Time" else "Free Time",
            icon = Icons.Default.Timer,
            color = if (summary.isStudyHours) appColors.primary01 else appColors.textSecondary
        )
    }
}

@Composable
private fun StatusChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

@Composable
private fun AppUsageRow(app: AppUsageInfo) {
    val appColors = LocalAppColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = appColors.darkSurface.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = appColors.textPrimary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = appColors.textSecondary
                )
            }
            Text(
                text = formatDuration(app.totalTimeInForegroundMs),
                style = MaterialTheme.typography.bodyMedium,
                color = appColors.primary01,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun RestrictionControls(
    onStartService: () -> Unit,
    onStopService: () -> Unit
) {
    val appColors = LocalAppColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = appColors.darkSurface.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "App Restriction Service",
                        style = MaterialTheme.typography.bodyLarge,
                        color = appColors.textPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Not running — restrictions not enforced",
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.textSecondary
                    )
                }
                Button(
                    onClick = onStartService,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = appColors.primary01
                    )
                ) {
                    Text(
                        text = "Start",
                        color = appColors.darkTextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    val appColors = LocalAppColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.headlineSmall, color = color, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = appColors.textSecondary)
    }
}
