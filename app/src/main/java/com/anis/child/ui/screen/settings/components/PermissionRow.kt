package com.anis.child.ui.screen.settings.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
fun PermissionRow(name: String, isGranted: Boolean) {
    val appColors = LocalAppColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = appColors.textSecondary,
            modifier = Modifier.weight(1f)
        )
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isGranted) appColors.success500.copy(alpha = 0.15f)
            else appColors.error500.copy(alpha = 0.15f)
        ) {
            Text(
                text = if (isGranted) stringResource(R.string.granted) else stringResource(R.string.denied),
                style = MaterialTheme.typography.labelSmall,
                color = if (isGranted) appColors.success500 else appColors.error500,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}
