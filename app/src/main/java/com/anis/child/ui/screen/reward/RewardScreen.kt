package com.anis.child.ui.screen.reward

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
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anis.child.data.local.RewardEntity
import com.anis.child.ui.components.EmptyStateView
import com.anis.child.ui.theme.AppColors

@Composable
fun RewardScreen(
    viewModel: RewardViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

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
                Icon(Icons.Default.ArrowBack, "Back", tint = AppColors.textPrimary)
            }
            Text(
                text = "Rewards",
                color = AppColors.textPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.primary01)
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
                        text = "Available Rewards",
                        style = MaterialTheme.typography.titleMedium,
                        color = AppColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                val available = uiState.rewards.filter { it.state == "earned" }
                if (available.isEmpty()) {
                    item {
                        EmptyStateView(Icons.Default.CardGiftcard, "No rewards available", Modifier.fillMaxWidth().padding(32.dp))
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
                            text = "History",
                            style = MaterialTheme.typography.titleMedium,
                            color = AppColors.textPrimary,
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

@Composable
private fun BalanceCard(balance: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.primary01)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Star, null, tint = AppColors.warning500, modifier = Modifier.size(36.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "$balance",
                    style = MaterialTheme.typography.displayMedium,
                    color = AppColors.darkTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Reward Points",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.darkTextPrimary.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun RewardCard(
    reward: RewardEntity,
    canAfford: Boolean,
    onClaim: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.darkSurface.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CardGiftcard, null, tint = AppColors.primary01, modifier = Modifier.size(36.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reward.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppColors.textPrimary,
                    fontWeight = FontWeight.Medium
                )
                if (reward.description.isNotEmpty()) {
                    Text(
                        text = reward.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.textSecondary
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${reward.pointCost} pts",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (canAfford) AppColors.warning500 else AppColors.error500,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = onClaim,
                    enabled = canAfford,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canAfford) AppColors.success500 else AppColors.textDisabled.copy(alpha = 0.3f)
                    )
                ) {
                    Text("Claim", color = AppColors.darkTextPrimary)
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(reward: RewardEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.darkSurface.copy(alpha = 0.03f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (reward.state == "consumed") Icons.Default.CheckCircle else Icons.Default.Star,
                null,
                tint = if (reward.state == "consumed") AppColors.success500 else AppColors.textDisabled,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(reward.title, style = MaterialTheme.typography.bodyMedium, color = AppColors.textPrimary)
                Text(reward.state, style = MaterialTheme.typography.bodySmall, color = AppColors.textSecondary)
            }
            Text("${reward.pointCost} pts", style = MaterialTheme.typography.bodySmall, color = AppColors.textSecondary)
        }
    }
}
