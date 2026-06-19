package com.anis.child.ui.screen.ai.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anis.child.data.local.SessionEntity
import com.anis.child.R
import androidx.compose.ui.res.stringResource
import com.anis.child.ui.theme.LocalAppColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SessionHistoryItem(
    session: SessionEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val appColors = LocalAppColors.current
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = appColors.darkSurface.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormat.format(Date(session.startTime)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = appColors.textPrimary
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = appColors.error500
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.status),
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.textSecondary
                    )
                    Text(
                        text = session.status,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (session.status == "ACTIVE") {
                            appColors.success500
                        } else {
                            appColors.textPrimary
                        }
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.interval),
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.textSecondary
                    )
                    Text(
                        text = "${session.intervalMs}ms",
                        style = MaterialTheme.typography.bodyMedium,
                        color = appColors.textPrimary
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.captures),
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.textSecondary
                    )
                    Text(
                        text = "${session.totalCaptures}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = appColors.textPrimary
                    )
                }
            }

            if (session.status == "COMPLETED") {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AssistChip(
                        onClick = {},
                        label = { Text("${stringResource(R.string.blocked)}: ${session.blockedCount}") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = appColors.error500.copy(alpha = 0.2f)
                        )
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text("${stringResource(R.string.safe)}: ${session.safeCount}") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = appColors.success500.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    }
}
