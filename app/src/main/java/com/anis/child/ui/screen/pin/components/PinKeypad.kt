package com.anis.child.ui.screen.pin.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anis.child.R
import androidx.compose.ui.res.stringResource
import com.anis.child.ui.theme.LocalAppColors

@Composable
fun PinKeypad(
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    enabled: Boolean
) {
    val appColors = LocalAppColors.current
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (row in listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"))) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                row.forEach { digit ->
                    PinKey(
                        digit = digit,
                        enabled = enabled,
                        onClick = { onDigitClick(digit) }
                    )
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.size(72.dp))

            PinKey(
                digit = "0",
                enabled = enabled,
                onClick = { onDigitClick("0") }
            )

            IconButton(
                onClick = onDeleteClick,
                enabled = enabled,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Backspace,
                    contentDescription = stringResource(R.string.delete),
                    tint = if (enabled) appColors.textPrimary else appColors.textDisabled,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
