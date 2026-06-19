package com.anis.child.ui.screen.ai.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.anis.child.ui.theme.LocalAppColors

@Composable
fun ViewHistoryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = appColors.darkSurface.copy(alpha = 0.2f)
        )
    ) {
        Text(
            text = "View Session History",
            color = appColors.textPrimary
        )
    }
}
