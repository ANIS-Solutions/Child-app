package com.anis.child.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anis.child.data.AppUsageInfo
import com.anis.child.ui.theme.AppColors

@Composable
fun HomeScreen(
    childName: String,
    homeViewModel: HomeViewModel,
    onSettingsClick: () -> Unit,
    onTaskClick: () -> Unit = {},
    onRewardClick: () -> Unit = {}
) {
    val homeData by homeViewModel.homeData.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.surface50)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.primary01)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = childName.firstOrNull()?.uppercase() ?: "C",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Hi, $childName!",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Let's see what's happening",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TodayScreenTimeCard(
                todayMinutes = homeData.todayMinutes,
                dailyLimit = homeData.dailyLimit
            )

            WeeklyAverageCard(weeklyAverage = homeData.weeklyAverage)

            TopAppsCard(apps = homeData.topApps)

            Spacer(Modifier.height(8.dp))

            ActionCard(
                icon = Icons.Default.TaskAlt,
                title = "See what's your assigned tasks",
                color = AppColors.entertainment500,
                onClick = onTaskClick
            )

            ActionCard(
                icon = Icons.Default.CardGiftcard,
                title = "It's time for a new reward",
                color = Color(0xFFFF9800),
                onClick = onRewardClick
            )
        }
    }
}

@Composable
private fun TodayScreenTimeCard(todayMinutes: Int, dailyLimit: Int) {
    val isOverLimit = dailyLimit > 0 && todayMinutes >= dailyLimit

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverLimit) AppColors.error500.copy(alpha = 0.1f)
            else AppColors.darkSurface.copy(alpha = 0.05f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Timer,
                    null,
                    tint = if (isOverLimit) AppColors.error500 else AppColors.primary01,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Today's Screen Time",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppColors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${todayMinutes}m",
                style = MaterialTheme.typography.headlineMedium,
                color = if (isOverLimit) AppColors.error500 else AppColors.primary01,
                fontWeight = FontWeight.Bold
            )
            if (isOverLimit) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Warning,
                        null,
                        tint = AppColors.error500,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "You've exceeded your daily limit of ${dailyLimit}m",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.error500,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyAverageCard(weeklyAverage: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.darkSurface.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Timer,
                null,
                tint = AppColors.textSecondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = "Weekly Average",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.textSecondary
                )
                Text(
                    text = "${weeklyAverage}m / day",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppColors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TopAppsCard(apps: List<AppUsageInfo>) {
    if (apps.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.darkSurface.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Top Apps Today",
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.textPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            apps.forEach { app ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = app.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.textPrimary
                    )
                    Text(
                        text = formatDuration(app.totalTimeInForegroundMs),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.primary01,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionCard(
    icon: ImageVector,
    title: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = title,
                color = AppColors.textPrimary,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "→",
                color = AppColors.textSecondary,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

private fun formatDuration(ms: Long): String {
    val minutes = (ms / 60000).toInt()
    val hours = minutes / 60
    val mins = minutes % 60
    return if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
}
