package com.anis.child.ui.screen.pairing

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.anis.child.data.ChildData
import com.anis.child.data.PairingRequest
import com.anis.child.data.PreferenceManager
import com.anis.child.data.QrData
import com.anis.child.data.repository.AuthRepository
import com.anis.child.network.ApiResult
import com.google.firebase.messaging.FirebaseMessaging
import com.anis.child.util.resolveDeviceId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import javax.inject.Inject
import kotlin.coroutines.resume

sealed class PairingUiState {
    data object Scanning : PairingUiState()
    data object Loading : PairingUiState()
    data class Success(val childData: ChildData) : PairingUiState()
    data class Error(val message: String, val details: String? = null) : PairingUiState()
}

@HiltViewModel
class PairingViewModel @Inject constructor(
    private val application: Application,
    private val preferenceManager: PreferenceManager,
    private val authRepository: AuthRepository
) : AndroidViewModel(application) {

    private val json = Json { ignoreUnknownKeys = true }

    private val _uiState = MutableStateFlow<PairingUiState>(PairingUiState.Scanning)
    val uiState: StateFlow<PairingUiState> = _uiState.asStateFlow()

    private val _onNavigateToHome = MutableStateFlow(false)
    val onNavigateToHome: StateFlow<Boolean> = _onNavigateToHome.asStateFlow()

    fun getDeviceId(context: Context): String = context.resolveDeviceId()

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
        if (qrData == null || (qrData.action != "PAIR_DEVICE" && qrData.action != "REPAIR_DEVICE")) {
            _uiState.value = PairingUiState.Error("Invalid QR code")
            return
        }

        val context = getApplication<Application>()
        val deviceId = getDeviceId(context)
        val deviceName = getDeviceName()

        viewModelScope.launch {
            _uiState.value = PairingUiState.Loading

            val fcmToken = fetchFcmToken()
            val request = PairingRequest(
                token = qrData.token,
                fcmToken = fcmToken,
                deviceId = deviceId,
                deviceName = deviceName
            )

            val result = when (qrData.action) {
                "PAIR_DEVICE" -> authRepository.pairDevice(request)
                "REPAIR_DEVICE" -> authRepository.repairDevice(request)
                else -> return@launch
            }

            when (result) {
                is ApiResult.Success -> {
                    _uiState.value = PairingUiState.Success(result.data)
                    preferenceManager.needsInitialAppSync = true
                    _onNavigateToHome.value = true
                }
                is ApiResult.Error -> {
                    _uiState.value = PairingUiState.Error(result.message, result.details)
                }
            }
        }
    }

    private suspend fun fetchFcmToken(): String {
        return try {
            val token = suspendCancellableCoroutine<String?> { cont ->
                FirebaseMessaging.getInstance().token
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            cont.resume(task.result)
                        } else {
                            cont.resume(null)
                        }
                    }
            }
            token?.takeIf { it.isNotEmpty() } ?: authRepository.savedFcmToken ?: ""
        } catch (e: Exception) {
            authRepository.savedFcmToken ?: ""
        }
    }

    fun onNavigationComplete() {
        _onNavigateToHome.value = false
    }

    fun resetToScanning() {
        _uiState.value = PairingUiState.Scanning
    }
}
