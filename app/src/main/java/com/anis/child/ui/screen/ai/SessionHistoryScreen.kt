package com.anis.child.ui.screen.ai

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.anis.child.data.local.AnalysisResultEntity
import com.anis.child.data.local.SessionEntity
import com.anis.child.ui.theme.AppColors
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SessionHistoryScreen(
    onNavigateBack: () -> Unit,
    onSessionClick: (Long) -> Unit,
    viewModel: SessionHistoryViewModel = hiltViewModel()
) {
    val sessions by viewModel.sessions.collectAsState()
    val voteState by viewModel.voteState.collectAsState()
    val unsyncedCount by viewModel.unsyncedCount.collectAsState()

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
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = AppColors.textPrimary)
            }
            Text(
                text = "Session History",
                color = AppColors.textPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (voteState.status == VoteStatus.Sent) {
            SyncSentCard(
                onDismiss = { viewModel.resetVoteState() },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        } else if (voteState.status != VoteStatus.Idle) {
            SyncCard(
                voteState = voteState,
                unsyncedCount = unsyncedCount,
                onVote = { viewModel.voteAllSessions() },
                onSend = { viewModel.sendVotedData() },
                onDismiss = { viewModel.resetVoteState() },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        } else if (unsyncedCount > 0) {
            SyncCard(
                voteState = voteState,
                unsyncedCount = unsyncedCount,
                onVote = { viewModel.voteAllSessions() },
                onSend = { viewModel.sendVotedData() },
                onDismiss = { viewModel.resetVoteState() },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No sessions yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = AppColors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Start a monitoring session to see history here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.textSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sessions, key = { it.id }) { session ->
                    SessionHistoryItem(
                        session = session,
                        onClick = { onSessionClick(session.id) },
                        onDelete = { viewModel.deleteSession(session.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SyncCard(
    voteState: VoteAllState,
    unsyncedCount: Int,
    onVote: () -> Unit,
    onSend: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.primary01.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            when (voteState.status) {
                VoteStatus.Idle, VoteStatus.Voting -> {
                    Text(
                        text = "$unsyncedCount unsynced sessions",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Run CondensationEngine across all sessions to select best keyframes",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    voteState.error?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.error500
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Button(
                        onClick = onVote,
                        enabled = voteState.status != VoteStatus.Voting,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.primary01
                        )
                    ) {
                        if (voteState.status == VoteStatus.Voting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = AppColors.darkTextPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (voteState.status == VoteStatus.Voting) "Voting..." else "Vote All Sessions",
                            color = AppColors.darkTextPrimary
                        )
                    }
                }

                VoteStatus.Previewing -> {
                    val results = voteState.selectedResults
                    val embCount = voteState.allEmbeddings.size
                    Text(
                        text = "Selected ${results.size} keyframes, $embCount embeddings from $unsyncedCount sessions",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(results, key = { it.id }) { result ->
                            KeyframeThumbnail(result = result)
                        }
                    }

                    voteState.error?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.error500
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onSend,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.primary01
                        )
                    ) {
                        Text("Send to Server", color = AppColors.darkTextPrimary)
                    }
                }

                VoteStatus.Sending -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = AppColors.primary01,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sending to server...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.textPrimary
                        )
                    }
                }

                VoteStatus.Sent -> { /* handled by SyncSentCard */ }
            }
        }
    }
}

@Composable
private fun SyncSentCard(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.success500.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Data sent successfully",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.success500
                )
            ) {
                Text("Done", color = AppColors.darkTextPrimary)
            }
        }
    }
}

@Composable
private fun KeyframeThumbnail(result: AnalysisResultEntity) {
    var bitmap by remember(result.id) { mutableStateOf<android.graphics.Bitmap?>(null) }

    androidx.compose.runtime.LaunchedEffect(result.imagePath) {
        val path = result.imagePath
        if (path != null) {
            bitmap = BitmapFactory.decodeFile(path)
        }
    }

    Card(
        modifier = Modifier.size(100.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        bitmap?.let { bmp ->
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "Keyframe ${result.id}",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        } ?: Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.darkSurface.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No image",
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.textSecondary
            )
        }
    }
}

@Composable
private fun SessionHistoryItem(
    session: SessionEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = AppColors.darkSurface.copy(alpha = 0.1f))
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
                    color = AppColors.textPrimary
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = AppColors.error500
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
                        text = "Status",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.textSecondary
                    )
                    Text(
                        text = session.status,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (session.status == "ACTIVE") {
                            AppColors.success500
                        } else {
                            AppColors.textPrimary
                        }
                    )
                }
                Column {
                    Text(
                        text = "Interval",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.textSecondary
                    )
                    Text(
                        text = "${session.intervalMs}ms",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.textPrimary
                    )
                }
                Column {
                    Text(
                        text = "Captures",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.textSecondary
                    )
                    Text(
                        text = "${session.totalCaptures}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.textPrimary
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
                        label = { Text("Blocked: ${session.blockedCount}") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = AppColors.error500.copy(alpha = 0.2f)
                        )
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text("Safe: ${session.safeCount}") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = AppColors.success500.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    }
}
