package com.anis.child.ui.screen.screentime.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anis.child.data.ScreenTimeSummary
import com.anis.child.R
import androidx.compose.ui.res.stringResource
import com.anis.child.ui.theme.LocalAppColors

@Composable
fun SummaryCard(summary: ScreenTimeSummary) {
    val appColors = LocalAppColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = appColors.darkSurface.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.todays_usage),
                style = MaterialTheme.typography.bodyLarge,
                color = appColors.textPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(stringResource(R.string.used), "${summary.todayTotalMinutes}m", appColors.primary01)
                if (summary.dailyLimitMinutes > 0) {
                    StatItem(stringResource(R.string.limit), "${summary.dailyLimitMinutes}m", appColors.warning500)
                    StatItem(stringResource(R.string.remaining), "${summary.remainingMinutes}m",
                        if (summary.isLimitReached) appColors.error500 else appColors.success500)
                }
            }

            if (summary.dailyLimitMinutes > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                val progress = (summary.todayTotalMinutes.toFloat() / summary.dailyLimitMinutes).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = if (summary.isLimitReached) appColors.error500 else appColors.primary01,
                    trackColor = appColors.textDisabled.copy(alpha = 0.3f)
                )
            }
        }
    }
}
