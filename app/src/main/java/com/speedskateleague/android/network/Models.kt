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
    val title: String? = null,
    val body: String? = null,
    @SerialName("type") val rawType: String? = null,
    @SerialName("source_type") val sourceType: String? = null,
    @SerialName("source_id") val sourceId: String? = null,
    @SerialName("action_url") val actionUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("read_at") val readAt: String? = null,
    val priority: String? = null,
    val metadata: AnnouncementMetadataDto? = null,
)

@Serializable
data class AnnouncementMetadataDto(
    val team: String? = null,
    val league: String? = null,
    val author: String? = null,
    val priority: String? = null,
)

/** Generic announcement DTO shared by /api/team-hub and /api/public/league-announcements. */
@Serializable
data class AnnouncementDto(
    val id: String? = null,
    val title: String? = null,
    val body: String? = null,
    val message: String? = null,
    val priority: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("created_by") val createdBy: String? = null,
    @SerialName("author_name") val authorName: String? = null,
    val league: String? = null,
    @SerialName("league_slug") val leagueSlug: String? = null,
    @SerialName("team_name") val teamName: String? = null,
)

/** /api/team-hub. Note: this endpoint's JSON uses camelCase keys, unlike the rest of the API. */
@Serializable
data class TeamHubDto(
    val team: String? = null,
    val league: String? = null,
    val coachAnnouncements: List<AnnouncementDto> = emptyList(),
    val leagueAnnouncements: List<AnnouncementDto> = emptyList(),
    val nextMeets: List<MeetDto> = emptyList(),
    val leagueSchedule: List<MeetDto> = emptyList(),
    val favoriteMeets: List<MeetDto> = emptyList(),
    val coachFavoriteMeets: List<MeetDto> = emptyList(),
    val meetAttendanceIntents: List<MeetAttendanceDto> = emptyList(),
    val favoriteMeetAttendanceIntents: List<MeetAttendanceDto> = emptyList(),
    val practices: List<CoachPracticeDto> = emptyList(),
    val teamSettings: CoachTeamSettingsDto? = null,
)

@Serializable
data class CoachTeamSettingsDto(
    @SerialName("team_name") val teamName: String? = null,
    @SerialName("league_slug") val leagueSlug: String? = null,
    @SerialName("dues_enabled") val duesEnabled: Boolean = false,
    @SerialName("dues_amount") val duesAmount: Double? = null,
    @SerialName("dues_reset_day") val duesResetDay: Int? = null,
)

@Serializable
data class CoachPracticeDto(
    @Serializable(with = FlexibleIdSerializer::class) val id: String,
    val title: String? = null,
    @SerialName("practice_date") val practiceDate: String? = null,
    @SerialName("practice_time") val practiceTime: String? = null,
    @SerialName("start_time") val startTime: String? = null,
    val location: String? = null,
    val notes: String? = null,
) {
    val sortKey: String get() = "${practiceDate.orEmpty()} ${startTime ?: practiceTime.orEmpty()}"
}

@Serializable
data class CoachAnnouncementDto(
    @Serializable(with = FlexibleIdSerializer::class) val id: String,
    val title: String? = null,
    val body: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("author_name") val authorName: String? = null,
)

@Serializable
data class CoachRosterMemberDto(
    @Serializable(with = FlexibleIdSerializer::class) val id: String,
    @SerialName("ssl_skater_id") val sslSkaterId: String? = null,
    @SerialName("full_name") val fullName: String,
    @SerialName("age_group") val ageGroup: String? = null,
    @SerialName("helmet_number") @Serializable(with = FlexibleIdSerializer::class) val helmetNumber: String? = null,
    @SerialName("dues_status") val duesStatusRaw: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("profile_photo_url") val profilePhotoUrl: String? = null,
) {
    val duesStatus: String get() = if ((duesStatusRaw ?: "owes").lowercase() == "paid") "paid" else "owes"
    val resolvedAvatarUrl: String? get() = avatarUrl ?: profilePhotoUrl
}

