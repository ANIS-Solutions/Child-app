package com.anis.child.ui.screen.pin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anis.child.ui.theme.AppColors
import kotlinx.coroutines.delay

@Composable
fun PinScreen(
    viewModel: PinViewModel,
    isSettingUp: Boolean = false,
    onVerified: () -> Unit,
    onCancel: (() -> Unit)? = null
) {
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
            .background(AppColors.surface50)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val title = when (uiState) {
                is PinUiState.Creating -> "Create PIN"
                is PinUiState.ConfirmNew -> "Confirm PIN"
                is PinUiState.Entry -> "Enter PIN"
                is PinUiState.LockedOut -> "Too Many Attempts"
                is PinUiState.Error -> "Enter PIN"
                is PinUiState.Verified -> "Verified!"
            }

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = AppColors.textPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            val subtitle = when (uiState) {
                is PinUiState.Creating -> "Set a 4-6 digit PIN to protect Settings"
                is PinUiState.ConfirmNew -> "Re-enter your new PIN"
                is PinUiState.Entry -> "Enter your PIN to access Settings"
                is PinUiState.LockedOut -> "Please wait 30 seconds before trying again"
                is PinUiState.Error -> (uiState as PinUiState.Error).message
                is PinUiState.Verified -> ""
            }

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.textSecondary,
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
                        containerColor = AppColors.error500
                    )
                ) {
                    Text("Cancel", color = AppColors.darkTextPrimary)
                }
            }

            if (uiState is PinUiState.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.reset() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.primary01
                    )
                ) {
                    Text("Try Again", color = AppColors.darkTextPrimary)
                }
            }
        }
    }
}

@Composable
private fun PinDots(
    pin: String,
    maxLength: Int,
    isError: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until maxLength) {
            val isFilled = i < pin.length
            val color = when {
                isError -> AppColors.error500
                isFilled -> AppColors.primary01
                else -> AppColors.textDisabled
            }
            Box(
                modifier = Modifier
                    .size(if (isFilled) 16.dp else 12.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
private fun PinKeypad(
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    enabled: Boolean
) {
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
                    contentDescription = "Delete",
                    tint = if (enabled) AppColors.textPrimary else AppColors.textDisabled,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun PinKey(
    digit: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (enabled) AppColors.darkSurface.copy(alpha = 0.1f) else AppColors.textDisabled.copy(alpha = 0.1f)
    val textColor = if (enabled) AppColors.textPrimary else AppColors.textDisabled

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(72.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = bgColor,
            disabledContainerColor = AppColors.textDisabled.copy(alpha = 0.05f)
        )
    ) {
        Text(
            text = digit,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
