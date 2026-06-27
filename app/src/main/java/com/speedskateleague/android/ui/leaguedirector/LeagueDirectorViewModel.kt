package com.speedskateleague.android.ui.leaguedirector

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.speedskateleague.android.SslApplication
import com.speedskateleague.android.network.CoachApprovalRequest
import com.speedskateleague.android.network.LeagueStatsDto
import com.speedskateleague.android.network.PendingPersonDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LeagueDirectorUiState(
    val pendingCoaches: List<PendingPersonDto> = emptyList(),
    val stats: LeagueStatsDto? = null,
    val isLoading: Boolean = true,
    val message: String? = null,
)

/** Android equivalent of the League Director panel server-renders at /portal (tab "league"). */
class LeagueDirectorViewModel(app: Application) : AndroidViewModel(app) {
    private val apiClient = (app as SslApplication).apiClient

    private val _uiState = MutableStateFlow(LeagueDirectorUiState())
    val uiState: StateFlow<LeagueDirectorUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            val pending = runCatching { apiClient.api.getPendingCoaches() }.getOrDefault(emptyList())
            val stats = runCatching { apiClient.api.getLeagueStats() }.getOrNull()
            _uiState.value = _uiState.value.copy(
                pendingCoaches = pending,
                stats = stats,
                isLoading = false,
            )
        }
    }

    fun approve(person: PendingPersonDto) = respond(person, "approve")

    fun deny(person: PendingPersonDto) = respond(person, "deny")

    private fun respond(person: PendingPersonDto, action: String) {
        viewModelScope.launch {
            val result = runCatching { apiClient.api.approveCoachRequest(person.id, CoachApprovalRequest(action)) }
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    pendingCoaches = _uiState.value.pendingCoaches.filterNot { it.id == person.id },
                    message = if (action == "approve") "Coach approved." else "Request denied.",
                )
            } else {
                _uiState.value = _uiState.value.copy(message = "Could not update the request.")
            }
        }
    }
}
