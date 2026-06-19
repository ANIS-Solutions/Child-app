package com.anis.child.ui.screen.ai.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.anis.child.ui.theme.LocalAppColors

@Composable
fun NotificationPermissionMessage(
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    Text(
        text = "Notification permission is needed.",
        color = appColors.textSecondary,
        style = MaterialTheme.typography.bodyMedium
    )
}
