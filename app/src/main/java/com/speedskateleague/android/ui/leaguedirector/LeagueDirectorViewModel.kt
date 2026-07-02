package com.speedskateleague.android.ui.leaguedirector

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.speedskateleague.android.SslApplication
import com.speedskateleague.android.network.CoachApprovalRequest
import com.speedskateleague.android.network.CreateDiscussionRequest
import com.speedskateleague.android.network.CreateReplyRequest
import com.speedskateleague.android.network.DiscussionDto
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
    // Directors Forum
    val discussions: List<DiscussionDto> = emptyList(),
    val selectedDiscussion: DiscussionDto? = null,
    val forumLoading: Boolean = false,
    val forumError: String? = null,
)

class LeagueDirectorViewModel(app: Application) : AndroidViewModel(app) {
    private val apiClient = (app as SslApplication).apiClient

    private val _uiState = MutableStateFlow(LeagueDirectorUiState())
    val uiState: StateFlow<LeagueDirectorUiState> = _uiState.asStateFlow()

    init {
        load()
        loadForum()
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

    fun loadForum() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(forumLoading = true, forumError = null)
            runCatching { apiClient.api.getLdDiscussions() }
                .onSuccess { _uiState.value = _uiState.value.copy(discussions = it.discussions, forumLoading = false) }
                .onFailure { _uiState.value = _uiState.value.copy(forumError = it.message, forumLoading = false) }
        }
    }

    fun selectDiscussion(discussion: DiscussionDto?) {
        if (discussion == null) {
            _uiState.value = _uiState.value.copy(selectedDiscussion = null)
            return
        }
        viewModelScope.launch {
            runCatching { apiClient.api.getLdDiscussion(discussion.id) }
                .onSuccess { _uiState.value = _uiState.value.copy(selectedDiscussion = it.discussion) }
                .onFailure { _uiState.value = _uiState.value.copy(selectedDiscussion = discussion) }
        }
    }

    fun createDiscussion(title: String, body: String, onDone: () -> Unit) {
        viewModelScope.launch {
            runCatching { apiClient.api.createLdDiscussion(CreateDiscussionRequest(title, body)) }
                .onSuccess {
                    loadForum()
                    onDone()
                }
        }
    }

    fun replyToDiscussion(discussionId: String, body: String) {
        viewModelScope.launch {
            runCatching { apiClient.api.createLdDiscussionReply(discussionId, CreateReplyRequest(body)) }
                .onSuccess {
                    runCatching { apiClient.api.getLdDiscussion(discussionId) }
                        .onSuccess { resp -> _uiState.value = _uiState.value.copy(selectedDiscussion = resp.discussion) }
                }
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
