package com.anis.child.ui.screen.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
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
fun WeeklyCard(weeklyTotal: Int, weeklyAverage: Int) {
    val appColors = LocalAppColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = appColors.primary01.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Timer,
                    null,
                    tint = appColors.primary01,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.this_week),
                    style = MaterialTheme.typography.bodySmall,
                    color = appColors.textSecondary
                )
                Text(
                    text = "${weeklyTotal}m",
                    style = MaterialTheme.typography.headlineSmall,
                    color = appColors.primary01,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.TrendingUp,
                    null,
                    tint = appColors.primary01,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.daily_avg),
                    style = MaterialTheme.typography.bodySmall,
                    color = appColors.textSecondary
                )
                Text(
                    text = "${weeklyAverage}m",
                    style = MaterialTheme.typography.headlineSmall,
                    color = appColors.primary01,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
