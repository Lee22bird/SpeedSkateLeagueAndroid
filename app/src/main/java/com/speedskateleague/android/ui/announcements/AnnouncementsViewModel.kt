package com.speedskateleague.android.ui.announcements

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.speedskateleague.android.SslApplication
import com.speedskateleague.android.network.AnnouncementDto
import com.speedskateleague.android.network.NotificationItemDto
import com.speedskateleague.android.network.SslAnnouncement
import com.speedskateleague.android.network.SslDateParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

enum class AnnouncementFilter { ALL, LEAGUE, TEAM }

data class AnnouncementsUiState(
    val all: List<SslAnnouncement> = emptyList(),
    val filter: AnnouncementFilter = AnnouncementFilter.ALL,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
) {
    val filtered: List<SslAnnouncement>
        get() = when (filter) {
            AnnouncementFilter.ALL -> all
            AnnouncementFilter.LEAGUE -> all.filter { it.source == "league" }
            AnnouncementFilter.TEAM -> all.filter { it.source == "team" || it.source == "coach" }
        }
}

/** Android equivalent of AnnouncementCenterView's view model in AnnouncementCenter.swift. */
class AnnouncementsViewModel(app: Application) : AndroidViewModel(app) {
    private val apiClient = (app as SslApplication).apiClient

    private val _uiState = MutableStateFlow(AnnouncementsUiState())
    val uiState: StateFlow<AnnouncementsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun setFilter(filter: AnnouncementFilter) {
        _uiState.value = _uiState.value.copy(filter = filter)
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val fromNotifications = runCatching { apiClient.api.getNotifications() }
                .getOrDefault(emptyList())
                .map(::fromNotification)
            val hub = runCatching { apiClient.api.getTeamHub() }.getOrNull()
            val fromHub = (hub?.coachAnnouncements.orEmpty().map { fromHubDto(it, "team") }) +
                (hub?.leagueAnnouncements.orEmpty().map { fromHubDto(it, "league") })

            var merged = dedupe(fromNotifications + fromHub)
            if (merged.isEmpty()) {
                val league = runCatching { apiClient.api.me().league }.getOrNull()?.trim()
                if (!league.isNullOrEmpty()) {
                    val publicRows = runCatching { apiClient.api.getPublicLeagueAnnouncements(league) }
                        .getOrDefault(emptyList())
                        .map { fromHubDto(it, "league") }
                    merged = dedupe(publicRows)
                }
            }

            if (merged.isEmpty() && fromNotifications.isEmpty() && hub == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Announcements are not available right now.",
                )
            } else {
                _uiState.value = _uiState.value.copy(all = merged, isLoading = false)
            }
        }
    }

    fun markRead(announcement: SslAnnouncement) {
        if (!announcement.id.startsWith("notification:")) return
        val rawId = announcement.id.removePrefix("notification:")
        _uiState.value = _uiState.value.copy(
            all = _uiState.value.all.map { if (it.id == announcement.id) it.copy(isRead = true) else it },
        )
        viewModelScope.launch {
            runCatching { apiClient.api.markNotificationRead(rawId) }
        }
    }

    private fun sourceFromRaw(raw: String?): String {
        val value = raw.orEmpty().lowercase()
        return when {
            "team" in value -> "team"
            "coach" in value -> "coach"
            "league" in value -> "league"
            else -> "update"
        }
    }

    private fun fromNotification(dto: NotificationItemDto): SslAnnouncement {
        val source = sourceFromRaw(dto.sourceType ?: dto.rawType)
        return SslAnnouncement(
            id = "notification:${dto.id}",
            title = dto.title ?: "Announcement",
            body = dto.body ?: "",
            source = source,
            priority = dto.metadata?.priority ?: "normal",
            createdAtMillis = SslDateParser.parseMillis(dto.createdAt),
            author = dto.metadata?.author,
            league = dto.metadata?.league,
            team = dto.metadata?.team,
            sourceId = dto.sourceId,
            actionUrl = dto.actionUrl,
            isRead = !dto.readAt.isNullOrBlank(),
        )
    }

    private fun fromHubDto(dto: AnnouncementDto, source: String): SslAnnouncement {
        val rawId = dto.id ?: UUID.randomUUID().toString()
        return SslAnnouncement(
            id = "$source:$rawId",
            title = dto.title ?: "Announcement",
            body = dto.body ?: dto.message ?: "",
            source = source,
            priority = dto.priority ?: "normal",
            createdAtMillis = SslDateParser.parseMillis(dto.createdAt),
            author = dto.authorName ?: dto.createdBy,
            league = dto.league ?: dto.leagueSlug,
            team = dto.teamName,
            sourceId = rawId,
            actionUrl = null,
            isRead = false,
        )
    }

    private fun dedupe(announcements: List<SslAnnouncement>): List<SslAnnouncement> {
        val seen = HashSet<String>()
        val result = mutableListOf<SslAnnouncement>()
        for (item in announcements) {
            val key = item.sourceId ?: item.id
            if (seen.add(key)) result.add(item)
        }
        return result.sortedByDescending { it.createdAtMillis ?: 0L }
    }
}
