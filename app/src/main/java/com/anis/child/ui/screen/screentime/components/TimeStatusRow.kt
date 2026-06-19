package com.anis.child.ui.screen.screentime.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anis.child.data.ScreenTimeSummary
import com.anis.child.R
import androidx.compose.ui.res.stringResource
import com.anis.child.ui.theme.LocalAppColors

@Composable
fun TimeStatusRow(summary: ScreenTimeSummary) {
    val appColors = LocalAppColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatusChip(
            modifier = Modifier.weight(1f),
            label = if (summary.isLimitReached) stringResource(R.string.limit_reached) else stringResource(R.string.within_limit),
            icon = if (summary.isLimitReached) Icons.Default.Block else Icons.Default.CheckCircle,
            color = if (summary.isLimitReached) appColors.error500 else appColors.success500
        )
        StatusChip(
            modifier = Modifier.weight(1f),
            label = if (summary.isBedtime) stringResource(R.string.bedtime) else stringResource(R.string.not_bedtime),
            icon = Icons.Default.Schedule,
            color = if (summary.isBedtime) appColors.warning500 else appColors.success500
        )
        StatusChip(
            modifier = Modifier.weight(1f),
            label = if (summary.isStudyHours) stringResource(R.string.study_time) else stringResource(R.string.free_time),
            icon = Icons.Default.Timer,
            color = if (summary.isStudyHours) appColors.primary01 else appColors.textSecondary
        )
    }
}
