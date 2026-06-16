package com.anis.child.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anis.child.data.LogEntry
import com.anis.child.data.LogManager
import com.anis.child.data.LogType
import com.anis.child.ui.theme.AppColors

@Composable
fun LogsScreen(
    logManager: LogManager,
    onBack: () -> Unit
) {
    var logs by remember { mutableStateOf(emptyList<LogEntry>()) }

    LaunchedEffect(Unit) {
        logs = logManager.getLogs().filter { it.type != LogType.NOTIFICATION }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.surface50)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = AppColors.textPrimary)
            }
            Text(
                text = "Logs",
                color = AppColors.textPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (logs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No logs yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.textDisabled
                )
            }
        } else {
            val listState = rememberLazyListState()

            LaunchedEffect(logs.size) {
                if (logs.isNotEmpty()) {
                    listState.animateScrollToItem(0)
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                reverseLayout = true,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(logs) { entry ->
                    LogItem(entry = entry, formatTime = { logManager.formatTimestamp(it) })
                }
            }
        }
    }
}

@Composable
private fun LogItem(
    entry: LogEntry,
    formatTime: (Long) -> String
) {
    val backgroundColor = when (entry.type) {
        LogType.INFO -> Color.Transparent
        LogType.SUCCESS -> AppColors.success500.copy(alpha = 0.1f)
        LogType.ERROR -> AppColors.error500.copy(alpha = 0.1f)
        LogType.LOCATION -> AppColors.primary01.copy(alpha = 0.1f)
        LogType.HTTP -> AppColors.warning500.copy(alpha = 0.1f)
        LogType.NOTIFICATION -> Color.Transparent
    }

    val textColor = when (entry.type) {
        LogType.INFO -> AppColors.textPrimary
        LogType.SUCCESS -> AppColors.success500
        LogType.ERROR -> AppColors.error500
        LogType.LOCATION -> AppColors.primary01
        LogType.HTTP -> AppColors.warning500
        LogType.NOTIFICATION -> AppColors.textPrimary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 4.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = formatTime(entry.timestamp),
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
            ),
            color = AppColors.textDisabled,
            modifier = Modifier.width(50.dp)
        )

        Text(
            text = entry.message,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
            ),
            color = textColor,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
