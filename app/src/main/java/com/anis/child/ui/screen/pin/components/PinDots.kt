package com.anis.child.ui.screen.pin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.anis.child.ui.theme.LocalAppColors

@Composable
fun PinDots(
    pin: String,
    maxLength: Int,
    isError: Boolean
) {
    val appColors = LocalAppColors.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        for (i in 0 until maxLength) {
            val isFilled = i < pin.length
            val color = when {
                isError -> appColors.error500
                isFilled -> appColors.primary01
                else -> appColors.textDisabled
            }
            Box(
                modifier = Modifier
                    .size(if (isFilled) 16.dp else 12.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}
