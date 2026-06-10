package com.anis.child.ui.screen.settings

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anis.child.data.LogManager
import com.anis.child.data.LogType
import com.anis.child.data.PreferenceManager
import com.anis.child.data.TelemetryManager
import com.anis.child.data.repository.AppsRepository
import com.anis.child.data.repository.LocationRepository
import com.anis.child.network.ApiResult
import com.anis.child.network.ApiService
import com.anis.child.network.safeApiCall
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferenceManager: PreferenceManager,
    val logManager: LogManager,
    private val telemetryManager: TelemetryManager,
    private val locationRepository: LocationRepository,
    private val appsRepository: AppsRepository,
    private val apiService: ApiService,
) : ViewModel() {

    var isMonitoringEnabled by mutableStateOf(preferenceManager.isMonitoringEnabled)
        private set

    var childId by mutableStateOf(preferenceManager.childId)
        private set

    var childName by mutableStateOf(preferenceManager.childName)
        private set

    var isSending by mutableStateOf(false)
        private set

    var isSendingApps by mutableStateOf(false)
        private set

    var isFetchingChild by mutableStateOf(false)
        private set

    init {
        if (preferenceManager.needsInitialAppSync) {
            preferenceManager.needsInitialAppSync = false
            sendInstalledApps()
            fetchChildMe()
        }
    }

    fun onMonitoringChanged(enabled: Boolean) {
        preferenceManager.isMonitoringEnabled = enabled
        isMonitoringEnabled = enabled
        if (!enabled) {
            telemetryManager.stopMonitoring()
            logManager.log("Location monitoring stopped", LogType.INFO)
        }
    }

    @SuppressLint("MissingPermission")
    fun sendCurrentLocation() {
        if (isSending) return
        isSending = true
        viewModelScope.launch {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                val cancellationToken = CancellationTokenSource()
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationToken.token
                ).addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        viewModelScope.launch {
                            when (val result = locationRepository.sendTelemetry(
                                location.latitude, location.longitude
                            )) {
                                is ApiResult.Success -> {
                                    logManager.log(
                                        "Manual: ${String.format("%.6f", location.latitude)}, ${String.format("%.6f", location.longitude)}",
                                        LogType.SUCCESS
                                    )
                                }
                                is ApiResult.Error -> {
                                    logManager.log("Send failed: ${result.message}", LogType.ERROR)
                                }
                            }
                        }
                    } else {
                        logManager.log("Could not get location", LogType.ERROR)
                    }
                }.addOnFailureListener { e ->
                    logManager.log("Location error: ${e.message}", LogType.ERROR)
                }
            } finally {
                isSending = false
            }
        }
    }

    fun sendInstalledApps() {
        if (isSendingApps) return
        isSendingApps = true
        viewModelScope.launch {
            try {
                val apps = appsRepository.getInstalledApps()
                logManager.log("Sending ${apps.size} app(s)...", LogType.INFO)
                when (val result = appsRepository.sendAppsList(apps)) {
                    is ApiResult.Success -> {
                        logManager.log("Apps sent: ${apps.size} app(s)", LogType.SUCCESS)
                    }
                    is ApiResult.Error -> {
                        logManager.log("Apps send failed: ${result.message}", LogType.ERROR)
                    }
                }
            } catch (e: Exception) {
                logManager.log("Apps error: ${e.message}", LogType.ERROR)
            } finally {
                isSendingApps = false
            }
        }
    }

    fun fetchChildMe() {
        if (isFetchingChild) return
        isFetchingChild = true
        viewModelScope.launch {
            try {
                logManager.log("Fetching child data...", LogType.INFO)
                when (val result = safeApiCall { apiService.getChildMe() }) {
                    is ApiResult.Success -> {
                        val data = result.data.data
                        if (data != null) {
                            preferenceManager.childId = data.id
                            preferenceManager.childName = data.firstName
                            childId = data.id
                            childName = data.firstName
                            logManager.log("Child data updated: ${data.firstName}", LogType.SUCCESS)
                        } else {
                            logManager.log("Child data is empty", LogType.ERROR)
                        }
                    }
                    is ApiResult.Error -> {
                        logManager.log("Fetch failed: ${result.message}", LogType.ERROR)
                    }
                }
            } catch (e: Exception) {
                logManager.log("Fetch error: ${e.message}", LogType.ERROR)
            } finally {
                isFetchingChild = false
            }
        }
    }

    fun logout() {
        telemetryManager.stopMonitoring()
        preferenceManager.clear()
        logManager.clear()
        logManager.log("Logged out", LogType.INFO)
    }
}
