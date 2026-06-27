package com.speedskateleague.android.network

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

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
    suspend fun getNotifications(): NotificationsResponse

    @POST("/api/notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: String)

    @POST("/api/notifications/read-all")
    suspend fun markAllNotificationsRead()

    @POST("/api/mobile/push-token")
    suspend fun registerPushToken(@Body body: Map<String, String>)

    @DELETE("/api/mobile/push-token")
    suspend fun unregisterPushToken()
}
