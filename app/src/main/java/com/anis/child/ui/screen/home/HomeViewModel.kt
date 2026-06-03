package com.anis.child.ui.screen.home

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anis.child.data.AppUsageInfo
import com.anis.child.data.LogManager
import com.anis.child.data.LogType
import com.anis.child.data.PreferenceManager
import com.anis.child.data.ScreenTimeManager
import com.anis.child.data.repository.AppsRepository
import com.anis.child.data.repository.LocationRepository
import com.anis.child.network.ApiResult
import com.anis.child.network.ApiService
import com.anis.child.network.safeApiCall
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeScreenData(
    val todayMinutes: Int = 0,
    val dailyLimit: Int = 0,
    val weeklyAverage: Int = 0,
    val topApps: List<AppUsageInfo> = emptyList(),
    val hasUsagePermission: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager,
    private val logManager: LogManager,
    private val locationRepository: LocationRepository,
    private val appsRepository: AppsRepository,
    private val apiService: ApiService,
    private val screenTimeManager: ScreenTimeManager
) : ViewModel() {

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _isSendingApps = MutableStateFlow(false)
    val isSendingApps: StateFlow<Boolean> = _isSendingApps.asStateFlow()

    private val _isFetchingChild = MutableStateFlow(false)
    val isFetchingChild: StateFlow<Boolean> = _isFetchingChild.asStateFlow()

    private val _homeData = MutableStateFlow(HomeScreenData())
    val homeData: StateFlow<HomeScreenData> = _homeData.asStateFlow()

    fun loadHomeData() {
        viewModelScope.launch {
            val todayMinutes = screenTimeManager.getTodayScreenTimeMinutes()
            val weeklyAverage = screenTimeManager.getWeeklyScreenTimeAverage()
            val topApps = screenTimeManager.getAppUsageToday().take(3)
            val config = screenTimeManager.getConfig()
            val hasUsagePermission = screenTimeManager.hasUsageStatsPermission()

            _homeData.value = HomeScreenData(
                todayMinutes = todayMinutes,
                dailyLimit = config.dailyLimitMinutes + config.extraTimeEarnedMinutes,
                weeklyAverage = weeklyAverage,
                topApps = topApps,
                hasUsagePermission = hasUsagePermission
            )
        }
    }

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
                            logManager.log(
                                "Send failed: ${result.message}",
                                LogType.ERROR
                            )
                        }
                    }
                }
            } else {
                logManager.log("Could not get location", LogType.ERROR)
            }
        }.addOnFailureListener { e ->
            logManager.log("Location error: ${e.message}", LogType.ERROR)
        }
    }

    fun sendInstalledApps(context: Context) {
        if (_isSendingApps.value) return
        _isSendingApps.value = true
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
                _isSendingApps.value = false
            }
        }
    }

    fun fetchChildMe(context: Context) {
        if (_isFetchingChild.value) return
        _isFetchingChild.value = true
        viewModelScope.launch {
            try {
                logManager.log("Fetching child data...", LogType.INFO)

                when (val result = safeApiCall { apiService.getChildMe() }) {
                    is ApiResult.Success -> {
                        val data = result.data.data
                        if (data != null) {
                            preferenceManager.childId = data.id
                            preferenceManager.childName = data.name
                            preferenceManager.childAge = data.age
                            preferenceManager.childAvatarUrl = data.avatarUrl
                            preferenceManager.childEmail = data.email
                            logManager.log("Child data updated: ${data.name}", LogType.SUCCESS)
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
                _isFetchingChild.value = false
            }
        }
    }
}
