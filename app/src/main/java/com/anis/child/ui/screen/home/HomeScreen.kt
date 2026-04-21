package com.anis.child.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anis.child.data.LogManager
import com.anis.child.ui.theme.AppColors

@Composable
fun HomeScreen(
    childName: String,
    logManager: LogManager,
    onSendLocationClick: () -> Unit,
    isSending: Boolean,
    onSettingsClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.End
        ) {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = AppColors.textPrimary
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome, $childName!",
                color = AppColors.textPrimary,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Button(
                onClick = onSendLocationClick,
                enabled = !isSending,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.primary01
                ),
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = AppColors.darkTextPrimary,
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "Sending...",
                        color = AppColors.darkTextPrimary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Send Location",
                        color = AppColors.darkTextPrimary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        LogSection(
            logManager = logManager,
            modifier = Modifier.fillMaxWidth()
        )
    }
}