package com.anis.child.ui.screen.ai.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.anis.child.ui.theme.LocalAppColors

@Composable
fun ErrorStateMessage(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    Text(
        text = "Error: $message",
        color = appColors.error500,
        style = MaterialTheme.typography.bodyMedium
    )
    Button(
        onClick = onRetry,
        modifier = modifier.fillMaxWidth()
    ) {
        Text("Retry")
    }
}