@Serializable
data class CoachPendingMemberDto(
    @Serializable(with = FlexibleIdSerializer::class) val id: String,
    @SerialName("ssl_skater_id") val sslSkaterId: String? = null,
    @SerialName("full_name") val fullName: String,
    @SerialName("age_group") val ageGroup: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("profile_photo_url") val profilePhotoUrl: String? = null,
) {
    val resolvedAvatarUrl: String? get() = avatarUrl ?: profilePhotoUrl
}

@Serializable
data class PostAnnouncementRequest(val title: String, val body: String, val published: Boolean = true)

@Serializable
data class CreatePracticeRequest(
    val title: String,
    @SerialName("practice_date") val practiceDate: String,
    @SerialName("practice_time") val practiceTime: String,
    @SerialName("start_time") val startTime: String,
    val location: String,
    val notes: String,
)

@Serializable
data class SetDuesRequest(@SerialName("dues_status") val duesStatus: String)

@Serializable
data class ApprovePendingRequest(val skaterId: String, val action: String)

@Serializable
data class MeetDto(
    val id: String? = null,
    @SerialName("meet_id") val meetId: String? = null,
    val title: String? = null,
    val name: String? = null,
    @SerialName("meet_title") val meetTitle: String? = null,
    @SerialName("event_date") val eventDate: String? = null,
    @SerialName("meet_date") val meetDate: String? = null,
    val date: String? = null,
    val league: String? = null,
    val location: String? = null,
    val venue: String? = null,
    @SerialName("meet_location") val meetLocation: String? = null,
    @SerialName("registration_url") val registrationUrl: String? = null,
    @SerialName("ssm_url") val ssmUrl: String? = null,
    @SerialName("registration_deadline") val registrationDeadline: String? = null,
    val deadline: String? = null,
    @SerialName("favorite_scope") val favoriteScope: String? = null,
    @SerialName("source_type") val sourceType: String? = null,
    @SerialName("registration_status") val registrationStatus: String? = null,
    @SerialName("is_favorite") val isFavoriteFlag: Boolean? = null,
    val category: String? = null,
) {
    val identity: String get() = meetId ?: id ?: ssmUrl ?: registrationUrl ?: title ?: name ?: meetTitle ?: "meet"
    val resolvedTitle: String get() = title ?: name ?: meetTitle ?: "Meet"
    val resolvedDate: String? get() = eventDate ?: meetDate ?: date
    val resolvedLocation: String? get() = location ?: meetLocation
}

@Serializable
data class MeetAttendanceDto(
    @SerialName("meet_id") val meetId: String? = null,
    val response: String? = null,
)

@Serializable
data class DiscoverMeetsResponse(val meets: List<MeetDto> = emptyList())

@Serializable
data class FavoriteMeetRequest(
    val scope: String = "user",
    @SerialName("meet_id") val meetId: String,
    @SerialName("meet_title") val meetTitle: String = "",
    @SerialName("meet_date") val meetDate: String = "",
    @SerialName("meet_location") val meetLocation: String = "",
    @SerialName("ssm_url") val ssmUrl: String = "",
    val league: String = "",
    @SerialName("source_type") val sourceType: String = "league",
)

@Serializable
data class UnfavoriteMeetRequest(
    val scope: String = "user",
    @SerialName("meet_id") val meetId: String,
)

/** Domain model for an upcoming meet, equivalent to UpcomingMeet/DiscoverMeet in UpcomingMeets.swift. */
data class SslMeet(
    val id: String,
    val title: String,
    val dateMillis: Long?,
    val league: String?,
    val location: String?,
    val venue: String?,
    val registrationUrl: String?,
    val ssmUrl: String?,
    val registrationDeadline: String? = null,
    val registrationStatus: String? = null,
    val category: String? = null,
    val isFavorite: Boolean = false,
    val isTeamMeet: Boolean = false,
    val isLeagueMeet: Boolean = false,
    val attendanceResponse: String? = null,
) {
    val displayLocation: String
        get() = listOfNotNull(location, venue).filter { it.isNotBlank() }.distinct().joinToString(" • ")

    val openUrl: String?
        get() = ssmUrl?.takeIf { it.isNotBlank() } ?: registrationUrl?.takeIf { it.isNotBlank() }
}

