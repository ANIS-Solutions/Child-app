package com.anis.child.ui.screen.contentprotection

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.anis.child.ui.theme.LocalAppColors
import com.anis.child.R
import androidx.compose.ui.res.stringResource

@Composable
fun ContentProtectionScreen(
    viewModel: ContentProtectionViewModel,
    onBack: () -> Unit
) {
    val appColors = LocalAppColors.current
    val uiState by viewModel.uiState.collectAsState()

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
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, stringResource(R.string.back), tint = appColors.textPrimary)
            }
            Text(
                text = stringResource(R.string.content_protection_title),
                color = appColors.textPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = appColors.primary01)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.installedApps) { app ->
                    AppCard(
                        app = app,
                        onToggleBlock = { viewModel.toggleAppBlock(app.packageName, !app.isBlocked) },
                        onSetLimit = { minutes -> viewModel.setDailyTimeLimit(app.packageName, minutes) }
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun AppCard(
    app: InstalledAppInfo,
    onToggleBlock: () -> Unit,
    onSetLimit: (Int) -> Unit
) {
    val appColors = LocalAppColors.current
    var showLimitDialog by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (app.isBlocked || app.dailyTimeLimitMinutes > 0)
                appColors.error500.copy(alpha = 0.06f)
            else appColors.darkSurface.copy(alpha = 0.03f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(app.icon, modifier = Modifier.size(40.dp))

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = appColors.textPrimary
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = appColors.textSecondary,
                    maxLines = 1
                )
                if (app.dailyTimeLimitMinutes > 0) {
                    Text(
                        text = "${app.dailyTimeLimitMinutes} min/day limit",
                        style = MaterialTheme.typography.labelSmall,
                        color = appColors.warning500
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            OutlinedButton(
                onClick = { showLimitDialog = true },
                modifier = Modifier.height(32.dp)
            ) {
                Text(stringResource(R.string.limit_btn), fontSize = 12.sp)
            }

            Spacer(Modifier.width(4.dp))

            Switch(
                checked = app.isBlocked,
                onCheckedChange = { onToggleBlock() }
            )
        }
    }

    if (showLimitDialog) {
        LimitDialog(
            currentMinutes = app.dailyTimeLimitMinutes,
            onConfirm = { minutes ->
                onSetLimit(minutes)
                showLimitDialog = false
            },
            onDismiss = { showLimitDialog = false }
        )
    }
}

@Composable
private fun AppIcon(icon: Drawable?, modifier: Modifier = Modifier) {
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

@Composable
private fun LimitDialog(
    currentMinutes: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = LocalAppColors.current
    var text by remember { mutableStateOf(if (currentMinutes > 0) currentMinutes.toString() else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.set_daily_time_limit_title)) },
        text = {
            Column {
                Text(
                    stringResource(R.string.set_daily_time_limit_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = appColors.textSecondary
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.filter { c -> c.isDigit() } },
                    label = { Text(stringResource(R.string.minutes)) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text.toIntOrNull() ?: 0) }) {
                Text(stringResource(R.string.set))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
