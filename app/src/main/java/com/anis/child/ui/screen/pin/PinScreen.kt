package com.anis.child.ui.screen.pin

import com.anis.child.ui.screen.pin.components.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anis.child.ui.theme.LocalAppColors
import com.anis.child.R
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.delay

@Composable
fun PinScreen(
    viewModel: PinViewModel,
    isSettingUp: Boolean = false,
    onVerified: () -> Unit,
    onCancel: (() -> Unit)? = null
) {
    val appColors = LocalAppColors.current
    val uiState by viewModel.uiState.collectAsState()
    val enteredPin by viewModel.enteredPin.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.start(isSettingUp)
    }

    LaunchedEffect(uiState) {
        if (uiState is PinUiState.Verified) {
            delay(300)
            onVerified()
            viewModel.onVerifiedHandled()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(appColors.surface50)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val title = when (uiState) {
                is PinUiState.Creating -> stringResource(R.string.pin_create)
                is PinUiState.ConfirmNew -> stringResource(R.string.pin_confirm)
                is PinUiState.Entry -> stringResource(R.string.pin_enter)
                is PinUiState.LockedOut -> stringResource(R.string.pin_locked_out)
                is PinUiState.Error -> stringResource(R.string.pin_enter)
                is PinUiState.Verified -> stringResource(R.string.pin_verified)
            }

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = appColors.textPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            val subtitle = when (uiState) {
                is PinUiState.Creating -> stringResource(R.string.pin_create_subtitle)
                is PinUiState.ConfirmNew -> stringResource(R.string.pin_confirm_subtitle)
                is PinUiState.Entry -> stringResource(R.string.pin_enter_subtitle)
                is PinUiState.LockedOut -> stringResource(R.string.pin_locked_out_subtitle)
                is PinUiState.Error -> (uiState as PinUiState.Error).message
                is PinUiState.Verified -> ""
            }

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = appColors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            PinDots(
                pin = enteredPin,
                maxLength = 4,
                isError = uiState is PinUiState.Error
            )

            Spacer(modifier = Modifier.height(32.dp))

            PinKeypad(
                onDigitClick = { viewModel.appendDigit(it) },
                onDeleteClick = { viewModel.deleteDigit() },
                enabled = uiState !is PinUiState.LockedOut && uiState !is PinUiState.Verified
            )

            if (uiState is PinUiState.Entry && onCancel != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = appColors.error500
                    )
                ) {
                    Text(stringResource(R.string.cancel), color = appColors.darkTextPrimary)
                }
            }

            if (uiState is PinUiState.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.reset() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = appColors.primary01
                    )
                ) {
                    Text(stringResource(R.string.try_again), color = appColors.darkTextPrimary)
                }
            }
        }
    }
}
