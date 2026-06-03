package com.anis.child.ui.screen.contentprotection

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anis.child.content.AppBlockAccessibilityService
import com.anis.child.data.ContentFilterManager
import com.anis.child.data.local.AppRestrictionDao
import com.anis.child.data.local.AppRestrictionEntity
import com.anis.child.data.local.ContentFilterRuleDao
import com.anis.child.data.local.ContentFilterRuleEntity
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
    val category: String = "General"
)

data class ContentProtectionUiState(
    val installedApps: List<InstalledAppInfo> = emptyList(),
    val filterRules: List<ContentFilterRuleEntity> = emptyList(),
    val isLoading: Boolean = true,
    val isAccessibilityEnabled: Boolean = false,
    val newRulePattern: String = "",
    val newRuleType: String = "keyword"
)

@HiltViewModel
class ContentProtectionViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appRestrictionDao: AppRestrictionDao,
    private val contentFilterRuleDao: ContentFilterRuleDao,
    private val contentFilterManager: ContentFilterManager
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
            loadFilterRules()
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    private suspend fun loadInstalledApps() {
        val pm = context.packageManager
        val blockedApps = appRestrictionDao.getBlockedApps()
        val blockedMap = blockedApps.associateBy { it.packageName }

        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { it.packageName != context.packageName }
            .filter {
                (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 ||
                        it.packageName in blockedMap
            }
            .sortedBy {
                try { pm.getApplicationLabel(it).toString() } catch (_: Exception) { it.packageName }
            }
            .map { ai ->
                val label = try { pm.getApplicationLabel(ai).toString() } catch (_: Exception) { ai.packageName }
                val existing = blockedMap[ai.packageName]
                InstalledAppInfo(
                    packageName = ai.packageName,
                    label = label,
                    icon = ai.loadIcon(pm),
                    isBlocked = existing?.isBlocked ?: false,
                    category = existing?.category ?: "General"
                )
            }

        _uiState.value = _uiState.value.copy(installedApps = apps)
    }

    private suspend fun loadFilterRules() {
        contentFilterRuleDao.getAllRules().collect { rules ->
            _uiState.value = _uiState.value.copy(filterRules = rules)
        }
    }

    fun toggleAppBlock(packageName: String, block: Boolean) {
        viewModelScope.launch {
            appRestrictionDao.upsert(
                AppRestrictionEntity(
                    packageName = packageName,
                    label = _uiState.value.installedApps
                        .find { it.packageName == packageName }?.label ?: "",
                    isBlocked = block
                )
            )
            if (block) {
                AppBlockAccessibilityService.sendUpdateBlockedApps(context)
            }
            loadInstalledApps()
        }
    }

    fun onNewRulePatternChanged(pattern: String) {
        _uiState.value = _uiState.value.copy(newRulePattern = pattern)
    }

    fun onNewRuleTypeChanged(type: String) {
        _uiState.value = _uiState.value.copy(newRuleType = type)
    }

    fun addFilterRule() {
        val state = _uiState.value
        if (state.newRulePattern.isBlank()) return
        viewModelScope.launch {
            contentFilterManager.addRule(state.newRulePattern, state.newRuleType)
            _uiState.value = _uiState.value.copy(newRulePattern = "")
        }
    }

    fun deleteFilterRule(rule: ContentFilterRuleEntity) {
        viewModelScope.launch {
            contentFilterManager.deleteRule(rule)
        }
    }

    fun toggleFilterRule(id: Long, enabled: Boolean) {
        viewModelScope.launch {
            contentFilterManager.toggleRule(id, enabled)
        }
    }

    fun addDefaultFilterRules() {
        viewModelScope.launch {
            val existing = contentFilterRuleDao.getAllRules().first()
            if (existing.isNotEmpty()) return@launch
            ContentFilterManager.DEFAULT_BLOCKED_KEYWORDS.forEach { pattern ->
                contentFilterManager.addRule(pattern, "keyword")
            }
            ContentFilterManager.DEFAULT_BLOCKED_URLS.forEach { pattern ->
                contentFilterManager.addRule(pattern, "url")
            }
        }
    }

}
