package com.speedskateleague.android.ui.notifications

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.speedskateleague.android.SslApplication
import com.speedskateleague.android.network.NotificationItemDto
import com.speedskateleague.android.network.SslDateParser
import com.speedskateleague.android.network.SslNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotificationsUiState(
    val items: List<SslNotification> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
) {
    val unreadCount: Int get() = items.count { !it.isRead }
}

/** Android equivalent of SSLNotificationCenterViewModel in SSLNotificationCenter.swift. */
class NotificationsViewModel(app: Application) : AndroidViewModel(app) {
    private val apiClient = (app as SslApplication).apiClient

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = runCatching { apiClient.api.getNotifications() }
            result.onSuccess { rows ->
                _uiState.value = NotificationsUiState(items = rows.map(::toDomain), isLoading = false)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Notifications are not available right now.",
                )
            }
        }
    }

    fun markRead(id: String) {
        val current = _uiState.value.items
        val target = current.firstOrNull { it.id == id } ?: return
        if (target.isRead) return
        _uiState.value = _uiState.value.copy(items = current.map { if (it.id == id) it.copy(isRead = true) else it })
        viewModelScope.launch {
            runCatching { apiClient.api.markNotificationRead(id) }
        }
    }

    fun markAllRead() {
        _uiState.value = _uiState.value.copy(items = _uiState.value.items.map { it.copy(isRead = true) })
        viewModelScope.launch {
            runCatching { apiClient.api.markAllNotificationsRead() }
        }
    }

    private fun toDomain(dto: NotificationItemDto): SslNotification = SslNotification(
        id = dto.id,
        title = dto.title ?: "Notification",
        body = dto.body ?: "",
        rawType = dto.rawType,
        sourceId = dto.sourceId,
        actionUrl = dto.actionUrl,
        createdAtMillis = SslDateParser.parseMillis(dto.createdAt),
        isRead = !dto.readAt.isNullOrBlank(),
        priority = dto.priority,
    )
}
