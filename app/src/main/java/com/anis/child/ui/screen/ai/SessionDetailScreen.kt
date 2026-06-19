package com.anis.child.ui.screen.ai

import com.anis.child.ui.screen.ai.components.*
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.anis.child.ui.theme.LocalAppColors
import com.anis.child.R
import androidx.compose.ui.res.stringResource

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
        SessionDetailTopBar(
            onBack = onNavigateBack,
            onExport = { viewModel.exportSession() }
        )

        if (session == null) {
            SessionDetailLoading()
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
                        NoAnalysisResultsCard()
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
