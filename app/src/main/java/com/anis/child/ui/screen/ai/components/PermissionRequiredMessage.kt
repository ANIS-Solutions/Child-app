package com.anis.child.ui.screen.ai.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.anis.child.ui.theme.LocalAppColors

@Composable
fun PermissionRequiredMessage(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    Text(
        text = "Required permissions are missing. Please grant them in settings.",
        color = appColors.error500,
        style = MaterialTheme.typography.bodyMedium
    )
    Button(
        onClick = onBack,
        modifier = modifier.fillMaxWidth()
    ) {
        Text("Back")
    }
}
