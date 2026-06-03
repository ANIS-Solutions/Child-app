package com.anis.child.ui.screen.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anis.child.data.PreferenceManager
import com.anis.child.data.TelemetryManager
import com.anis.child.data.local.LocationTelemetryDao
import com.anis.child.data.local.LocationTelemetryEntity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LocationHistoryUiState(
    val locations: List<LocationTelemetryEntity> = emptyList(),
    val currentLatitude: Double? = null,
    val currentLongitude: Double? = null,
    val isMonitoringEnabled: Boolean = false,
    val isGettingCurrentLocation: Boolean = false,
    val totalSharedCount: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class LocationHistoryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locationDao: LocationTelemetryDao,
    private val preferenceManager: PreferenceManager,
    private val telemetryManager: TelemetryManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocationHistoryUiState())
    val uiState: StateFlow<LocationHistoryUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        _uiState.value = _uiState.value.copy(
            isMonitoringEnabled = preferenceManager.isMonitoringEnabled
        )

        viewModelScope.launch {
            locationDao.getAllLocations().collect { locations ->
                val sharedCount = locations.count { it.isSent }
                _uiState.value = _uiState.value.copy(
                    locations = locations,
                    totalSharedCount = sharedCount,
                    isLoading = false
                )
            }
        }
    }

    fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) return

        _uiState.value = _uiState.value.copy(isGettingCurrentLocation = true)

        viewModelScope.launch {
            try {
                val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                val location = fusedClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    null
                )
                location.addOnSuccessListener { loc ->
                    if (loc != null) {
                        _uiState.value = _uiState.value.copy(
                            currentLatitude = loc.latitude,
                            currentLongitude = loc.longitude,
                            isGettingCurrentLocation = false
                        )
                        viewModelScope.launch {
                            telemetryManager.saveLocation(loc.latitude, loc.longitude)
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(isGettingCurrentLocation = false)
                    }
                }
                location.addOnFailureListener {
                    _uiState.value = _uiState.value.copy(isGettingCurrentLocation = false)
                }
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isGettingCurrentLocation = false)
            }
        }
    }

    fun toggleMonitoring(enabled: Boolean) {
        preferenceManager.isMonitoringEnabled = enabled
        _uiState.value = _uiState.value.copy(isMonitoringEnabled = enabled)
        if (enabled) {
            telemetryManager.startMonitoring()
        } else {
            telemetryManager.stopMonitoring()
        }
    }

    fun deleteLocation(id: Long) {
        viewModelScope.launch {
            locationDao.delete(id)
        }
    }

    fun clearSentLocations() {
        viewModelScope.launch {
            locationDao.deleteAllSent()
        }
    }
}
