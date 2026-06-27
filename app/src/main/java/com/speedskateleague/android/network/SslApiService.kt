package com.speedskateleague.android.network

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
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
    suspend fun registerPushToken(@Body body: Map<String, String>)

    @DELETE("/api/mobile/push-token")
    suspend fun unregisterPushToken()
}
