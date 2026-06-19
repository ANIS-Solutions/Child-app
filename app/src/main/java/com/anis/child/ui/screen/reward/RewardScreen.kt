package com.anis.child.ui.screen.reward

import com.anis.child.ui.screen.reward.components.*
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.res.stringResource
import com.anis.child.ui.components.EmptyStateView
import com.anis.child.ui.theme.LocalAppColors
import com.anis.child.R

@Composable
fun RewardScreen(
    viewModel: RewardViewModel,
    onBack: () -> Unit
) {
    val appColors = LocalAppColors.current
    val uiState by viewModel.uiState.collectAsState()

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
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, stringResource(R.string.back), tint = appColors.textPrimary)
            }
            Text(
                text = stringResource(R.string.rewards_title_screen),
                color = appColors.textPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = appColors.primary01)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    BalanceCard(balance = uiState.balance)
                }

                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.available_rewards),
                        style = MaterialTheme.typography.titleMedium,
                        color = appColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                val available = uiState.rewards.filter { it.state == "earned" }
                if (available.isEmpty()) {
                    item {
                        EmptyStateView(Icons.Default.CardGiftcard, stringResource(R.string.no_rewards), Modifier.fillMaxWidth().padding(32.dp))
                    }
                } else {
                    items(available) { reward ->
                        RewardCard(
                            reward = reward,
                            canAfford = reward.pointCost <= uiState.balance,
                            onClaim = { viewModel.claimReward(reward.id) }
                        )
                    }
                }

                val history = uiState.rewards.filter { it.state != "earned" }
                if (history.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.rewards_history),
                            style = MaterialTheme.typography.titleMedium,
                            color = appColors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(history) { reward ->
                        HistoryCard(reward = reward)
                    }
                }
            }
        }
    }
}
