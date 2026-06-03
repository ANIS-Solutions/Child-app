package com.anis.child.ui.screen.pin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anis.child.data.PinManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PinUiState {
    data object Entry : PinUiState()
    data object Creating : PinUiState()
    data object ConfirmNew : PinUiState()
    data class Error(val message: String) : PinUiState()
    data object LockedOut : PinUiState()
    data object Verified : PinUiState()
}

@HiltViewModel
class PinViewModel @Inject constructor(
    private val pinManager: PinManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<PinUiState>(PinUiState.Entry)
    val uiState: StateFlow<PinUiState> = _uiState.asStateFlow()

    private val _enteredPin = MutableStateFlow("")
    val enteredPin: StateFlow<String> = _enteredPin.asStateFlow()

    private var firstPin: String? = null
    private var lockoutUpdateJob: Job? = null

    fun start(isSettingUp: Boolean = false) {
        if (pinManager.isLockedOut()) {
            _uiState.value = PinUiState.LockedOut
            startLockoutTimer()
        } else if (isSettingUp || !pinManager.isPinSet()) {
            _uiState.value = PinUiState.Creating
        } else {
            _uiState.value = PinUiState.Entry
        }
    }

    fun appendDigit(digit: String) {
        if (_enteredPin.value.length >= 6) return
        _enteredPin.value += digit
        if (_enteredPin.value.length >= 4) {
            when (val state = _uiState.value) {
                is PinUiState.Entry -> validatePin()
                is PinUiState.Creating -> onFirstPinEntered()
                is PinUiState.ConfirmNew -> onConfirmPin()
                else -> {}
            }
        }
    }

    fun deleteDigit() {
        if (_enteredPin.value.isNotEmpty()) {
            _enteredPin.value = _enteredPin.value.dropLast(1)
        }
    }

    fun clearEntry() {
        _enteredPin.value = ""
    }

    private fun validatePin() {
        if (_enteredPin.value.length < 4) return
        if (pinManager.validatePin(_enteredPin.value)) {
            _uiState.value = PinUiState.Verified
        } else {
            if (pinManager.isLockedOut()) {
                _uiState.value = PinUiState.LockedOut
                startLockoutTimer()
            } else {
                _uiState.value = PinUiState.Error("Incorrect PIN. ${5 - pinManager.getFailedAttempts()} attempts remaining")
                _enteredPin.value = ""
            }
        }
    }

    private fun onFirstPinEntered() {
        if (_enteredPin.value.length < 4) return
        firstPin = _enteredPin.value
        _enteredPin.value = ""
        _uiState.value = PinUiState.ConfirmNew
    }

    private fun onConfirmPin() {
        if (_enteredPin.value.length < 4) return
        if (_enteredPin.value == firstPin) {
            pinManager.setPin(_enteredPin.value)
            _uiState.value = PinUiState.Verified
        } else {
            _uiState.value = PinUiState.Error("PINs do not match. Try again.")
            _enteredPin.value = ""
            firstPin = null
            _uiState.value = PinUiState.Creating
        }
    }

    fun reset() {
        _enteredPin.value = ""
        firstPin = null
        _uiState.value = if (pinManager.isPinSet()) PinUiState.Entry else PinUiState.Creating
    }

    fun onVerifiedHandled() {
        _uiState.value = PinUiState.Entry
    }

    private fun startLockoutTimer() {
        lockoutUpdateJob?.cancel()
        lockoutUpdateJob = viewModelScope.launch {
            while (pinManager.isLockedOut()) {
                delay(1000)
                _uiState.value = PinUiState.LockedOut
            }
            _enteredPin.value = ""
            _uiState.value = PinUiState.Entry
        }
    }

    override fun onCleared() {
        super.onCleared()
        lockoutUpdateJob?.cancel()
    }
}
