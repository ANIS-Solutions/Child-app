package com.anis.child.ui.screen.notifications

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anis.child.data.local.NotificationInterceptDao
import com.anis.child.data.local.NotificationInterceptEntity
import com.anis.child.service.AppNotificationListenerService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationHistoryUiState(
    val notifications: List<NotificationInterceptEntity> = emptyList(),
    val unreadCount: Int = 0,
    val isListenerEnabled: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class NotificationHistoryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationDao: NotificationInterceptDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationHistoryUiState())
    val uiState: StateFlow<NotificationHistoryUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        _uiState.value = _uiState.value.copy(
            isListenerEnabled = AppNotificationListenerService.isNotificationListenerEnabled(context)
        )

        viewModelScope.launch {
            notificationDao.getAll().collect { notifications ->
                _uiState.value = _uiState.value.copy(
                    notifications = notifications,
                    isLoading = false
                )
            }
        }

        viewModelScope.launch {
            notificationDao.getUnreadCount().collect { count ->
                _uiState.value = _uiState.value.copy(unreadCount = count)
            }
        }
    }

    fun markAsRead(id: Long) {
        viewModelScope.launch { notificationDao.markAsRead(id) }
    }

    fun markAllAsRead() {
        viewModelScope.launch { notificationDao.markAllAsRead() }
    }

    fun deleteNotification(id: Long) {
        viewModelScope.launch { notificationDao.delete(id) }
    }

    fun clearAll() {
        viewModelScope.launch { notificationDao.clearAll() }
    }

    fun openNotificationSettings() {
        AppNotificationListenerService.openNotificationSettings(context)
    }

    fun refreshListenerStatus() {
        _uiState.value = _uiState.value.copy(
            isListenerEnabled = AppNotificationListenerService.isNotificationListenerEnabled(context)
        )
    }
}
