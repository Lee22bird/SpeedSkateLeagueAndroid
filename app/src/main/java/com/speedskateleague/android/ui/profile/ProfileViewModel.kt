package com.speedskateleague.android.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.speedskateleague.android.SslApplication
import com.speedskateleague.android.network.MeProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: MeProfile? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

/**
 * Android equivalent of ProfileCard.swift's identity section. The richer aggregation
 * (skater-results, official-portfolio, portal scrape) lands in a later phase once those
 * screens exist; for now this surfaces everything /api/me already returns.
 */
class ProfileViewModel(app: Application) : AndroidViewModel(app) {
    private val apiClient = (app as SslApplication).apiClient

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = runCatching { apiClient.api.me() }
            result.onSuccess { profile ->
                _uiState.value = ProfileUiState(profile = profile, isLoading = false)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Unable to load profile.",
                )
            }
        }
    }
}
