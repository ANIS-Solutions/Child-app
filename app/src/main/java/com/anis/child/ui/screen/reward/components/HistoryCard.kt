package com.anis.child.ui.screen.reward.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anis.child.data.local.RewardEntity
import com.anis.child.ui.theme.LocalAppColors

@Composable
fun HistoryCard(reward: RewardEntity) {
    val appColors = LocalAppColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = appColors.darkSurface.copy(alpha = 0.03f))
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
                tint = if (reward.state == "consumed") appColors.success500 else appColors.textDisabled,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(reward.title, style = MaterialTheme.typography.bodyMedium, color = appColors.textPrimary)
                Text(reward.state, style = MaterialTheme.typography.bodySmall, color = appColors.textSecondary)
            }
            Text("${reward.pointCost} pts", style = MaterialTheme.typography.bodySmall, color = appColors.textSecondary)
        }
    }
}
