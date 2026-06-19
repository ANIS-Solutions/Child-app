package com.anis.child.ui.screen.ai.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anis.child.ui.theme.LocalAppColors

@Composable
fun StopMonitoringButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = appColors.error500)
    ) {
        Icon(
            imageVector = Icons.Default.Stop,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = "Stop Monitoring",
            color = appColors.darkTextPrimary,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
