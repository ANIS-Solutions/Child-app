package com.anis.child.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
fun LogSection(
    logManager: LogManager,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var logs by remember { mutableStateOf(emptyList<LogEntry>()) }

    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            logs = logManager.getLogs()
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.darkSurface)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Logs",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.textPrimary,
                fontWeight = FontWeight.Medium
            )

            val logCount = logManager.getLogsJson().length()
            if (logCount > 0) {
                Text(
                    text = "$logCount entries",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.textSecondary
                )
            }

            IconButton(onClick = { isExpanded = !isExpanded }) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = AppColors.textSecondary
                )
            }
        }

        AnimatedVisibility(visible = isExpanded) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.darkBackground)
            ) {
                val listState = rememberLazyListState()

                LaunchedEffect(logs.size) {
                    if (logs.isNotEmpty()) {
                        listState.animateScrollToItem(0)
                    }
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
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        reverseLayout = true
                    ) {
                        items(logs) { entry ->
                            LogItem(
                                entry = entry,
                                formatTime = { logManager.formatTimestamp(it) }
                            )
                        }
                    }
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
    }

    val textColor = when (entry.type) {
        LogType.INFO -> AppColors.textPrimary
        LogType.SUCCESS -> AppColors.success500
        LogType.ERROR -> AppColors.error500
        LogType.LOCATION -> AppColors.primary01
        LogType.HTTP -> AppColors.warning500
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 2.dp, horizontal = 4.dp),
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