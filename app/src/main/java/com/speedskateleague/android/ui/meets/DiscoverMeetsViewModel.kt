package com.speedskateleague.android.ui.meets

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.speedskateleague.android.SslApplication
import com.speedskateleague.android.network.FavoriteMeetRequest
import com.speedskateleague.android.network.MeetDto
import com.speedskateleague.android.network.SslDateParser
import com.speedskateleague.android.network.SslMeet
import com.speedskateleague.android.network.UnfavoriteMeetRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

enum class DiscoverMeetsFilter { ALL, UPCOMING, NATIONAL, REGIONAL, LEAGUE, FAVORITES }

data class DiscoverMeetsUiState(
    val meets: List<SslMeet> = emptyList(),
    val filter: DiscoverMeetsFilter = DiscoverMeetsFilter.ALL,
    val searchText: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
) {
    val filtered: List<SslMeet>
        get() = meets
            .filter { matches(it, searchText) }
            .filter {
                when (filter) {
                    DiscoverMeetsFilter.ALL -> true
                    DiscoverMeetsFilter.UPCOMING -> isUpcoming(it)
                    DiscoverMeetsFilter.NATIONAL -> it.category == "national"
                    DiscoverMeetsFilter.REGIONAL -> it.category == "regional"
                    DiscoverMeetsFilter.LEAGUE -> it.category == "league"
                    DiscoverMeetsFilter.FAVORITES -> it.isFavorite
                }
            }
            .sortedBy { it.dateMillis ?: Long.MAX_VALUE }

    private fun isUpcoming(meet: SslMeet): Boolean {
        val date = meet.dateMillis ?: return true
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }.timeInMillis
        return date >= yesterday
    }

    private fun matches(meet: SslMeet, query: String): Boolean {
        val clean = query.trim().lowercase()
        if (clean.isEmpty()) return true
        return listOfNotNull(meet.title, meet.league, meet.location, meet.venue, meet.category, meet.registrationStatus)
            .any { it.lowercase().contains(clean) }
    }
}

/** Android equivalent of DiscoverMeetsViewModel in UpcomingMeets.swift. */
class DiscoverMeetsViewModel(app: Application) : AndroidViewModel(app) {
    private val apiClient = (app as SslApplication).apiClient

    private val _uiState = MutableStateFlow(DiscoverMeetsUiState())
    val uiState: StateFlow<DiscoverMeetsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun setFilter(filter: DiscoverMeetsFilter) {
        _uiState.value = _uiState.value.copy(filter = filter)
    }

    fun setSearchText(text: String) {
        _uiState.value = _uiState.value.copy(searchText = text)
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = runCatching { apiClient.api.getDiscoverMeets() }
            result.onSuccess { response ->
                _uiState.value = _uiState.value.copy(meets = response.meets.map(::toDomain), isLoading = false)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Discover meets is not available right now.",
                )
            }
        }
    }

    fun toggleFavorite(meet: SslMeet) {
        val wasFavorite = meet.isFavorite
        _uiState.value = _uiState.value.copy(
            meets = _uiState.value.meets.map { if (it.id == meet.id) it.copy(isFavorite = !wasFavorite) else it },
        )
        viewModelScope.launch {
            val result = if (wasFavorite) {
                runCatching { apiClient.api.removeFavoriteMeet(UnfavoriteMeetRequest(meetId = meet.id)) }
            } else {
                runCatching {
                    apiClient.api.addFavoriteMeet(
                        FavoriteMeetRequest(
                            meetId = meet.id,
                            meetTitle = meet.title,
                            meetLocation = meet.displayLocation,
                            ssmUrl = meet.ssmUrl ?: meet.registrationUrl ?: "",
                            league = meet.league ?: "",
                            sourceType = meet.category ?: "league",
                        ),
                    )
                }
            }
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    meets = _uiState.value.meets.map { if (it.id == meet.id) it.copy(isFavorite = wasFavorite) else it },
                )
            }
        }
    }

    private fun toDomain(dto: MeetDto): SslMeet = SslMeet(
        id = dto.identity,
        title = dto.resolvedTitle,
        dateMillis = SslDateParser.parseMillis(dto.resolvedDate),
        league = dto.league,
        location = dto.resolvedLocation,
        venue = dto.venue,
        registrationUrl = dto.registrationUrl,
        ssmUrl = dto.ssmUrl,
        registrationStatus = dto.registrationStatus,
        category = dto.category ?: dto.sourceType,
        isFavorite = dto.isFavoriteFlag == true,
    )
}