/** Domain model for an announcement, equivalent to Announcement in AnnouncementCenter.swift. */
data class SslAnnouncement(
    val id: String,
    val title: String,
    val body: String,
    val source: String,
    val priority: String,
    val createdAtMillis: Long?,
    val author: String?,
    val league: String?,
    val team: String?,
    val sourceId: String?,
    val actionUrl: String?,
    val isRead: Boolean,
)

/** Domain model for a notification, equivalent to SSLNotificationItem in SSLNotificationCenter.swift. */
data class SslNotification(
    val id: String,
    val title: String,
    val body: String,
    val rawType: String?,
    val sourceId: String?,
    val actionUrl: String?,
    val createdAtMillis: Long?,
    val isRead: Boolean,
    val priority: String?,
)

@Serializable
data class SkaterResultsDto(
    val meets: List<ResultMeetDto> = emptyList(),
    @SerialName("time_trials") val timeTrials: List<TimeTrialGroupDto> = emptyList(),
    val summary: ResultsSummaryDto? = null,
)

@Serializable
data class ResultMeetDto(
    @SerialName("ssm_meet_id") val ssmMeetId: String? = null,
    @SerialName("meet_name") val meetName: String? = null,
    @SerialName("meet_date") val meetDate: String? = null,
    val league: String? = null,
    val location: String? = null,
    val results: List<ResultLineDto> = emptyList(),
)

@Serializable
data class ResultLineDto(
    @SerialName("division_label") val divisionLabel: String? = null,
    @SerialName("distance_label") val distanceLabel: String? = null,
    val place: Int? = null,
    val points: Int? = null,
)

@Serializable
data class TimeTrialGroupDto(
    val distance: String? = null,
    @SerialName("personal_best") val personalBest: TimeRowDto? = null,
    @SerialName("season_best") val seasonBest: TimeRowDto? = null,
)

@Serializable
data class TimeRowDto(@SerialName("time_seconds") val timeSeconds: Double? = null)

@Serializable
data class ResultsSummaryDto(
    val competitions: Int = 0,
    val results: Int = 0,
    val wins: Int = 0,
    val podiums: Int = 0,
    @SerialName("time_trials") val timeTrials: Int = 0,
)

/** Domain models equivalent to SSLRaceResultLine/Meet/SSLTimeTrialGroup/Summary in SSLRaceResults.swift. */
data class SslRaceResultLine(
    val divisionLabel: String?,
    val distanceLabel: String?,
    val place: Int?,
    val points: Int?,
) {
    val label: String get() = listOfNotNull(divisionLabel, distanceLabel).joinToString(" · ")
    val placeLabel: String
        get() = when (place) {
            null -> "—"
            1 -> "🥇 1st"
            2 -> "🥈 2nd"
            3 -> "🥉 3rd"
            else -> "${place}th"
        }
}

data class SslRaceResultMeet(
    val id: String,
    val meetName: String,
    val meetDateMillis: Long?,
    val league: String?,
    val location: String?,
    val lines: List<SslRaceResultLine>,
)

data class SslTimeTrialGroup(
    val id: String,
    val distance: String,
    val personalBestSeconds: Double?,
    val seasonBestSeconds: Double?,
)

data class SslRaceResultsSummary(
    val competitions: Int = 0,
    val results: Int = 0,
    val wins: Int = 0,
    val podiums: Int = 0,
    val timeTrials: Int = 0,
)

data class SslRaceResultsPayload(
    val meets: List<SslRaceResultMeet>,
    val timeTrials: List<SslTimeTrialGroup>,
    val summary: SslRaceResultsSummary,
)
