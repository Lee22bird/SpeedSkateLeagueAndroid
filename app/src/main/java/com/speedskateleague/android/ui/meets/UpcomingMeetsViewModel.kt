package com.speedskateleague.android.ui.meets

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.speedskateleague.android.SslApplication
import com.speedskateleague.android.network.MeetAttendanceDto
import com.speedskateleague.android.network.MeetDto
import com.speedskateleague.android.network.SslDateParser
import com.speedskateleague.android.network.SslMeet
import com.speedskateleague.android.push.MeetReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class UpcomingMeetsFilter { ALL, MY_TEAM, MY_LEAGUE, FAVORITES }

data class UpcomingMeetsUiState(
    val meets: List<SslMeet> = emptyList(),
    val filter: UpcomingMeetsFilter = UpcomingMeetsFilter.ALL,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
) {
    val filtered: List<SslMeet>
        get() = meets
            .filter {
                when (filter) {
                    UpcomingMeetsFilter.ALL -> true
                    UpcomingMeetsFilter.MY_TEAM -> it.isTeamMeet
                    UpcomingMeetsFilter.MY_LEAGUE -> it.isLeagueMeet
                    UpcomingMeetsFilter.FAVORITES -> it.isFavorite
                }
            }
            .sortedBy { it.dateMillis ?: Long.MAX_VALUE }
}

/** Android equivalent of UpcomingMeetsViewModel in UpcomingMeets.swift. */
class UpcomingMeetsViewModel(app: Application) : AndroidViewModel(app) {
    private val apiClient = (app as SslApplication).apiClient

    private val _uiState = MutableStateFlow(UpcomingMeetsUiState())
    val uiState: StateFlow<UpcomingMeetsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun setFilter(filter: UpcomingMeetsFilter) {
        _uiState.value = _uiState.value.copy(filter = filter)
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val hub = runCatching { apiClient.api.getTeamHub() }.getOrNull()
            val favorites = runCatching { apiClient.api.getFavoriteMeets() }.getOrDefault(emptyList())

            if (hub == null && favorites.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Upcoming meets are not available right now.",
                )
                return@launch
            }

            val attendance = HashMap<String, String>()
            (hub?.meetAttendanceIntents.orEmpty() + hub?.favoriteMeetAttendanceIntents.orEmpty())
                .forEach { intent: MeetAttendanceDto ->
                    val meetId = intent.meetId ?: return@forEach
                    val response = intent.response ?: return@forEach
                    attendance[meetId] = response
                }

            val next = hub?.nextMeets.orEmpty().map { toDomain(it, isFavorite = false, isTeamMeet = false, isLeagueMeet = true, attendance) }
            val schedule = hub?.leagueSchedule.orEmpty().map { toDomain(it, isFavorite = false, isTeamMeet = false, isLeagueMeet = true, attendance) }
            val hubFavorites = (hub?.favoriteMeets.orEmpty() + hub?.coachFavoriteMeets.orEmpty())
                .map { toDomain(it, isFavorite = true, isTeamMeet = it.favoriteScope == "team", isLeagueMeet = false, attendance) }
            val standaloneFavorites = favorites.map { toDomain(it, isFavorite = true, isTeamMeet = it.favoriteScope == "team", isLeagueMeet = false, attendance) }

            val merged = dedupe(next + schedule + hubFavorites + standaloneFavorites)
            _uiState.value = _uiState.value.copy(meets = merged, isLoading = false)
            MeetReminderScheduler.scheduleReminders(getApplication(), merged)
        }
    }

    private fun toDomain(
        dto: MeetDto,
        isFavorite: Boolean,
        isTeamMeet: Boolean,
        isLeagueMeet: Boolean,
        attendance: Map<String, String>,
    ): SslMeet = SslMeet(
        id = dto.identity,
        title = dto.resolvedTitle,
        dateMillis = SslDateParser.parseMillis(dto.resolvedDate),
        league = dto.league,
        location = dto.resolvedLocation,
        venue = dto.venue,
        registrationUrl = dto.registrationUrl,
        ssmUrl = dto.ssmUrl,
        registrationDeadline = dto.registrationDeadline ?: dto.deadline,
        isFavorite = isFavorite || dto.favoriteScope != null,
        isTeamMeet = isTeamMeet,
        isLeagueMeet = isLeagueMeet || dto.league != null,
        attendanceResponse = attendance[dto.identity],
    )

    private fun dedupe(meets: List<SslMeet>): List<SslMeet> {
        val byId = LinkedHashMap<String, SslMeet>()
        for (meet in meets) {
            val existing = byId[meet.id]
            byId[meet.id] = if (existing == null) meet else merge(existing, meet)
        }
        return byId.values.toList()
    }

    private fun merge(existing: SslMeet, incoming: SslMeet): SslMeet = existing.copy(
        title = existing.title.ifEmpty { incoming.title },
        dateMillis = existing.dateMillis ?: incoming.dateMillis,
        league = existing.league ?: incoming.league,
        location = existing.location ?: incoming.location,
        venue = existing.venue ?: incoming.venue,
        registrationUrl = existing.registrationUrl ?: incoming.registrationUrl,
        ssmUrl = existing.ssmUrl ?: incoming.ssmUrl,
        registrationDeadline = existing.registrationDeadline ?: incoming.registrationDeadline,
        isFavorite = existing.isFavorite || incoming.isFavorite,
        isTeamMeet = existing.isTeamMeet || incoming.isTeamMeet,
        isLeagueMeet = existing.isLeagueMeet || incoming.isLeagueMeet,
        attendanceResponse = existing.attendanceResponse ?: incoming.attendanceResponse,
    )
}
