package com.anis.child.ui.screen.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
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
fun TodayScreenTimeCard(todayMinutes: Int, dailyLimit: Int) {
    val appColors = LocalAppColors.current
    val isOverLimit = dailyLimit > 0 && todayMinutes >= dailyLimit

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverLimit) appColors.error500.copy(alpha = 0.1f)
            else appColors.primary01.copy(alpha = 0.08f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Timer,
                    null,
                    tint = if (isOverLimit) appColors.error500 else appColors.primary01,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.today_screen_time),
                    style = MaterialTheme.typography.bodyLarge,
                    color = appColors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${todayMinutes}m",
                style = MaterialTheme.typography.headlineMedium,
                color = if (isOverLimit) appColors.error500 else appColors.primary01,
                fontWeight = FontWeight.Bold
            )
            if (isOverLimit) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Warning,
                        null,
                        tint = appColors.error500,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.daily_limit_exceeded, "${dailyLimit}m"),
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.error500,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
