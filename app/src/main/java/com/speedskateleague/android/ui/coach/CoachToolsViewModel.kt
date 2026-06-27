package com.speedskateleague.android.ui.coach

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.speedskateleague.android.SslApplication
import com.speedskateleague.android.network.ApprovePendingRequest
import com.speedskateleague.android.network.AnnouncementDto
import com.speedskateleague.android.network.CoachPendingMemberDto
import com.speedskateleague.android.network.CoachPracticeDto
import com.speedskateleague.android.network.CoachRosterMemberDto
import com.speedskateleague.android.network.CreatePracticeRequest
import com.speedskateleague.android.network.PostAnnouncementRequest
import com.speedskateleague.android.network.SetDuesRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class CoachTab { ANNOUNCEMENTS, PRACTICES, ROSTER, DUES, REQUESTS }

data class CoachToolsUiState(
    val team: String = "",
    val league: String = "",
    val roster: List<CoachRosterMemberDto> = emptyList(),
    val pending: List<CoachPendingMemberDto> = emptyList(),
    val practices: List<CoachPracticeDto> = emptyList(),
    val announcements: List<AnnouncementDto> = emptyList(),
    val selectedTab: CoachTab = CoachTab.ANNOUNCEMENTS,
    val isLoading: Boolean = true,
    val message: String? = null,
)

/** Android equivalent of CoachToolsViewModel in CoachTools.swift. */
class CoachToolsViewModel(app: Application) : AndroidViewModel(app) {
    private val apiClient = (app as SslApplication).apiClient

    private val _uiState = MutableStateFlow(CoachToolsUiState())
    val uiState: StateFlow<CoachToolsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun selectTab(tab: CoachTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            val failures = mutableListOf<String>()

            val hub = runCatching { apiClient.api.getTeamHub() }
                .onFailure { failures.add("team hub") }
                .getOrNull()
            val roster = runCatching { apiClient.api.getCoachRoster() }
                .onFailure { failures.add("roster") }
                .getOrDefault(_uiState.value.roster)
            val pending = runCatching { apiClient.api.getCoachPending() }
                .onFailure { failures.add("requests") }
                .getOrDefault(_uiState.value.pending)

            _uiState.value = _uiState.value.copy(
                team = hub?.team ?: _uiState.value.team,
                league = hub?.league ?: _uiState.value.league,
                practices = hub?.practices?.sortedBy { it.sortKey } ?: _uiState.value.practices,
                announcements = hub?.coachAnnouncements?.sortedByDescending { it.createdAt.orEmpty() } ?: _uiState.value.announcements,
                roster = roster.sortedBy { it.fullName.lowercase() },
                pending = pending.sortedBy { it.fullName.lowercase() },
                isLoading = false,
                message = if (failures.isEmpty()) null else "Some coach tools could not refresh: ${failures.joinToString(", ")}.",
            )
        }
    }

    fun postAnnouncement(title: String, body: String) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) {
            _uiState.value = _uiState.value.copy(message = "Announcement title is required.")
            return
        }
        viewModelScope.launch {
            val result = runCatching { apiClient.api.postCoachAnnouncement(PostAnnouncementRequest(cleanTitle, body.trim())) }
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(message = "Announcement posted.")
                load()
            } else {
                _uiState.value = _uiState.value.copy(message = "Announcement could not be posted.")
            }
        }
    }

    fun deleteAnnouncement(announcement: AnnouncementDto) {
        val id = announcement.id ?: return
        viewModelScope.launch {
            val result = runCatching { apiClient.api.deleteCoachAnnouncement(id) }
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    announcements = _uiState.value.announcements.filterNot { it.id == id },
                    message = "Announcement deleted.",
                )
            } else {
                _uiState.value = _uiState.value.copy(message = "Announcement could not be deleted.")
            }
        }
    }

    fun createPractice(title: String, practiceDate: String, time: String, location: String, notes: String) {
        viewModelScope.launch {
            val cleanTitle = title.trim().ifEmpty { "Team Practice" }
            val result = runCatching {
                apiClient.api.createCoachPractice(
                    CreatePracticeRequest(
                        title = cleanTitle,
                        practiceDate = practiceDate,
                        practiceTime = time,
                        startTime = time,
                        location = location.trim(),
                        notes = notes.trim(),
                    ),
                )
            }
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(message = "Practice added.")
                load()
            } else {
                _uiState.value = _uiState.value.copy(message = "Practice could not be added.")
            }
        }
    }

    fun deletePractice(practice: CoachPracticeDto) {
        viewModelScope.launch {
            val result = runCatching { apiClient.api.deleteCoachPractice(practice.id) }
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    practices = _uiState.value.practices.filterNot { it.id == practice.id },
                    message = "Practice deleted.",
                )
            } else {
                _uiState.value = _uiState.value.copy(message = "Practice could not be deleted.")
            }
        }
    }

    fun setDues(member: CoachRosterMemberDto, status: String) {
        viewModelScope.launch {
            val result = runCatching { apiClient.api.setMemberDues(member.id, SetDuesRequest(status)) }
            if (result.isSuccess) {
                load()
            } else {
                _uiState.value = _uiState.value.copy(message = "Dues status could not be saved.")
            }
        }
    }

    fun approve(member: CoachPendingMemberDto) = updatePending(member, "approve")

    fun deny(member: CoachPendingMemberDto) = updatePending(member, "deny")

    private fun updatePending(member: CoachPendingMemberDto, action: String) {
        viewModelScope.launch {
            val result = runCatching { apiClient.api.updatePendingMember(ApprovePendingRequest(member.id, action)) }
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    pending = _uiState.value.pending.filterNot { it.id == member.id },
                    message = if (action == "approve") "Skater approved." else "Request denied.",
                )
                load()
            } else {
                _uiState.value = _uiState.value.copy(message = "Team request could not be updated.")
            }
        }
    }
}
