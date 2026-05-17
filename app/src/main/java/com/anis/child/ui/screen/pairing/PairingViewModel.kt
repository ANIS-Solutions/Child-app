package com.anis.child.ui.screen.pairing

import android.app.Application
import android.content.Context
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.anis.child.data.ChildData
import com.anis.child.data.PairingRequest
import com.anis.child.data.PreferenceManager
import com.anis.child.data.QrData
import com.anis.child.data.repository.AuthRepository
import com.anis.child.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

sealed class PairingUiState {
    data object Scanning : PairingUiState()
    data object Loading : PairingUiState()
    data class Success(val childData: ChildData) : PairingUiState()
    data class Error(val message: String, val details: String? = null) : PairingUiState()
}

class PairingViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(PreferenceManager(application))
    private val json = Json { ignoreUnknownKeys = true }

    private val _uiState = MutableStateFlow<PairingUiState>(PairingUiState.Scanning)
    val uiState: StateFlow<PairingUiState> = _uiState.asStateFlow()

    private val _onNavigateToHome = MutableStateFlow(false)
    val onNavigateToHome: StateFlow<Boolean> = _onNavigateToHome.asStateFlow()

    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
    }

    fun getDeviceName(): String {
        return android.os.Build.MODEL
    }

    fun parseQrCode(qrContent: String): QrData? {
        return try {
            json.decodeFromString<QrData>(qrContent)
        } catch (e: Exception) {
            null
        }
    }

    fun processQrCode(qrContent: String) {
        val qrData = parseQrCode(qrContent)
        if (qrData == null || qrData.action != "PAIR_DEVICE") {
            _uiState.value = PairingUiState.Error("Invalid QR code")
            return
        }

        val context = getApplication<Application>()
        val deviceId = getDeviceId(context)
        val deviceName = getDeviceName()

        val request = PairingRequest(
            childId = qrData.childId,
            token = qrData.token,
            fcmToken = authRepository.savedFcmToken ?: "",
            deviceId = deviceId,
            deviceName = deviceName
        )

        viewModelScope.launch {
            _uiState.value = PairingUiState.Loading
            when (val result = authRepository.pairDevice(request)) {
                is ApiResult.Success -> {
                    _uiState.value = PairingUiState.Success(result.data)
                }
                is ApiResult.Error -> {
                    _uiState.value = PairingUiState.Error(result.message, result.details)
                }
            }
        }
    }

    fun onNavigationComplete() {
        _onNavigateToHome.value = false
    }

    fun resetToScanning() {
        _uiState.value = PairingUiState.Scanning
    }
}