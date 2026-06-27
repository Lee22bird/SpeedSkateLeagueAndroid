package com.speedskateleague.android.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.speedskateleague.android.SslApplication
import com.speedskateleague.android.network.MeProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val profile: MeProfile? = null,
    val isLoading: Boolean = true,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
)

/** Android equivalent of HomeDashboardService + its ViewModel in HomeDashboard.swift. */
class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val apiClient = (app as SslApplication).apiClient

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            var lastError: Throwable? = null
            repeat(3) { attempt ->
                val result = runCatching { apiClient.api.me() }
                result.onSuccess { profile ->
                    _uiState.value = HomeUiState(profile = profile, isLoading = false, isOffline = false)
                    return@launch
                }
                lastError = result.exceptionOrNull()
                if (attempt < 2) kotlinx.coroutines.delay(500L * (attempt + 1))
            }
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isOffline = true,
                errorMessage = lastError?.message ?: "Unable to load dashboard.",
            )
        }
    }
}
