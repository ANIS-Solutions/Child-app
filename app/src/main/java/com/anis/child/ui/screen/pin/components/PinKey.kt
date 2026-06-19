package com.anis.child.ui.screen.pin.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anis.child.ui.theme.LocalAppColors

@Composable
fun PinKey(
    digit: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val appColors = LocalAppColors.current
    val bgColor = if (enabled) appColors.darkSurface.copy(alpha = 0.1f) else appColors.textDisabled.copy(alpha = 0.1f)
    val textColor = if (enabled) appColors.textPrimary else appColors.textDisabled

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(72.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = bgColor,
            disabledContainerColor = appColors.textDisabled.copy(alpha = 0.05f)
        )
    ) {
        Text(
            text = digit,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
