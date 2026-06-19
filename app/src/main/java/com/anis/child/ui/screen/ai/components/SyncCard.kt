package com.anis.child.ui.screen.ai.components

import com.anis.child.ui.screen.ai.VoteAllState
import com.anis.child.ui.screen.ai.VoteStatus
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anis.child.R
import androidx.compose.ui.res.stringResource
import com.anis.child.ui.theme.LocalAppColors

@Composable
fun SyncCard(
    voteState: VoteAllState,
    unsyncedCount: Int,
    onVote: () -> Unit,
    onSend: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = appColors.primary01.copy(alpha = 0.1f)
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
                        text = stringResource(R.string.unsynced_count, unsyncedCount),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = appColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.unsynced_sessions_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    voteState.error?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = appColors.error500
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Button(
                        onClick = onVote,
                        enabled = voteState.status != VoteStatus.Voting,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = appColors.primary01
                        )
                    ) {
                        if (voteState.status == VoteStatus.Voting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = appColors.darkTextPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (voteState.status == VoteStatus.Voting) stringResource(R.string.voting) else stringResource(R.string.vote_all_sessions),
                            color = appColors.darkTextPrimary
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
                        color = appColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(results, key = { it.id }) { result ->
                            SessionHistoryKeyframeThumbnail(result = result)
                        }
                    }

                    voteState.error?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = appColors.error500
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onSend,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = appColors.primary01
                        )
                    ) {
                        Text(stringResource(R.string.send_to_server), color = appColors.darkTextPrimary)
                    }
                }

                VoteStatus.Sending -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = appColors.primary01,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.sending_to_server),
                            style = MaterialTheme.typography.bodyMedium,
                            color = appColors.textPrimary
                        )
                    }
                }

                VoteStatus.Sent -> { }
            }
        }
    }
}
