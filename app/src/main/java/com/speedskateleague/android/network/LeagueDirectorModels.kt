package com.speedskateleague.android.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** GET /api/league-director/pending-coaches, GET /api/admin/pending-roles, GET /api/admin/users. */
@Serializable
data class PendingPersonDto(
    val id: String,
    @SerialName("full_name") val fullName: String? = null,
    val gender: String? = null,
    val team: String? = null,
    val league: String? = null,
    val role: String? = null,
    @SerialName("secondary_role") val secondaryRole: String? = null,
    val roles: List<String> = emptyList(),
    @SerialName("pending_role") val pendingRole: String? = null,
    @SerialName("pending_league") val pendingLeague: String? = null,
    @SerialName("age_group") val ageGroup: String? = null,
    @SerialName("team_status") val teamStatus: String? = null,
    @SerialName("approval_status") val approvalStatus: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("profile_photo_url") val profilePhotoUrl: String? = null,
    @SerialName("helmet_number") @Serializable(with = FlexibleIdSerializer::class) val helmetNumber: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
) {
    val resolvedAvatarUrl: String? get() = avatarUrl ?: profilePhotoUrl
}

@Serializable
data class CoachApprovalRequest(val action: String)

/** GET /api/director/league-stats. */
@Serializable
data class LeagueStatsDto(
    val league: String = "",
    val displayName: String = "",
    val teams: Int = 0,
    val skaters: Int = 0,
    val coaches: Int = 0,
)
