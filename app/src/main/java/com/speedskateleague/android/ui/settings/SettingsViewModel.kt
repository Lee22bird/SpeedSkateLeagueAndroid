package com.speedskateleague.android.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.speedskateleague.android.SslApplication
import com.speedskateleague.android.network.NotificationPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val preferences: NotificationPreferences = NotificationPreferences(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val statusMessage: String? = null,
)

/** Android equivalent of SettingsView.swift's notification preferences screen. */
class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val apiClient = (app as SslApplication).apiClient

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val result = runCatching { apiClient.api.getNotificationPreferences() }
            result.onSuccess { prefs ->
                _uiState.value = SettingsUiState(preferences = prefs, isLoading = false)
            }.onFailure {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun update(transform: (NotificationPreferences) -> NotificationPreferences) {
        _uiState.value = _uiState.value.copy(preferences = transform(_uiState.value.preferences), statusMessage = null)
    }

    fun save() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, statusMessage = null)
            val result = runCatching { apiClient.api.saveNotificationPreferences(_uiState.value.preferences) }
            result.onSuccess {
                _uiState.value = _uiState.value.copy(isSaving = false, statusMessage = "Saved.")
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    statusMessage = error.message ?: "Unable to save preferences.",
                )
            }
        }
    }
}
