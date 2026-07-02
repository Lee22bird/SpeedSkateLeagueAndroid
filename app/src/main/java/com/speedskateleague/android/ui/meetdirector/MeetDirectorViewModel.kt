package com.speedskateleague.android.ui.meetdirector

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.speedskateleague.android.SslApplication
import com.speedskateleague.android.network.CreateDiscussionRequest
import com.speedskateleague.android.network.CreateReplyRequest
import com.speedskateleague.android.network.DiscussionDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MeetDirectorUiState(
    val discussions: List<DiscussionDto> = emptyList(),
    val selected: DiscussionDto? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

class MeetDirectorViewModel(app: Application) : AndroidViewModel(app) {
    private val apiClient = (app as SslApplication).apiClient

    private val _uiState = MutableStateFlow(MeetDirectorUiState())
    val uiState: StateFlow<MeetDirectorUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = runCatching { apiClient.api.getDiscussions() }
            result.onSuccess { response ->
                _uiState.value = MeetDirectorUiState(discussions = response.discussions, isLoading = false)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Could not load discussions.",
                )
            }
        }
    }

    fun select(discussion: DiscussionDto?) {
        _uiState.value = _uiState.value.copy(selected = discussion)
    }

    fun createDiscussion(title: String, body: String, onDone: () -> Unit) {
        viewModelScope.launch {
            val result = runCatching { apiClient.api.createDiscussion(CreateDiscussionRequest(title.trim(), body.trim())) }
            if (result.isSuccess) { load(); onDone() }
            else _uiState.value = _uiState.value.copy(errorMessage = "Could not post the discussion.")
        }
    }

    fun reply(discussionId: String, body: String) {
        viewModelScope.launch {
            val result = runCatching { apiClient.api.createDiscussionReply(discussionId, CreateReplyRequest(body.trim())) }
            if (result.isSuccess) {
                val updated = runCatching { apiClient.api.getDiscussion(discussionId) }.getOrNull()?.discussion
                if (updated != null) {
                    _uiState.value = _uiState.value.copy(
                        selected = updated,
                        discussions = _uiState.value.discussions.map { if (it.id == updated.id) updated else it },
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(errorMessage = "Could not post the reply.")
            }
        }
    }
}
