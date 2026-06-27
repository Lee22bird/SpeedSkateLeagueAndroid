package com.speedskateleague.android.ui.results

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.speedskateleague.android.SslApplication
import com.speedskateleague.android.network.SslDateParser
import com.speedskateleague.android.network.SslRaceResultLine
import com.speedskateleague.android.network.SslRaceResultMeet
import com.speedskateleague.android.network.SslRaceResultsPayload
import com.speedskateleague.android.network.SslRaceResultsSummary
import com.speedskateleague.android.network.SslTimeTrialGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class RaceResultsUiState(
    val payload: SslRaceResultsPayload? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

/** Android equivalent of SSLRaceResultsViewModel in SSLRaceResults.swift. */
class RaceResultsViewModel(app: Application) : AndroidViewModel(app) {
    private val apiClient = (app as SslApplication).apiClient

    private val _uiState = MutableStateFlow(RaceResultsUiState())
    val uiState: StateFlow<RaceResultsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = runCatching {
                val me = apiClient.api.me()
                val profileId = me.profileId
                require(!profileId.isNullOrBlank()) { "Missing profile id" }
                apiClient.api.getSkaterResults(profileId)
            }
            result.onSuccess { dto ->
                val payload = SslRaceResultsPayload(
                    meets = dto.meets.map { meetDto ->
                        SslRaceResultMeet(
                            id = meetDto.ssmMeetId ?: meetDto.meetName ?: UUID.randomUUID().toString(),
                            meetName = meetDto.meetName?.takeIf { it.isNotBlank() } ?: "Meet",
                            meetDateMillis = SslDateParser.parseMillis(meetDto.meetDate),
                            league = meetDto.league,
                            location = meetDto.location,
                            lines = meetDto.results.map {
                                SslRaceResultLine(
                                    divisionLabel = it.divisionLabel,
                                    distanceLabel = it.distanceLabel,
                                    place = it.place,
                                    points = it.points,
                                )
                            },
                        )
                    },
                    timeTrials = dto.timeTrials.map {
                        SslTimeTrialGroup(
                            id = it.distance ?: UUID.randomUUID().toString(),
                            distance = it.distance ?: "Time Trial",
                            personalBestSeconds = it.personalBest?.timeSeconds,
                            seasonBestSeconds = it.seasonBest?.timeSeconds,
                        )
                    },
                    summary = dto.summary?.let {
                        SslRaceResultsSummary(it.competitions, it.results, it.wins, it.podiums, it.timeTrials)
                    } ?: SslRaceResultsSummary(),
                )
                _uiState.value = RaceResultsUiState(payload = payload, isLoading = false)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Race results are not available right now.",
                )
            }
        }
    }
}
