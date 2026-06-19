package com.anis.child.ui.screen.settings.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.anis.child.ui.theme.LocalAppColors

@Composable
fun ActionButton(
    icon: ImageVector,
    label: String,
    isLoading: Boolean,
    loadingLabel: String,
    onClick: () -> Unit,
) {
    val appColors = LocalAppColors.current
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = appColors.darkSurface.copy(alpha = 0.15f)
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = appColors.textPrimary,
                strokeWidth = 2.dp
            )
            Text(
                text = loadingLabel,
                color = appColors.textPrimary,
                modifier = Modifier.padding(start = 8.dp)
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = appColors.textPrimary
            )
            Text(
                text = label,
                color = appColors.textPrimary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
