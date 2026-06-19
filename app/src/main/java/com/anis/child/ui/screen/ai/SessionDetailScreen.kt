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
import com.anis.child.ui.theme.LocalAppColors
import com.anis.child.R
import androidx.compose.ui.res.stringResource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SessionDetailScreen(
    sessionId: Long,
    onNavigateBack: () -> Unit,
    viewModel: SessionDetailViewModel = hiltViewModel()
) {
    val appColors = LocalAppColors.current
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
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.export_session_zip)))
            viewModel.clearExportUri()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(appColors.surface50)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), tint = appColors.textPrimary)
            }
            Text(
                text = stringResource(R.string.session_details_title),
                color = appColors.textPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { viewModel.exportSession() }) {
                Icon(Icons.Default.Share, contentDescription = stringResource(R.string.export), tint = appColors.textPrimary)
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
                        text = stringResource(R.string.analysis_log),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = appColors.textPrimary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                if (results.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = appColors.darkSurface.copy(alpha = 0.1f)
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.no_analysis_results),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = appColors.textSecondary
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
    val appColors = LocalAppColors.current
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = appColors.primary01.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.session_number, session.id),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = appColors.textPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.started),
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.textSecondary
                    )
                    Text(
                        text = dateFormat.format(Date(session.startTime)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = appColors.textPrimary
                    )
                }

                if (session.endTime != null) {
                    Column {
                        Text(
                            text = stringResource(R.string.ended),
                            style = MaterialTheme.typography.bodySmall,
                            color = appColors.textSecondary
                        )
                        Text(
                            text = dateFormat.format(Date(session.endTime)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = appColors.textPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(label = stringResource(R.string.interval_label), value = "${session.intervalMs}ms")
                StatItem(label = stringResource(R.string.total), value = "${session.totalCaptures}")
                StatItem(label = stringResource(R.string.blocked), value = "${session.blockedCount}")
                StatItem(label = stringResource(R.string.safe), value = "${session.safeCount}")
            }

            if (session.endTime != null && session.batteryStart > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.device_usage),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = appColors.textSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val batteryStr = "${session.batteryStart}% \u2192 ${session.batteryEnd}%"
                    val batteryDelta = session.batteryStart - session.batteryEnd
                    val batteryLabel = if (session.batteryCharging) stringResource(R.string.battery_charging) else stringResource(R.string.battery)
                    StatItem(
                        label = batteryLabel,
                        value = if (batteryDelta > 0) "$batteryStr (-$batteryDelta%)" else batteryStr
                    )
                    StatItem(
                        label = stringResource(R.string.cpu),
                        value = "${"%.1f".format(session.cpuUsagePercent)}%"
                    )
                    StatItem(
                        label = stringResource(R.string.ram),
                        value = "${"%.0f".format(session.ramPssMb)} MB"
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    val appColors = LocalAppColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = appColors.textPrimary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = appColors.textSecondary
        )
    }
}

@Composable
private fun KeyframeSection(keyframeResults: List<AnalysisResultEntity>) {
    val appColors = LocalAppColors.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.keyframes_count, keyframeResults.size),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = appColors.textPrimary,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = appColors.primary01.copy(alpha = 0.1f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = stringResource(R.string.keyframes_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = appColors.textSecondary,
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

@Composable
private fun AnalysisResultItem(result: AnalysisResultEntity) {
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
                        .heightIn(max = 200.dp)
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
