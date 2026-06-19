package com.anis.child.ui.screen.ai.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.anis.child.data.local.AnalysisResultEntity
import com.anis.child.R
import com.anis.child.ui.theme.LocalAppColors
import androidx.compose.ui.res.stringResource
import com.anis.child.ui.theme.LocalAppColors
import android.graphics.BitmapFactory

@Composable
fun SessionHistoryKeyframeThumbnail(result: AnalysisResultEntity) {
    val appColors = LocalAppColors.current
    var bitmap by remember(result.id) { mutableStateOf<android.graphics.Bitmap?>(null) }

    LaunchedEffect(result.imagePath) {
        val path = result.imagePath
        if (path != null) {
            bitmap = BitmapFactory.decodeFile(path)
        }
    }

    Card(
        modifier = Modifier.size(100.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        bitmap?.let { bmp ->
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "Keyframe ${result.id}",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        } ?: Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors.darkSurface.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.no_image),
                style = MaterialTheme.typography.labelSmall,
                color = appColors.textSecondary
            )
        }
    }
}
