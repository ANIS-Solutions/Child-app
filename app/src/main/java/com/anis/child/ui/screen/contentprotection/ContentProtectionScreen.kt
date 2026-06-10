package com.anis.child.ui.screen.contentprotection

import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SettingsAccessibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anis.child.content.AppBlockAccessibilityService
import com.anis.child.data.local.ContentFilterRuleEntity
import com.anis.child.ui.theme.AppColors

@Composable
fun ContentProtectionScreen(
    viewModel: ContentProtectionViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

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
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Back", tint = AppColors.textPrimary)
            }
            Text(
                text = "Content Protection",
                color = AppColors.textPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.primary01)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    AccessibilityStatusCard(
                        onOpenAccessibilitySettings = { viewModel.openAccessibilitySettings() }
                    )
                }

                item {
                    Text(
                        "Blocked Apps",
                        style = MaterialTheme.typography.titleLarge,
                        color = AppColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(uiState.installedApps.filter { it.isBlocked }) { app ->
                    BlockedAppCard(
                        app = app,
                        onUnblock = { viewModel.toggleAppBlock(app.packageName, false) }
                    )
                }

                if (uiState.installedApps.none { it.isBlocked }) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = AppColors.darkSurface.copy(alpha = 0.05f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, null, tint = AppColors.success500, modifier = Modifier.size(24.dp))
                                Spacer(Modifier.width(12.dp))
                                Text("No apps are currently blocked", color = AppColors.textSecondary)
                            }
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "All Apps",
                        style = MaterialTheme.typography.titleLarge,
                        color = AppColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Tap an app to block or unblock it",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.textSecondary
                    )
                }

                items(uiState.installedApps) { app ->
                    AppListItem(
                        app = app,
                        onToggle = { viewModel.toggleAppBlock(app.packageName, !app.isBlocked) }
                    )
                }

                item {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Content Filter Rules",
                        style = MaterialTheme.typography.titleLarge,
                        color = AppColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    AddRuleSection(viewModel = viewModel)
                }

                items(uiState.filterRules) { rule ->
                    FilterRuleCard(
                        rule = rule,
                        onToggle = { viewModel.toggleFilterRule(rule.id, !rule.isBlocked) },
                        onDelete = { viewModel.deleteFilterRule(rule) }
                    )
                }

                if (uiState.filterRules.isEmpty()) {
                    item {
                        var showDefaults by remember { mutableStateOf(false) }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = AppColors.darkSurface.copy(alpha = 0.05f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.FilterList, null, tint = AppColors.textDisabled, modifier = Modifier.size(32.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("No filter rules configured", style = MaterialTheme.typography.bodyMedium, color = AppColors.textSecondary)
                                Spacer(Modifier.height(8.dp))
                                OutlinedButton(onClick = { viewModel.addDefaultFilterRules() }) {
                                    Text("Add Default Rules")
                                }
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
private fun AccessibilityStatusCard(onOpenAccessibilitySettings: () -> Unit = {}) {
    val context = LocalContext.current
    val pm = context.packageManager
    val isEnabled = try {
        pm.getServiceInfo(
            android.content.ComponentName(context, AppBlockAccessibilityService::class.java),
            PackageManager.GET_META_DATA
        ) != null
    } catch (_: Exception) { false }

    Card(
        colors = CardDefaults.cardColors(containerColor = AppColors.warning500.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warning,
                null,
                tint = AppColors.warning500,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Accessibility Service Required",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textPrimary
                )
                Text(
                    "Enable the ANIS Content Protection service in your device's Accessibility settings for real-time app blocking.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.textSecondary
                )
            }
        }
        Button(
            onClick = onOpenAccessibilitySettings,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary01)
        ) {
            Icon(Icons.Default.SettingsAccessibility, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Open Accessibility Settings", color = AppColors.darkTextPrimary)
        }
    }
}

@Composable
private fun BlockedAppCard(
    app: InstalledAppInfo,
    onUnblock: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppColors.error500.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Block, null, tint = AppColors.error500, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(app.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                Text(app.packageName, style = MaterialTheme.typography.bodySmall, color = AppColors.textSecondary)
            }
            FilledTonalButton(onClick = onUnblock) {
                Text("Unblock", color = AppColors.error500)
            }
        }
    }
}

@Composable
private fun AppListItem(
    app: InstalledAppInfo,
    onToggle: () -> Unit
) {
    Card(
        onClick = onToggle,
        colors = CardDefaults.cardColors(
            containerColor = if (app.isBlocked) AppColors.error500.copy(alpha = 0.08f)
            else AppColors.darkSurface.copy(alpha = 0.03f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (app.isBlocked) Icons.Default.Block else Icons.Default.Lock,
                null,
                tint = if (app.isBlocked) AppColors.error500 else AppColors.textDisabled,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(app.label, style = MaterialTheme.typography.bodyMedium, color = AppColors.textPrimary)
                Text(app.packageName, style = MaterialTheme.typography.bodySmall, color = AppColors.textSecondary)
            }
            Text(
                if (app.isBlocked) "Blocked" else "Allowed",
                color = if (app.isBlocked) AppColors.error500 else AppColors.success500,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun AddRuleSection(viewModel: ContentProtectionViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    if (!expanded) {
        FilledTonalButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Add Filter Rule")
        }
        return
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = AppColors.darkSurface.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("New Rule", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)

            Spacer(Modifier.height(8.dp))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = uiState.newRuleType == "keyword",
                    onClick = { viewModel.onNewRuleTypeChanged("keyword") },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) { Text("Keyword") }
                SegmentedButton(
                    selected = uiState.newRuleType == "url",
                    onClick = { viewModel.onNewRuleTypeChanged("url") },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) { Text("URL") }
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.newRulePattern,
                onValueChange = { viewModel.onNewRulePatternChanged(it) },
                label = { Text(if (uiState.newRuleType == "keyword") "e.g. *gambling*" else "e.g. *casino*") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.addFilterRule(); expanded = false },
                    enabled = uiState.newRulePattern.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary01)
                ) {
                    Text("Add", color = AppColors.darkTextPrimary)
                }
                OutlinedButton(onClick = { expanded = false }) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
private fun FilterRuleCard(
    rule: ContentFilterRuleEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val typeColor = if (rule.type == "keyword") AppColors.game500 else AppColors.entertainment500

    Card(
        onClick = onToggle,
        colors = CardDefaults.cardColors(
            containerColor = if (rule.isBlocked) AppColors.darkSurface.copy(alpha = 0.08f)
            else AppColors.darkSurface.copy(alpha = 0.03f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        rule.pattern,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (rule.isBlocked) AppColors.textPrimary else AppColors.textDisabled,
                        fontWeight = if (rule.isBlocked) FontWeight.Medium else FontWeight.Normal
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        rule.type.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = typeColor
                    )
                }
            }
            if (!rule.isBlocked) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete", tint = AppColors.error500)
                }
            } else {
                Icon(
                    Icons.Default.CheckCircle,
                    "Active",
                    tint = AppColors.success500,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
