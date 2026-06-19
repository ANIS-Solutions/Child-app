package com.anis.child.ui.screen.settings

import com.anis.child.ui.screen.settings.components.*
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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
        DisableAiDialog(
            onDismiss = { showDisableAiDialog = false },
            onConfirm = {
                viewModel.confirmDisableAiFiltering()
                showDisableAiDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(appColors.surface50)
    ) {
        SettingsTopBar(onBack = onBack)

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
