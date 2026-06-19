package com.anis.child.ui.screen.reward.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anis.child.data.local.RewardEntity
import com.anis.child.R
import androidx.compose.ui.res.stringResource
import com.anis.child.ui.theme.LocalAppColors

@Composable
fun RewardCard(
    reward: RewardEntity,
    canAfford: Boolean,
    onClaim: () -> Unit
) {
    val appColors = LocalAppColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = appColors.darkSurface.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CardGiftcard, null, tint = appColors.primary01, modifier = Modifier.size(36.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reward.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = appColors.textPrimary,
                    fontWeight = FontWeight.Medium
                )
                if (reward.description.isNotEmpty()) {
                    Text(
                        text = reward.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.textSecondary
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${reward.pointCost} pts",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (canAfford) appColors.warning500 else appColors.error500,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = onClaim,
                    enabled = canAfford,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canAfford) appColors.success500 else appColors.textDisabled.copy(alpha = 0.3f)
                    )
                ) {
                    Text(stringResource(R.string.claim), color = appColors.darkTextPrimary)
                }
            }
        }
    }
}
