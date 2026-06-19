package com.anis.child.ui.screen.contentprotection.components

import com.anis.child.ui.screen.contentprotection.InstalledAppInfo
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anis.child.R
import androidx.compose.ui.res.stringResource
import com.anis.child.ui.theme.LocalAppColors

@Composable
fun AppCard(
    app: InstalledAppInfo,
    onToggleBlock: () -> Unit,
    onSetLimit: (Int) -> Unit
) {
    val appColors = LocalAppColors.current
    var showLimitDialog by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (app.isBlocked || app.dailyTimeLimitMinutes > 0)
                appColors.error500.copy(alpha = 0.06f)
            else appColors.darkSurface.copy(alpha = 0.03f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(app.icon, modifier = Modifier.size(40.dp))

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = appColors.textPrimary
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = appColors.textSecondary,
                    maxLines = 1
                )
                if (app.dailyTimeLimitMinutes > 0) {
                    Text(
                        text = "${app.dailyTimeLimitMinutes} min/day limit",
                        style = MaterialTheme.typography.labelSmall,
                        color = appColors.warning500
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            OutlinedButton(
                onClick = { showLimitDialog = true },
                modifier = Modifier.height(32.dp)
            ) {
                Text(stringResource(R.string.limit_btn), fontSize = 12.sp)
            }

            Spacer(Modifier.width(4.dp))

            Switch(
                checked = app.isBlocked,
                onCheckedChange = { onToggleBlock() }
            )
        }
    }

    if (showLimitDialog) {
        LimitDialog(
            currentMinutes = app.dailyTimeLimitMinutes,
            onConfirm = { minutes ->
                onSetLimit(minutes)
                showLimitDialog = false
            },
            onDismiss = { showLimitDialog = false }
        )
    }
}
