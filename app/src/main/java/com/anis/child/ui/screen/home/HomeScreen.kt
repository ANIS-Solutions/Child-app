package com.anis.child.ui.screen.home

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anis.child.data.AppUsageInfo
import com.anis.child.ui.theme.LocalAppColors
import com.anis.child.ui.theme.ThemeManager
import com.anis.child.R
import androidx.compose.ui.res.stringResource

@Composable
fun HomeScreen(
    childName: String,
    homeViewModel: HomeViewModel,
    currentLanguage: String = "en",
    onSettingsClick: () -> Unit,
    onTaskClick: () -> Unit = {},
    onRewardClick: () -> Unit = {},
    onLanguageChange: (String) -> Unit = {}
) {
    val appColors = LocalAppColors.current
    val homeData by homeViewModel.homeData.collectAsState()
    var showLanguageDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(appColors.surface50)
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
                    .background(appColors.primary01.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = childName.firstOrNull()?.uppercase() ?: "C",
                    color = appColors.primary01,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.home_greeting, childName),
                    color = appColors.textPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.home_subtitle),
                    color = appColors.textSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(Modifier.weight(1f))

            IconButton(onClick = { showLanguageDialog = true }) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(appColors.primary01.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (currentLanguage == "ar") "ع" else "EN",
                        color = appColors.primary01,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            IconButton(onClick = { ThemeManager.toggle() }) {
                Icon(
                    imageVector = if (ThemeManager.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = if (ThemeManager.isDarkMode) stringResource(R.string.switch_to_light_mode) else stringResource(R.string.switch_to_dark_mode),
                    tint = appColors.textSecondary
                )
            }
        }

        if (showLanguageDialog) {
            AlertDialog(
                onDismissRequest = { showLanguageDialog = false },
                title = { Text(stringResource(R.string.language)) },
                text = {
                    Column {
                        TextButton(
                            onClick = {
                                onLanguageChange("en")
                                showLanguageDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.language_en),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        TextButton(
                            onClick = {
                                onLanguageChange("ar")
                                showLanguageDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.language_ar),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                },
                confirmButton = {}
            )
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
                title = stringResource(R.string.top_apps_today),
                apps = homeData.topApps
            )

            Spacer(Modifier.height(8.dp))

            ActionCard(
                icon = Icons.Default.TaskAlt,
                title = stringResource(R.string.quests_title),
                subtitle = stringResource(R.string.quests_subtitle),
                color = appColors.entertainment500,
                onClick = onTaskClick
            )

            ActionCard(
                icon = Icons.Default.CardGiftcard,
                title = stringResource(R.string.rewards_title),
                subtitle = stringResource(R.string.rewards_subtitle),
                color = Color(0xFFFF9800),
                onClick = onRewardClick
            )

            ActionCard(
                icon = Icons.Default.Settings,
                title = stringResource(R.string.parent_pypass),
                subtitle = stringResource(R.string.parent_pypass_subtitle),
                color = Color(0xFFD3D3D3),
                onClick = onSettingsClick
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TodayScreenTimeCard(todayMinutes: Int, dailyLimit: Int) {
    val appColors = LocalAppColors.current
    val isOverLimit = dailyLimit > 0 && todayMinutes >= dailyLimit

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverLimit) appColors.error500.copy(alpha = 0.1f)
            else appColors.primary01.copy(alpha = 0.08f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Timer,
                    null,
                    tint = if (isOverLimit) appColors.error500 else appColors.primary01,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.today_screen_time),
                    style = MaterialTheme.typography.bodyLarge,
                    color = appColors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${todayMinutes}m",
                style = MaterialTheme.typography.headlineMedium,
                color = if (isOverLimit) appColors.error500 else appColors.primary01,
                fontWeight = FontWeight.Bold
            )
            if (isOverLimit) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Warning,
                        null,
                        tint = appColors.error500,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.daily_limit_exceeded, "${dailyLimit}m"),
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.error500,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyCard(weeklyTotal: Int, weeklyAverage: Int) {
    val appColors = LocalAppColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = appColors.primary01.copy(alpha = 0.08f)
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
                    tint = appColors.primary01,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.this_week),
                    style = MaterialTheme.typography.bodySmall,
                    color = appColors.textSecondary
                )
                Text(
                    text = "${weeklyTotal}m",
                    style = MaterialTheme.typography.headlineSmall,
                    color = appColors.primary01,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.TrendingUp,
                    null,
                    tint = appColors.primary01,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.daily_avg),
                    style = MaterialTheme.typography.bodySmall,
                    color = appColors.textSecondary
                )
                Text(
                    text = "${weeklyAverage}m",
                    style = MaterialTheme.typography.headlineSmall,
                    color = appColors.primary01,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TopAppsCard(title: String, apps: List<AppUsageInfo>) {
    val appColors = LocalAppColors.current
    if (apps.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = appColors.darkSurface.copy(alpha = 0.05f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = appColors.textPrimary,
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
                        color = appColors.textSecondary,
                        modifier = Modifier.width(24.dp)
                    )
                    Text(
                        text = app.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = appColors.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = formatDuration(app.totalTimeInForegroundMs),
                        style = MaterialTheme.typography.bodyMedium,
                        color = appColors.primary01,
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
    val appColors = LocalAppColors.current
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
                    color = appColors.textPrimary,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = appColors.textSecondary,
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
