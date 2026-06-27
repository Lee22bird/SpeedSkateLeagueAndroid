package com.speedskateleague.android.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** GET /api/admin/stats. */
@Serializable
data class AdminStatsDto(
    val totalUsers: Int = 0,
    val totalProfiles: Int = 0,
    val accountUsers: Int = 0,
    val childSkaters: Int = 0,
    val skaters: Int = 0,
    val coaches: Int = 0,
    val leagueDirectors: Int = 0,
    val meetDirectors: Int = 0,
    val tabulators: Int = 0,
    val judges: Int = 0,
    val referees: Int = 0,
    val announcers: Int = 0,
    val pendingRoles: Int = 0,
    val activeTeamMembers: Int = 0,
)

/** GET/PUT /api/admin/profile/:id. Mirrors profileSelectFields() in profileService.js. */
@Serializable
data class AdminProfileDto(
    val id: String,
    @SerialName("ssl_skater_id") val sslSkaterId: String? = null,
    @SerialName("full_name") val fullName: String? = null,
    val gender: String? = null,
    val birthdate: String? = null,
    @SerialName("age_group") val ageGroup: String? = null,
    val team: String? = null,
    val league: String? = null,
    val role: String? = null,
    @SerialName("secondary_role") val secondaryRole: String? = null,
    val roles: List<String> = emptyList(),
    @SerialName("pending_role") val pendingRole: String? = null,
    @SerialName("pending_league") val pendingLeague: String? = null,
    @SerialName("approval_status") val approvalStatus: String? = null,
    @SerialName("team_status") val teamStatus: String? = null,
    @Serializable(with = FlexibleIdSerializer::class) @SerialName("helmet_number") val helmetNumber: String? = null,
    val sponsor: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("profile_photo_url") val profilePhotoUrl: String? = null,
    @SerialName("is_child") val isChild: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class AdminProfileUpdateRequest(
    @SerialName("full_name") val fullName: String,
    val gender: String? = null,
    val birthdate: String? = null,
    val team: String? = null,
    val league: String? = null,
    val role: String,
    @SerialName("secondary_role") val secondaryRole: String? = null,
    @SerialName("pending_role") val pendingRole: String? = null,
    @SerialName("pending_league") val pendingLeague: String? = null,
    @SerialName("team_status") val teamStatus: String,
)

@Serializable
data class ApproveRoleRequest(val userId: String, val action: String, val scope: String)

/** /league_schedule_events rows, returned by /api/director/schedule. */
@Serializable
data class ScheduleEventDto(
    val id: String? = null,
    val league: String? = null,
    val title: String? = null,
    @SerialName("event_date") val eventDate: String? = null,
    val location: String? = null,
    val venue: String? = null,
    @SerialName("ssm_url") val ssmUrl: String? = null,
    val published: Boolean = true,
    @SerialName("created_by") val createdBy: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class ScheduleEventRequest(
    val league: String,
    val title: String,
    @SerialName("event_date") val eventDate: String,
    val location: String? = null,
    val venue: String? = null,
    @SerialName("ssm_url") val ssmUrl: String? = null,
    val published: Boolean = true,
)

object AdminRoleOptions {
    val PRIMARY_ROLES = listOf("skater", "coach", "league_director", "meet_director", "tabulator", "referee", "announcer", "admin")
    val SECONDARY_ROLES = listOf("coach", "league_director", "meet_director", "tabulator", "referee", "announcer")
    val PENDING_ROLES = listOf("coach", "league_director", "meet_director", "tabulator", "referee", "announcer")
    val TEAM_STATUSES = listOf("pending", "active", "unplaced", "inactive")
}
