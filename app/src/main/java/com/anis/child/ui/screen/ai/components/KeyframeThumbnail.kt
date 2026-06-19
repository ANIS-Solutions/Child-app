package com.anis.child.ui.screen.ai.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.anis.child.ai.util.ImageStorageManager
import com.anis.child.data.local.AnalysisResultEntity
import com.anis.child.R
import androidx.compose.ui.res.stringResource
import com.anis.child.ui.theme.LocalAppColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun KeyframeThumbnail(result: AnalysisResultEntity) {
    val appColors = LocalAppColors.current
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    val context = LocalContext.current

    LaunchedEffect(result.imagePath) {
        result.imagePath?.let { path ->
            bitmap = ImageStorageManager.loadBitmapFromPath(context, path)
        }
    }

    Card(
        modifier = Modifier.width(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (result.decision == "BLOCKED")
                appColors.error500.copy(alpha = 0.15f)
            else appColors.darkSurface.copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            bitmap?.let { bmp ->
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = stringResource(R.string.keyframe),
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )
            } ?: Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(appColors.darkSurface.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_image),
                    style = MaterialTheme.typography.labelSmall,
                    color = appColors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = timeFormat.format(Date(result.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = appColors.textSecondary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
