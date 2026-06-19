package com.anis.child.ui.screen.ai.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import com.anis.child.ui.theme.LocalAppColors

@Composable
fun SessionStatItem(label: String, value: String) {
    val appColors = LocalAppColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = appColors.textPrimary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = appColors.textSecondary
        )
    }
}
