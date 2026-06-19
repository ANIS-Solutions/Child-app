package com.anis.child.ui.screen.home

import com.anis.child.ui.screen.home.components.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.anis.child.ui.theme.LocalAppColors
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
        HomeScreenHeader(
            childName = childName,
            currentLanguage = currentLanguage,
            onLanguageClick = { showLanguageDialog = true }
        )

        if (showLanguageDialog) {
            LanguageSelectorDialog(
                onDismiss = { showLanguageDialog = false },
                onLanguageChange = onLanguageChange
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
