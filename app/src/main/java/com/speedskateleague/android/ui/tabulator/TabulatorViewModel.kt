package com.speedskateleague.android.ui.tabulator

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.speedskateleague.android.SslApplication
import com.speedskateleague.android.network.TabulatorAssignmentDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TabulatorUiState(
    val assignments: List<TabulatorAssignmentDto> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

class TabulatorViewModel(app: Application) : AndroidViewModel(app) {
    private val apiClient = (app as SslApplication).apiClient

    private val _uiState = MutableStateFlow(TabulatorUiState())
    val uiState: StateFlow<TabulatorUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = runCatching { apiClient.api.getTabulatorAssignments() }
            result.onSuccess { response ->
                _uiState.value = TabulatorUiState(assignments = response.assignments, isLoading = false)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Could not load assignments.",
                )
            }
        }
    }
}
