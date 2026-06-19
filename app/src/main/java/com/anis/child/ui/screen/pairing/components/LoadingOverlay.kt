package com.anis.child.ui.screen.pairing.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anis.child.R
import androidx.compose.ui.res.stringResource
import com.anis.child.ui.theme.LocalAppColors

@Composable
fun LoadingOverlay() {
    val appColors = LocalAppColors.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(appColors.surface50),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                color = appColors.primary01
            )
            Text(
                text = stringResource(R.string.pairing_device),
                style = MaterialTheme.typography.bodyLarge,
                color = appColors.textPrimary,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
