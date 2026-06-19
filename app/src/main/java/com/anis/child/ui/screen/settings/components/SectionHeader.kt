package com.anis.child.ui.screen.settings.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anis.child.ui.theme.LocalAppColors

@Composable
fun SectionHeader(title: String) {
    val appColors = LocalAppColors.current
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = appColors.textSecondary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 4.dp)
    )
}
