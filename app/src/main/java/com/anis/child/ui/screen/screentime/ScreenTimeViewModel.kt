package com.anis.child.ui.screen.screentime

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anis.child.data.ScreenTimeManager
import com.anis.child.data.ScreenTimeSummary
import com.anis.child.data.local.AppRestrictionEntity
import com.anis.child.data.local.ScreenTimeConfigEntity
import com.anis.child.di.AppRestrictionController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScreenTimeUiState(
    val summary: ScreenTimeSummary? = null,
    val appUsage: List<com.anis.child.data.AppUsageInfo> = emptyList(),
    val restrictions: List<AppRestrictionEntity> = emptyList(),
    val config: ScreenTimeConfigEntity = ScreenTimeConfigEntity(),
    val hasUsagePermission: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class ScreenTimeViewModel @Inject constructor(
    private val screenTimeManager: ScreenTimeManager,
    private val appRestrictionController: AppRestrictionController
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScreenTimeUiState())
    val uiState: StateFlow<ScreenTimeUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, hasUsagePermission = screenTimeManager.hasUsageStatsPermission())

            val summary = screenTimeManager.getSummary()
            val appUsage = screenTimeManager.getAppUsageToday()
            val config = screenTimeManager.getConfig()
            val restrictions = screenTimeManager.getAllRestrictions()

            _uiState.value = ScreenTimeUiState(
                summary = summary,
                appUsage = appUsage,
                restrictions = restrictions,
                config = config,
                hasUsagePermission = screenTimeManager.hasUsageStatsPermission(),
                isLoading = false
            )
        }
    }

    fun openUsageStatsSettings() {
        screenTimeManager.openUsageStatsSettings()
    }

    fun updateConfig(config: ScreenTimeConfigEntity) {
        viewModelScope.launch {
            screenTimeManager.updateConfig(config)
            refresh()
        }
    }

    fun startRestrictionService() {
        appRestrictionController.start()
    }

    fun stopRestrictionService() {
        appRestrictionController.stop()
    }
}
