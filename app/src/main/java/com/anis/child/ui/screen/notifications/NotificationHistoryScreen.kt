package com.anis.child.ui.screen.notifications

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
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anis.child.data.local.NotificationInterceptEntity
import com.anis.child.ui.theme.AppColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationHistoryScreen(
    viewModel: NotificationHistoryViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

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
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = AppColors.darkTextPrimary)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Notifications",
                        color = AppColors.darkTextPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (uiState.unreadCount > 0) {
                        Spacer(Modifier.width(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = AppColors.error500),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                "${uiState.unreadCount}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = AppColors.darkTextPrimary
                            )
                        }
                    }
                }
                if (uiState.unreadCount > 0) {
                    IconButton(onClick = { viewModel.markAllAsRead() }) {
                        Icon(Icons.Default.DoneAll, "Mark all read", tint = AppColors.darkTextPrimary)
                    }
                }
                if (uiState.notifications.isNotEmpty()) {
                    IconButton(onClick = { viewModel.clearAll() }) {
                        Icon(Icons.Default.ClearAll, "Clear all", tint = AppColors.darkTextPrimary)
                    }
                }
            }
        }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.primary01)
            }
        } else if (uiState.notifications.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.NotificationsOff, null, tint = AppColors.textDisabled, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("No notifications", color = AppColors.textSecondary)
                    Text("Notifications from other apps will appear here", style = MaterialTheme.typography.bodySmall, color = AppColors.textDisabled)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.notifications) { notification ->
                    NotificationCard(
                        notification = notification,
                        onDelete = { viewModel.deleteNotification(notification.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: NotificationInterceptEntity,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead)
                AppColors.darkSurface.copy(alpha = 0.03f)
            else AppColors.darkSurface.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (notification.isRead) Icons.Default.Notifications else Icons.Default.Notifications,
                null,
                tint = if (notification.isRead) AppColors.textDisabled else AppColors.primary01,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = notification.appLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.textSecondary
                    )
                    if (!notification.isRead) {
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(AppColors.primary01, RoundedCornerShape(3.dp))
                        )
                    }
                }
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.textPrimary,
                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (notification.text.isNotEmpty()) {
                    Text(
                        text = notification.text,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = dateFormat.format(Date(notification.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.textDisabled
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete", tint = AppColors.error500, modifier = Modifier.size(20.dp))
            }
        }
    }
}
