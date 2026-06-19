package com.anis.child.ui.screen.home.components

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anis.child.data.LogEntry
import com.anis.child.data.LogManager
import com.anis.child.ui.theme.LocalAppColors

@Composable
fun LogSection(
    logManager: LogManager,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
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
                .background(appColors.darkSurface)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Logs",
                style = MaterialTheme.typography.bodyMedium,
                color = appColors.textPrimary,
                fontWeight = FontWeight.Medium
            )

            val logCount = logManager.getLogsJson().length()
            if (logCount > 0) {
                Text(
                    text = "$logCount entries",
                    style = MaterialTheme.typography.bodySmall,
                    color = appColors.textSecondary
                )
            }

            IconButton(onClick = { isExpanded = !isExpanded }) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = appColors.textSecondary
                )
            }
        }

        AnimatedVisibility(visible = isExpanded) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp),
                colors = CardDefaults.cardColors(containerColor = appColors.darkBackground)
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
                            color = appColors.textDisabled
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
