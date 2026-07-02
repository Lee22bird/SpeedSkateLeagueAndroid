package com.speedskateleague.android.ui.teamhub

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.speedskateleague.android.SslApplication
import com.speedskateleague.android.network.AnnouncementDto
import com.speedskateleague.android.network.CoachPracticeDto
import com.speedskateleague.android.network.MeetDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TeamHubUiState(
    val teamName: String? = null,
    val league: String? = null,
    val practices: List<CoachPracticeDto> = emptyList(),
    val coachAnnouncements: List<AnnouncementDto> = emptyList(),
    val leagueAnnouncements: List<AnnouncementDto> = emptyList(),
    val nextMeets: List<MeetDto> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

class TeamHubViewModel(app: Application) : AndroidViewModel(app) {
    private val apiClient = (app as SslApplication).apiClient
    private val _uiState = MutableStateFlow(TeamHubUiState())
    val uiState: StateFlow<TeamHubUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            runCatching { apiClient.api.getTeamHub() }
                .onSuccess { dto ->
                    val today = java.time.LocalDate.now().toString()
                    val upcoming = dto.practices
                        .filter { it.practiceDate != null && it.practiceDate >= today }
                        .sortedBy { it.sortKey }
                    val allMeets: List<MeetDto> = dto.nextMeets + dto.leagueSchedule
                    val meets = allMeets.distinctBy { it.identity }.take(2)
                    _uiState.value = TeamHubUiState(
                        teamName = dto.team,
                        league = dto.league,
                        practices = upcoming,
                        coachAnnouncements = dto.coachAnnouncements,
                        leagueAnnouncements = dto.leagueAnnouncements,
                        nextMeets = meets,
                        isLoading = false,
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
                }
        }
    }
}
