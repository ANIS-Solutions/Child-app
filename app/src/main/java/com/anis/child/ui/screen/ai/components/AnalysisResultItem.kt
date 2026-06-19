package com.anis.child.ui.screen.ai.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
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
fun AnalysisResultItem(result: AnalysisResultEntity) {
    val appColors = LocalAppColors.current
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val isBlocked = result.decision == "BLOCKED"
    val context = LocalContext.current

    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(result.imagePath) {
        result.imagePath?.let { path ->
            bitmap = ImageStorageManager.loadBitmapFromPath(context, path)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isBlocked) {
                appColors.error500.copy(alpha = 0.15f)
            } else {
                appColors.darkSurface.copy(alpha = 0.1f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = timeFormat.format(Date(result.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = appColors.textSecondary
                )
                AssistChip(
                    onClick = {},
                    label = { Text(result.decision) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (isBlocked) appColors.error500 else appColors.success500,
                        labelColor = appColors.darkTextPrimary
                    )
                )
            }

            bitmap?.let { bmp ->
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = stringResource(R.string.captured_image),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = result.analysisResult,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Start,
                color = appColors.textPrimary
            )

            if (result.ocrTimeMs > 0 || result.onnxTimeMs > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (result.ocrTimeMs > 0) {
                        Text(
                            text = "OCR: ${String.format("%.2f", result.ocrTimeMs)}ms",
                            style = MaterialTheme.typography.labelSmall,
                            color = appColors.textSecondary
                        )
                    }
                    if (result.onnxTimeMs > 0) {
                        Text(
                            text = "ONNX: ${String.format("%.2f", result.onnxTimeMs)}ms",
                            style = MaterialTheme.typography.labelSmall,
                            color = appColors.textSecondary
                        )
                    }
                }
            }
        }
    }
}
