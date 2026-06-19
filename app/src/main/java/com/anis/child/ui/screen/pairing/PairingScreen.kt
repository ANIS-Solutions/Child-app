package com.anis.child.ui.screen.pairing

import com.anis.child.ui.screen.pairing.components.*
import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anis.child.ui.theme.LocalAppColors
import com.anis.child.R
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PairingScreen(
    viewModel: PairingViewModel,
    onNavigateToHome: () -> Unit
) {
    val appColors = LocalAppColors.current
    val uiState by viewModel.uiState.collectAsState()
    val navigateToHome by viewModel.onNavigateToHome.collectAsState()

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(navigateToHome) {
        if (navigateToHome) {
            onNavigateToHome()
            viewModel.onNavigationComplete()
        }
    }

    when {
        cameraPermissionState.status.isGranted -> {
            when (val state = uiState) {
                is PairingUiState.Scanning -> {
                    QrScannerContent(
                        onQrCodeScanned = { qrContent ->
                            viewModel.processQrCode(qrContent)
                        }
                    )
                }
                is PairingUiState.Loading -> {
                    LoadingOverlay()
                }
                is PairingUiState.Success -> {
                    SuccessContent(
                        childName = state.childData.name,
                        onContinue = {
                            viewModel.resetToScanning()
                        }
                    )
                }
                is PairingUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        details = state.details,
                        onRetry = {
                            viewModel.resetToScanning()
                        }
                    )
                }
            }
        }
        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(appColors.surface50)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (cameraPermissionState.status.shouldShowRationale) {
                        stringResource(R.string.camera_permission_rationale)
                    } else {
                        stringResource(R.string.camera_permission_required)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = appColors.textPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 100.dp)
                )
                Button(
                    onClick = { cameraPermissionState.launchPermissionRequest() },
                    modifier = Modifier.padding(top = 24.dp)
                ) {
                    Text(stringResource(R.string.grant_permission))
                }
            }
        }
    }
}
