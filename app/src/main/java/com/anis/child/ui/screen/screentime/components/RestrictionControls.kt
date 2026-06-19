package com.anis.child.ui.screen.screentime.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
fun RestrictionControls(
    onStartService: () -> Unit,
    onStopService: () -> Unit
) {
    val appColors = LocalAppColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = appColors.darkSurface.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.app_restriction_service),
                        style = MaterialTheme.typography.bodyLarge,
                        color = appColors.textPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = stringResource(R.string.restriction_service_not_running),
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.textSecondary
                    )
                }
                Button(
                    onClick = onStartService,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = appColors.primary01
                    )
                ) {
                    Text(
                        text = stringResource(R.string.start),
                        color = appColors.darkTextPrimary
                    )
                }
            }
        }
    }
}
