package com.anis.child.ui.screen.contentprotection

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anis.child.di.BlockedAppsController
import com.anis.child.data.local.AppRestrictionDao
import com.anis.child.data.local.AppRestrictionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InstalledAppInfo(
    val packageName: String,
    val label: String,
    val icon: android.graphics.drawable.Drawable?,
    val isBlocked: Boolean = false,
    val dailyTimeLimitMinutes: Int = 0,
    val category: String = "General"
)

data class ContentProtectionUiState(
    val installedApps: List<InstalledAppInfo> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ContentProtectionViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appRestrictionDao: AppRestrictionDao,
    private val blockedAppsController: BlockedAppsController
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContentProtectionUiState())
    val uiState: StateFlow<ContentProtectionUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            loadInstalledApps()
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    private suspend fun loadInstalledApps() {
        val pm = context.packageManager
        val allRestrictions = appRestrictionDao.getAllRestrictions().first()
        val restrictionMap = allRestrictions.associateBy { it.packageName }

        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { it.packageName != context.packageName }
            .filter {
                (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 ||
                        it.packageName in restrictionMap
            }
            .sortedBy {
                try { pm.getApplicationLabel(it).toString() } catch (_: Exception) { it.packageName }
            }
            .map { ai ->
                val label = try { pm.getApplicationLabel(ai).toString() } catch (_: Exception) { ai.packageName }
                val existing = restrictionMap[ai.packageName]
                InstalledAppInfo(
                    packageName = ai.packageName,
                    label = label,
                    icon = ai.loadIcon(pm),
                    isBlocked = existing?.isBlocked ?: false,
                    dailyTimeLimitMinutes = existing?.dailyTimeLimitMinutes ?: 0,
                    category = existing?.category ?: "General"
                )
            }

        _uiState.value = _uiState.value.copy(installedApps = apps)
    }

    fun toggleAppBlock(packageName: String, block: Boolean) {
        viewModelScope.launch {
            val current = _uiState.value.installedApps.find { it.packageName == packageName }
            val existing = appRestrictionDao.getRestriction(packageName)
            appRestrictionDao.upsert(
                AppRestrictionEntity(
                    packageName = packageName,
                    label = current?.label ?: "",
                    category = current?.category ?: "General",
                    isBlocked = block,
                    dailyTimeLimitMinutes = existing?.dailyTimeLimitMinutes ?: current?.dailyTimeLimitMinutes ?: 0
                )
            )
            if (block) {
                blockedAppsController.sendUpdateBlockedApps()
            }
            loadInstalledApps()
        }
    }

    fun setDailyTimeLimit(packageName: String, minutes: Int) {
        viewModelScope.launch {
            val current = _uiState.value.installedApps.find { it.packageName == packageName }
            val existing = appRestrictionDao.getRestriction(packageName)
            appRestrictionDao.upsert(
                AppRestrictionEntity(
                    packageName = packageName,
                    label = current?.label ?: "",
                    category = current?.category ?: "General",
                    isBlocked = existing?.isBlocked ?: current?.isBlocked ?: false,
                    dailyTimeLimitMinutes = minutes.coerceAtLeast(0)
                )
            )
            loadInstalledApps()
        }
    }

    fun refresh() {
        loadData()
    }
}
