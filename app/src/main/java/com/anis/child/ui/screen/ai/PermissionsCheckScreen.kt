package com.anis.child.ui.screen.ai

import com.anis.child.ui.screen.ai.components.*
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anis.child.ai.util.PermissionManager
import com.anis.child.ui.components.PermissionItem
import com.anis.child.ui.theme.LocalAppColors

@Composable
fun PermissionsCheckScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onHistoryClick: () -> Unit = {}
) {
    val appColors = LocalAppColors.current
    val context = LocalContext.current
    val activity = context as? Activity

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    val overlaySettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ -> }

    var refreshKey by remember { mutableStateOf(0) }

    val hasNotification = remember { mutableStateOf(PermissionManager.hasNotificationPermission(context)) }
    val hasOverlay = remember { mutableStateOf(PermissionManager.hasOverlayPermission(context)) }
    val allGranted = hasNotification.value && hasOverlay.value

    fun refreshPermissions() {
        refreshKey++
        hasNotification.value = PermissionManager.hasNotificationPermission(context)
        hasOverlay.value = PermissionManager.hasOverlayPermission(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(appColors.surface50)
    ) {
        PermissionsCheckTopBar(onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(24.dp)
                .verticalScroll(rememberScrollState(refreshKey)),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Required Permissions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = appColors.textPrimary
            )

            Text(
                text = "Grant the following permissions to enable AI content monitoring.",
                style = MaterialTheme.typography.bodyMedium,
                color = appColors.textSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            PermissionItem(
                title = "Notifications",
                description = if (hasNotification.value) {
                    "Permission granted - alerts for blocked content will work"
                } else {
                    "Required to show session status and blocked content alerts"
                },
                isGranted = hasNotification.value,
                onClick = {
                    if (activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            )

            PermissionItem(
                title = "Overlay",
                description = if (hasOverlay.value) {
                    "Permission granted - blocked content overlay enabled"
                } else {
                    "Required to show a blocking overlay when inappropriate content is detected"
                },
                isGranted = hasOverlay.value,
                onClick = {
                    if (activity != null) {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${activity.packageName}")
                        )
                        overlaySettingsLauncher.launch(intent)
                    }
                }
            )

            PermissionItem(
                title = "Screen Recording",
                description = "Required to capture screen content for AI analysis. Will be requested when starting a session.",
                isGranted = true,
                onClick = {}
            )

            Spacer(modifier = Modifier.height(16.dp))

            PermissionCheckStatusCard(allGranted = allGranted)

            Spacer(modifier = Modifier.height(8.dp))

            ContinueButton(
                allGranted = allGranted,
                onClick = {
                    refreshPermissions()
                    if (allGranted) {
                        onContinue()
                    }
                }
            )

            ViewHistoryButton(onClick = onHistoryClick)
        }
    }
}
