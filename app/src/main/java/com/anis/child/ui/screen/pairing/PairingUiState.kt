package com.anis.child.ui.screen.pairing

import com.anis.child.data.ChildData

sealed class PairingUiState {
    data object Scanning : PairingUiState()
    data object Loading : PairingUiState()
    data class Success(val childData: ChildData) : PairingUiState()
    data class Error(val message: String, val details: String? = null) : PairingUiState()
}
