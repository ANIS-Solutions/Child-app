package com.anis.child.ui.screen.contentprotection.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.anis.child.ui.theme.LocalAppColors

@Composable
fun AppIcon(icon: Drawable?, modifier: Modifier = Modifier) {
    val appColors = LocalAppColors.current
    val bmp = remember(icon) {
        icon?.toBitmap(128, 128)?.asImageBitmap()
    }
    if (bmp != null) {
        Image(
            bitmap = bmp,
            contentDescription = null,
            modifier = modifier.clip(RoundedCornerShape(8.dp))
        )
    } else {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(8.dp))
                .background(appColors.darkSurface.copy(alpha = 0.1f))
        )
    }
}
