package com.speedskateleague.android.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class MobileAuthSession(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("expires_at") val expiresAt: Long? = null,
    @SerialName("token_type") val tokenType: String? = "Bearer",
)

@Serializable
data class MobileAuthResponse(val session: MobileAuthSession)

@Serializable
data class RefreshRequest(@SerialName("refresh_token") val refreshToken: String)

@Serializable
data class MeStats(
    @SerialName("unread_notifications_count") val unreadNotifications: Int = 0,
    @SerialName("upcoming_meets_count") val upcomingMeets: Int = 0,
    @SerialName("race_results_count") val raceResults: Int = 0,
    @SerialName("time_trial_results_count") val timeTrialResults: Int = 0,
    @SerialName("official_assignments_count") val officialAssignments: Int = 0,
)

@Serializable
data class MeProfile(
    @SerialName("profile_id") val profileId: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("full_name") val fullName: String? = null,
    val email: String? = null,
    @SerialName("ssl_skater_id") val sslSkaterId: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val team: String? = null,
    val league: String? = null,
    val roles: List<String> = emptyList(),
    @SerialName("helmet_number") val helmetNumber: String? = null,
    val sponsor: String? = null,
    val stats: MeStats = MeStats(),
)

@Serializable
data class NotificationPreferences(
    @SerialName("team_announcements") val teamAnnouncements: Boolean = true,
    @SerialName("league_announcements") val leagueAnnouncements: Boolean = true,
    @SerialName("meet_reminders") val meetReminders: Boolean = true,
    @SerialName("coach_updates") val coachUpdates: Boolean = true,
    @SerialName("push_notifications") val pushNotifications: Boolean = true,
    @SerialName("email_notifications") val emailNotifications: Boolean = true,
)

@Serializable
data class NotificationItemDto(
    val id: String,
    val title: String,
    val body: String? = null,
    @SerialName("type") val rawType: String? = null,
    @SerialName("source_id") val sourceId: String? = null,
    @SerialName("action_url") val actionUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("read_at") val readAt: String? = null,
    val priority: String? = null,
)

@Serializable
data class NotificationsResponse(val notifications: List<NotificationItemDto> = emptyList())
