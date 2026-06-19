package com.anis.child.ui.screen.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anis.child.data.AppUsageInfo
import com.anis.child.util.formatDuration
import com.anis.child.ui.theme.LocalAppColors

@Composable
fun TopAppsCard(title: String, apps: List<AppUsageInfo>) {
    val appColors = LocalAppColors.current
    if (apps.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = appColors.darkSurface.copy(alpha = 0.05f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = appColors.textPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            apps.forEachIndexed { index, app ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${index + 1}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = appColors.textSecondary,
                        modifier = Modifier.width(24.dp)
                    )
                    Text(
                        text = app.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = appColors.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = formatDuration(app.totalTimeInForegroundMs),
                        style = MaterialTheme.typography.bodyMedium,
                        color = appColors.primary01,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
