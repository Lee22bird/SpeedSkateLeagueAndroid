package com.speedskateleague.android.network

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface SslApiService {
    @POST("/api/mobile/auth/login")
    suspend fun login(@Body body: LoginRequest): MobileAuthResponse

    @POST("/api/mobile/auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): MobileAuthResponse

    @GET("/api/me")
    suspend fun me(): MeProfile

    @GET("/api/notification-preferences")
    suspend fun getNotificationPreferences(): NotificationPreferences

    @POST("/api/notification-preferences")
    suspend fun saveNotificationPreferences(@Body body: NotificationPreferences): NotificationPreferences

    @GET("/api/notifications")
    suspend fun getNotifications(): List<NotificationItemDto>

    @POST("/api/notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: String)

    @POST("/api/notifications/read-all")
    suspend fun markAllNotificationsRead()

    @GET("/api/team-hub")
    suspend fun getTeamHub(): TeamHubDto

    @GET("/api/public/league-announcements")
    suspend fun getPublicLeagueAnnouncements(@Query("league") league: String): List<AnnouncementDto>

    @GET("/api/favorite-meets")
    suspend fun getFavoriteMeets(): List<MeetDto>

    @POST("/api/favorite-meets")
    suspend fun addFavoriteMeet(@Body body: FavoriteMeetRequest)

    @HTTP(method = "DELETE", path = "/api/favorite-meets", hasBody = true)
    suspend fun removeFavoriteMeet(@Body body: UnfavoriteMeetRequest)

    @GET("/api/discover-meets")
    suspend fun getDiscoverMeets(): DiscoverMeetsResponse

    @GET("/api/skater-results/{profileId}")
    suspend fun getSkaterResults(@Path("profileId") profileId: String): SkaterResultsDto

    @GET("/api/coach/roster")
    suspend fun getCoachRoster(): List<CoachRosterMemberDto>

    @GET("/api/coach/pending")
    suspend fun getCoachPending(): List<CoachPendingMemberDto>

    @POST("/api/coach/announcement")
    suspend fun postCoachAnnouncement(@Body body: PostAnnouncementRequest): CoachAnnouncementDto

    @POST("/api/coach/practices")
    suspend fun createCoachPractice(@Body body: CreatePracticeRequest): CoachPracticeDto

    @DELETE("/api/coach/practices/{id}")
    suspend fun deleteCoachPractice(@Path("id") id: String)

    @DELETE("/api/coach/announcements/{id}")
    suspend fun deleteCoachAnnouncement(@Path("id") id: String)

    @POST("/api/coach/roster/{memberId}/dues")
    suspend fun setMemberDues(@Path("memberId") memberId: String, @Body body: SetDuesRequest): CoachRosterMemberDto

    @POST("/api/coach/approve")
    suspend fun updatePendingMember(@Body body: ApprovePendingRequest)

    @POST("/api/mobile/push-token")
    suspend fun registerPushToken(@Body body: RegisterPushTokenRequest)

    @HTTP(method = "DELETE", path = "/api/mobile/push-token", hasBody = true)
    suspend fun unregisterPushToken(@Body body: UnregisterPushTokenRequest)

    // League Director
    @GET("/api/league-director/pending-coaches")
    suspend fun getPendingCoaches(): List<PendingPersonDto>

    @POST("/api/league-director/pending-coaches/{id}")
    suspend fun approveCoachRequest(@Path("id") id: String, @Body body: CoachApprovalRequest)

    @GET("/api/director/league-stats")
    suspend fun getLeagueStats(@Query("league") league: String? = null): LeagueStatsDto

    // Tabulator / meet-director discussions
    @GET("/api/meet-director/discussions")
    suspend fun getDiscussions(): DiscussionsResponse

    @GET("/api/meet-director/discussions/{id}")
    suspend fun getDiscussion(@Path("id") id: String): DiscussionDetailResponse

    @POST("/api/meet-director/discussions")
    suspend fun createDiscussion(@Body body: CreateDiscussionRequest): DiscussionDto

    @POST("/api/meet-director/discussions/{id}/replies")
    suspend fun createDiscussionReply(@Path("id") id: String, @Body body: CreateReplyRequest): DiscussionReplyDto

    // Admin
    @GET("/api/admin/stats")
    suspend fun getAdminStats(): AdminStatsDto

    @GET("/api/admin/users")
    suspend fun searchAdminUsers(@Query("q") query: String): List<PendingPersonDto>

    @GET("/api/admin/profile/{id}")
    suspend fun getAdminProfile(@Path("id") id: String): AdminProfileDto

    @PUT("/api/admin/profile/{id}")
    suspend fun updateAdminProfile(@Path("id") id: String, @Body body: AdminProfileUpdateRequest): AdminProfileDto

    @GET("/api/admin/pending-roles")
    suspend fun getPendingRoles(): List<PendingPersonDto>

    @POST("/api/admin/approve-role")
    suspend fun approveRoleRequest(@Body body: ApproveRoleRequest)

    @GET("/api/director/schedule")
    suspend fun getDirectorSchedule(@Query("league") league: String? = null): List<ScheduleEventDto>

    @POST("/api/director/schedule")
    suspend fun createScheduleEvent(@Body body: ScheduleEventRequest): ScheduleEventDto

    @PUT("/api/director/schedule/{id}")
    suspend fun updateScheduleEvent(@Path("id") id: String, @Body body: ScheduleEventRequest): ScheduleEventDto

    @DELETE("/api/director/schedule/{id}")
    suspend fun deleteScheduleEvent(@Path("id") id: String)
}
