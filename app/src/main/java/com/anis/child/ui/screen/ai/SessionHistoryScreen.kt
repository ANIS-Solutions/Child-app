package com.anis.child.ui.screen.ai

import com.anis.child.ui.screen.ai.components.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.anis.child.ui.theme.LocalAppColors
import com.anis.child.R
import androidx.compose.ui.res.stringResource

@Composable
fun SessionHistoryScreen(
    onNavigateBack: () -> Unit,
    onSessionClick: (Long) -> Unit,
    viewModel: SessionHistoryViewModel = hiltViewModel()
) {
    val appColors = LocalAppColors.current
    val sessions by viewModel.sessions.collectAsState()
    val voteState by viewModel.voteState.collectAsState()
    val unsyncedCount by viewModel.unsyncedCount.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(appColors.surface50)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), tint = appColors.textPrimary)
            }
            Text(
                text = stringResource(R.string.session_history_title),
                color = appColors.textPrimary,
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
                        text = stringResource(R.string.no_sessions),
                        style = MaterialTheme.typography.titleMedium,
                        color = appColors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.no_sessions_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = appColors.textSecondary
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
