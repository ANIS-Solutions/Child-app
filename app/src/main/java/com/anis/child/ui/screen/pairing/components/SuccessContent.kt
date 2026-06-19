package com.anis.child.ui.screen.pairing.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anis.child.R
import androidx.compose.ui.res.stringResource
import com.anis.child.ui.theme.LocalAppColors

@Composable
fun SuccessContent(
    childName: String,
    onContinue: () -> Unit
) {
    val appColors = LocalAppColors.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(appColors.surface50),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(appColors.success500, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "\u2713",
                    style = MaterialTheme.typography.headlineLarge,
                    color = appColors.darkTextPrimary
                )
            }
            Text(
                text = stringResource(R.string.pairing_successful),
                style = MaterialTheme.typography.headlineSmall,
                color = appColors.textPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp)
            )
            Text(
                text = stringResource(R.string.pairing_connected, childName),
                style = MaterialTheme.typography.bodyLarge,
                color = appColors.textSecondary,
                modifier = Modifier.padding(top = 8.dp)
            )
            Button(
                onClick = onContinue,
                modifier = Modifier.padding(top = 32.dp)
            ) {
                Text(stringResource(R.string.continue_btn))
            }
        }
    }
}
