package com.anis.child.ui.screen.ai.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anis.child.data.local.AnalysisResultEntity
import com.anis.child.R
import androidx.compose.ui.res.stringResource
import com.anis.child.ui.theme.LocalAppColors

@Composable
fun KeyframeSection(keyframeResults: List<AnalysisResultEntity>) {
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
