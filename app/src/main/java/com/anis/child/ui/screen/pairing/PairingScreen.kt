package com.anis.child.ui.screen.pairing

import android.Manifest
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.anis.child.ui.theme.LocalAppColors
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

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
                        "Camera permission is required to scan QR codes"
                    } else {
                        "Please grant camera permission to continue"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = appColors.textPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 100.dp)
                )
                androidx.compose.material3.Button(
                    onClick = { cameraPermissionState.launchPermissionRequest() },
                    modifier = Modifier.padding(top = 24.dp)
                ) {
                    Text("Grant Permission")
                }
            }
        }
    }
}

@Composable
private fun QrScannerContent(
    onQrCodeScanned: (String) -> Unit
) {
    val appColors = LocalAppColors.current
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasScanned by mutableStateOf(false)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(appColors.surface50)
    ) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    val barcodeScanner = BarcodeScanning.getClient()

                    imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null && !hasScanned) {
                            val image = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )
                            barcodeScanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    for (barcode in barcodes) {
                                        if (barcode.valueType == Barcode.TYPE_TEXT ||
                                            barcode.valueType == Barcode.TYPE_UNKNOWN
                                        ) {
                                            barcode.rawValue?.let { value ->
                                                if (value.contains("action")) {
                                                    hasScanned = true
                                                    onQrCodeScanned(value)
                                                }
                                            }
                                        }
                                    }
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        } else {
                            imageProxy.close()
                        }
                    }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(context))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = "Point camera at QR code",
                color = appColors.textPrimary,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .background(
                        appColors.surface50.copy(alpha = 0.8f),
                        MaterialTheme.shapes.medium
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun LoadingOverlay() {
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
                text = "Pairing device...",
                style = MaterialTheme.typography.bodyLarge,
                color = appColors.textPrimary,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
private fun SuccessContent(
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
                    text = "✓",
                    style = MaterialTheme.typography.headlineLarge,
                    color = appColors.darkTextPrimary
                )
            }
            Text(
                text = "Pairing Successful!",
                style = MaterialTheme.typography.headlineSmall,
                color = appColors.textPrimary,
                modifier = Modifier.padding(top = 24.dp)
            )
            Text(
                text = "Connected to $childName",
                style = MaterialTheme.typography.bodyLarge,
                color = appColors.textSecondary,
                modifier = Modifier.padding(top = 8.dp)
            )
            androidx.compose.material3.Button(
                onClick = onContinue,
                modifier = Modifier.padding(top = 32.dp)
            ) {
                Text("Continue")
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    details: String? = null,
    onRetry: () -> Unit
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
            modifier = Modifier
                .padding(32.dp)
                .padding(bottom = 48.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(appColors.error500, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✕",
                    style = MaterialTheme.typography.headlineLarge,
                    color = appColors.darkTextPrimary
                )
            }
            Text(
                text = "Pairing Failed",
                style = MaterialTheme.typography.headlineSmall,
                color = appColors.textPrimary,
                modifier = Modifier.padding(top = 24.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = appColors.textSecondary,
                modifier = Modifier.padding(top = 8.dp)
            )
            if (details != null) {
                Text(
                    text = details,
                    style = MaterialTheme.typography.bodySmall,
                    color = appColors.textDisabled,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
                )
            }
            androidx.compose.material3.Button(
                onClick = onRetry,
                modifier = Modifier.padding(top = 32.dp)
            ) {
                Text("Try Again")
            }
        }
    }
}