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
import com.anis.child.network.NetworkProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import retrofit2.HttpException

sealed class PairingUiState {
    data object Scanning : PairingUiState()
    data object Loading : PairingUiState()
    data class Success(val childData: ChildData) : PairingUiState()
    data class Error(val message: String, val details: String? = null) : PairingUiState()
}

class PairingViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = NetworkProvider.provideApiService(application)
    private val preferenceManager = PreferenceManager(application)
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
            deviceId = deviceId,
            deviceName = deviceName
        )

        viewModelScope.launch {
            _uiState.value = PairingUiState.Loading
            try {
                val response = apiService.pairDevice(request)
                
                if (response.success && response.accessToken != null) {
                    preferenceManager.accessToken = response.accessToken
                    preferenceManager.isLoggedIn = true
                    response.data?.let { child ->
                        preferenceManager.childId = child.id
                        preferenceManager.childName = child.name
                        _uiState.value = PairingUiState.Success(child)
                    }
                } else {
                    val errors = response.errors
                    if (!errors.isNullOrEmpty()) {
                        val errorMessages = errors.joinToString("\n") { "${it.field}: ${it.message}" }
                        _uiState.value = PairingUiState.Error(
                            message = response.message ?: "Validation failed",
                            details = errorMessages
                        )
                    } else {
                        _uiState.value = PairingUiState.Error(response.message ?: "Pairing failed")
                    }
                }
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                _uiState.value = PairingUiState.Error(
                    message = "HTTP ${e.code()}: ${e.message()}",
                    details = errorBody
                )
            } catch (e: Exception) {
                _uiState.value = PairingUiState.Error(e.message ?: "Network error")
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