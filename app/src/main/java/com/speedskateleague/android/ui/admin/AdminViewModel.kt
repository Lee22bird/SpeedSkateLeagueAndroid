package com.speedskateleague.android.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.speedskateleague.android.SslApplication
import com.speedskateleague.android.network.AdminProfileDto
import com.speedskateleague.android.network.AdminProfileUpdateRequest
import com.speedskateleague.android.network.AdminStatsDto
import com.speedskateleague.android.network.ApproveRoleRequest
import com.speedskateleague.android.network.PendingPersonDto
import com.speedskateleague.android.network.ScheduleEventDto
import com.speedskateleague.android.network.ScheduleEventRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AdminTab { STATS, USERS, APPROVALS, SCHEDULE }

data class AdminUiState(
    val selectedTab: AdminTab = AdminTab.STATS,
    val stats: AdminStatsDto? = null,
    val searchQuery: String = "",
    val searchResults: List<PendingPersonDto> = emptyList(),
    val editingProfile: AdminProfileDto? = null,
    val pendingRoles: List<PendingPersonDto> = emptyList(),
    val scheduleEvents: List<ScheduleEventDto> = emptyList(),
    val editingScheduleEvent: ScheduleEventDto? = null,
    val isLoading: Boolean = true,
    val message: String? = null,
)

/** Android equivalent of the Admin/Super Admin portal tab — no Swift reference exists. */
class AdminViewModel(app: Application) : AndroidViewModel(app) {
    private val apiClient = (app as SslApplication).apiClient

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        selectTab(AdminTab.STATS)
    }

    fun selectTab(tab: AdminTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
        when (tab) {
            AdminTab.STATS -> loadStats()
            AdminTab.USERS -> Unit
            AdminTab.APPROVALS -> loadPendingRoles()
            AdminTab.SCHEDULE -> loadSchedule()
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val stats = runCatching { apiClient.api.getAdminStats() }.getOrNull()
            _uiState.value = _uiState.value.copy(stats = stats, isLoading = false)
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        if (query.trim().length < 2) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList())
            return
        }
        viewModelScope.launch {
            val results = runCatching { apiClient.api.searchAdminUsers(query.trim()) }.getOrDefault(emptyList())
            _uiState.value = _uiState.value.copy(searchResults = results)
        }
    }

    fun openProfileEditor(personId: String) {
        viewModelScope.launch {
            val profile = runCatching { apiClient.api.getAdminProfile(personId) }.getOrNull()
            _uiState.value = _uiState.value.copy(editingProfile = profile)
        }
    }

    fun closeProfileEditor() {
        _uiState.value = _uiState.value.copy(editingProfile = null)
    }

    fun saveProfile(update: AdminProfileUpdateRequest) {
        val profile = _uiState.value.editingProfile ?: return
        viewModelScope.launch {
            val result = runCatching { apiClient.api.updateAdminProfile(profile.id, update) }
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(editingProfile = null, message = "Profile saved.")
                setSearchQuery(_uiState.value.searchQuery)
            } else {
                _uiState.value = _uiState.value.copy(message = "Could not save profile.")
            }
        }
    }

    private fun loadPendingRoles() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val rows = runCatching { apiClient.api.getPendingRoles() }.getOrDefault(emptyList())
            _uiState.value = _uiState.value.copy(pendingRoles = rows, isLoading = false)
        }
    }

    fun approveRole(person: PendingPersonDto, scope: String) = respondToRole(person, "approve", scope)

    fun denyRole(person: PendingPersonDto, scope: String) = respondToRole(person, "deny", scope)

    private fun respondToRole(person: PendingPersonDto, action: String, scope: String) {
        viewModelScope.launch {
            val result = runCatching { apiClient.api.approveRoleRequest(ApproveRoleRequest(person.id, action, scope)) }
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    pendingRoles = _uiState.value.pendingRoles.filterNot { it.id == person.id },
                    message = if (action == "approve") "Request approved." else "Request denied.",
                )
            } else {
                _uiState.value = _uiState.value.copy(message = "Could not update the request.")
            }
        }
    }

    private fun loadSchedule() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val rows = runCatching { apiClient.api.getDirectorSchedule() }.getOrDefault(emptyList())
            _uiState.value = _uiState.value.copy(
                scheduleEvents = rows.sortedBy { it.eventDate.orEmpty() },
                isLoading = false,
            )
        }
    }

    fun openScheduleEditor(event: ScheduleEventDto?) {
        _uiState.value = _uiState.value.copy(editingScheduleEvent = event ?: ScheduleEventDto())
    }

    fun closeScheduleEditor() {
        _uiState.value = _uiState.value.copy(editingScheduleEvent = null)
    }

    fun saveScheduleEvent(request: ScheduleEventRequest) {
        val existingId = _uiState.value.editingScheduleEvent?.id
        viewModelScope.launch {
            val result = runCatching {
                if (existingId != null) {
                    apiClient.api.updateScheduleEvent(existingId, request)
                } else {
                    apiClient.api.createScheduleEvent(request)
                }
            }
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(editingScheduleEvent = null, message = "Schedule saved.")
                loadSchedule()
            } else {
                _uiState.value = _uiState.value.copy(message = "Could not save schedule event.")
            }
        }
    }

    fun deleteScheduleEvent(event: ScheduleEventDto) {
        val id = event.id ?: return
        viewModelScope.launch {
            val result = runCatching { apiClient.api.deleteScheduleEvent(id) }
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    scheduleEvents = _uiState.value.scheduleEvents.filterNot { it.id == id },
                    message = "Schedule event deleted.",
                )
            } else {
                _uiState.value = _uiState.value.copy(message = "Could not delete schedule event.")
            }
        }
    }
}
