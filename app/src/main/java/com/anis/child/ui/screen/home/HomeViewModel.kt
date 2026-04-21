package com.anis.child.ui.screen.home

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anis.child.data.LogManager
import com.anis.child.data.LogType
import com.anis.child.data.PreferenceManager
import com.anis.child.data.TelemetryRequest
import com.anis.child.network.ApiService
import com.anis.child.network.NetworkProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    fun sendCurrentLocation(context: Context) {
        if (_isSending.value) return

        _isSending.value = true
        viewModelScope.launch {
            try {
                sendLocationNow(context)
            } finally {
                _isSending.value = false
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun sendLocationNow(context: Context) {
        val logManager = LogManager(context)
        val apiService = NetworkProvider.provideApiService(context)
        val preferenceManager = PreferenceManager(context)

        val accessToken = preferenceManager.accessToken
        if (accessToken.isNullOrEmpty()) {
            logManager.log("No access token", LogType.ERROR)
            return
        }

        val childId = preferenceManager.childId
        if (childId.isNullOrEmpty()) {
            logManager.log("No child ID", LogType.ERROR)
            return
        }

        val fusedLocationClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(context)

        val cancellationToken = CancellationTokenSource()
        val locationTask = fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationToken.token
        )

        locationTask.addOnSuccessListener { location ->
            if (location != null) {
                viewModelScope.launch {
                    try {
                        val response = apiService.sendTelemetry(
                            token = "Bearer $accessToken",
                            request = TelemetryRequest(
                                childId = childId,
                                lat = location.latitude,
                                lng = location.longitude
                            )
                        )
                        if (response.success) {
                            logManager.log(
                                "Manual: ${String.format("%.6f", location.latitude)}, ${String.format("%.6f", location.longitude)}",
                                LogType.SUCCESS
                            )
                        } else {
                            logManager.log(
                                "Send failed: ${response.message ?: "Unknown error"}",
                                LogType.ERROR
                            )
                        }
                    } catch (e: Exception) {
                        logManager.log(
                            "Send error: ${e.message}",
                            LogType.ERROR
                        )
                    }
                }
            } else {
                logManager.log("Could not get location", LogType.ERROR)
            }
        }.addOnFailureListener { e ->
            logManager.log("Location error: ${e.message}", LogType.ERROR)
        }
    }
}