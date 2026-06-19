package com.anis.child.ui.screen.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anis.child.data.LogEntry
import com.anis.child.data.LogType
import com.anis.child.ui.theme.LocalAppColors

@Composable
fun LogItem(
    entry: LogEntry,
    formatTime: (Long) -> String
) {
    val appColors = LocalAppColors.current
    val backgroundColor = when (entry.type) {
        LogType.INFO -> Color.Transparent
        LogType.SUCCESS -> appColors.success500.copy(alpha = 0.1f)
        LogType.ERROR -> appColors.error500.copy(alpha = 0.1f)
        LogType.LOCATION -> appColors.primary01.copy(alpha = 0.1f)
        LogType.HTTP -> appColors.warning500.copy(alpha = 0.1f)
        LogType.NOTIFICATION -> Color.Transparent
    }

    val textColor = when (entry.type) {
        LogType.INFO -> appColors.textPrimary
        LogType.SUCCESS -> appColors.success500
        LogType.ERROR -> appColors.error500
        LogType.LOCATION -> appColors.primary01
        LogType.HTTP -> appColors.warning500
        LogType.NOTIFICATION -> appColors.textPrimary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 4.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = formatTime(entry.timestamp),
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
            ),
            color = appColors.textDisabled,
            modifier = Modifier.width(50.dp)
        )

        Text(
            text = entry.message,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
            ),
            color = textColor,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
