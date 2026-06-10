package com.anis.child.ui.screen.home

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AppColors.primary01.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = childName.firstOrNull()?.uppercase() ?: "C",
                    color = AppColors.primary01,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "Hi, $childName!",
                    color = AppColors.textPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Let's see what's happening",
                    color = AppColors.textSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = AppColors.textSecondary
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TodayScreenTimeCard(
                todayMinutes = homeData.todayMinutes,
                dailyLimit = homeData.dailyLimit
            )

            WeeklyCard(
                weeklyTotal = homeData.weeklyTotal,
                weeklyAverage = homeData.weeklyAverage
            )

            TopAppsCard(
                title = "Top Apps Today",
                apps = homeData.topApps
            )

            TopAppsCard(
                title = "Top Apps This Week",
                apps = homeData.weeklyTopApps
            )

            Spacer(Modifier.height(8.dp))

            ActionCard(
                icon = Icons.Default.TaskAlt,
                title = "See what's your assigned tasks",
                subtitle = "Complete tasks and earn points",
                color = AppColors.entertainment500,
                onClick = onTaskClick
            )

            ActionCard(
                icon = Icons.Default.CardGiftcard,
                title = "It's time for a new reward",
                subtitle = "Spend your points on rewards",
                color = Color(0xFFFF9800),
                onClick = onRewardClick
            )

            Spacer(Modifier.height(16.dp))
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
            else AppColors.primary01.copy(alpha = 0.08f)
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
private fun WeeklyCard(weeklyTotal: Int, weeklyAverage: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.primary01.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Timer,
                    null,
                    tint = AppColors.primary01,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "This Week",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.textSecondary
                )
                Text(
                    text = "${weeklyTotal}m",
                    style = MaterialTheme.typography.headlineSmall,
                    color = AppColors.primary01,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.TrendingUp,
                    null,
                    tint = AppColors.primary01,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Daily Avg",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.textSecondary
                )
                Text(
                    text = "${weeklyAverage}m",
                    style = MaterialTheme.typography.headlineSmall,
                    color = AppColors.primary01,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TopAppsCard(title: String, apps: List<AppUsageInfo>) {
    if (apps.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.darkSurface.copy(alpha = 0.05f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.textPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            apps.forEachIndexed { index, app ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${index + 1}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.textSecondary,
                        modifier = Modifier.width(24.dp)
                    )
                    Text(
                        text = app.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.textPrimary,
                        modifier = Modifier.weight(1f)
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
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = AppColors.textPrimary,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = AppColors.textSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = "→",
                color = color,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
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
