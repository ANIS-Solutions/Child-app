package com.anis.child.ui.screen.reward.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
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
import com.anis.child.R
import androidx.compose.ui.res.stringResource
import com.anis.child.ui.theme.LocalAppColors

@Composable
fun BalanceCard(balance: Int) {
    val appColors = LocalAppColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = appColors.primary01)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Star, null, tint = appColors.warning500, modifier = Modifier.size(36.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "$balance",
                    style = MaterialTheme.typography.displayMedium,
                    color = appColors.darkTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.reward_points),
                    style = MaterialTheme.typography.bodyMedium,
                    color = appColors.darkTextPrimary.copy(alpha = 0.8f)
                )
            }
        }
    }
}
