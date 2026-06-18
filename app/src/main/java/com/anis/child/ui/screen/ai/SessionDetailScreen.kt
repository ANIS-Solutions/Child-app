package com.anis.child.ui.screen.ai

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.anis.child.ai.util.ImageStorageManager
import com.anis.child.data.local.AnalysisResultEntity
import com.anis.child.data.local.SessionEntity
import com.anis.child.ui.theme.AppColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SessionDetailScreen(
    sessionId: Long,
    onNavigateBack: () -> Unit,
    viewModel: SessionDetailViewModel = hiltViewModel()
) {
    val session by viewModel.session.collectAsState()
    val results by viewModel.results.collectAsState()
    val exportUri by viewModel.exportUri.collectAsState()
    val keyframeResults by viewModel.keyframeResults.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(sessionId) {
        viewModel.loadSession(sessionId)
    }

    LaunchedEffect(exportUri) {
        exportUri?.let { uri ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Export Session (ZIP)"))
            viewModel.clearExportUri()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.surface50)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = AppColors.textPrimary)
            }
            Text(
                text = "Session Details",
                color = AppColors.textPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { viewModel.exportSession() }) {
                Icon(Icons.Default.Share, contentDescription = "Export", tint = AppColors.textPrimary)
            }
        }

        if (session == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    SessionSummaryCard(session = session!!)
                }

                if (keyframeResults.isNotEmpty()) {
                    item {
                        KeyframeSection(keyframeResults = keyframeResults)
                    }
                }

                item {
                    Text(
                        text = "Analysis Log",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                if (results.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = AppColors.darkSurface.copy(alpha = 0.1f)
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No analysis results yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AppColors.textSecondary
                                )
                            }
                        }
                    }
                } else {
                    items(results, key = { it.id }) { result ->
                        AnalysisResultItem(result = result)
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionSummaryCard(session: SessionEntity) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.primary01.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Session #${session.id}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Started",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.textSecondary
                    )
                    Text(
                        text = dateFormat.format(Date(session.startTime)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.textPrimary
                    )
                }

                if (session.endTime != null) {
                    Column {
                        Text(
                            text = "Ended",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.textSecondary
                        )
                        Text(
                            text = dateFormat.format(Date(session.endTime)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.textPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(label = "Interval", value = "${session.intervalMs}ms")
                StatItem(label = "Total", value = "${session.totalCaptures}")
                StatItem(label = "Blocked", value = "${session.blockedCount}")
                StatItem(label = "Safe", value = "${session.safeCount}")
            }

            if (session.endTime != null && session.batteryStart > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Device Usage",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val batteryStr = "${session.batteryStart}% \u2192 ${session.batteryEnd}%"
                    val batteryDelta = session.batteryStart - session.batteryEnd
                    val batteryLabel = if (session.batteryCharging) "Battery \u26a1" else "Battery"
                    StatItem(
                        label = batteryLabel,
                        value = if (batteryDelta > 0) "$batteryStr (-$batteryDelta%)" else batteryStr
                    )
                    StatItem(
                        label = "CPU",
                        value = "${"%.1f".format(session.cpuUsagePercent)}%"
                    )
                    StatItem(
                        label = "RAM",
                        value = "${"%.0f".format(session.ramPssMb)} MB"
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.textPrimary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.textSecondary
        )
    }
}

@Composable
private fun KeyframeSection(keyframeResults: List<AnalysisResultEntity>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Keyframes (${keyframeResults.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.textPrimary,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = AppColors.primary01.copy(alpha = 0.1f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Most representative frames from this session",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.textSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(keyframeResults, key = { it.id }) { result ->
                        KeyframeThumbnail(result = result)
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyframeThumbnail(result: AnalysisResultEntity) {
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
                AppColors.error500.copy(alpha = 0.15f)
            else AppColors.darkSurface.copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            bitmap?.let { bmp ->
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Keyframe",
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
                    .background(AppColors.darkSurface.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No image",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = timeFormat.format(Date(result.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.textSecondary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun AnalysisResultItem(result: AnalysisResultEntity) {
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
                AppColors.error500.copy(alpha = 0.15f)
            } else {
                AppColors.darkSurface.copy(alpha = 0.1f)
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
                    color = AppColors.textSecondary
                )
                AssistChip(
                    onClick = {},
                    label = { Text(result.decision) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (isBlocked) AppColors.error500 else AppColors.success500,
                        labelColor = AppColors.darkTextPrimary
                    )
                )
            }

            bitmap?.let { bmp ->
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Captured image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = result.analysisResult,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Start,
                color = AppColors.textPrimary
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
                            color = AppColors.textSecondary
                        )
                    }
                    if (result.onnxTimeMs > 0) {
                        Text(
                            text = "ONNX: ${String.format("%.2f", result.onnxTimeMs)}ms",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppColors.textSecondary
                        )
                    }
                }
            }
        }
    }
}
