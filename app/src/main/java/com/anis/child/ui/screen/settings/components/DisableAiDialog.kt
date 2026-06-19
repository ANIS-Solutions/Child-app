package com.anis.child.ui.screen.settings.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.anis.child.ui.theme.LocalAppColors
import com.anis.child.R
import androidx.compose.ui.res.stringResource

@Composable
fun DisableAiDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val appColors = LocalAppColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.disable_ai_dialog_title)) },
        text = {
            Text(stringResource(R.string.disable_ai_dialog_text))
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.disable), color = appColors.error500)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.keep_enabled))
            }
        }
    )
}
