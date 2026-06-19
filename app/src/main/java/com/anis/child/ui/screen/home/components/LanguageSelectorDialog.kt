package com.anis.child.ui.screen.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.anis.child.R
import androidx.compose.ui.res.stringResource

@Composable
fun LanguageSelectorDialog(
    onDismiss: () -> Unit,
    onLanguageChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.language)) },
        text = {
            Column {
                TextButton(
                    onClick = {
                        onLanguageChange("en")
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.language_en),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                TextButton(
                    onClick = {
                        onLanguageChange("ar")
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.language_ar),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {}
    )
}
