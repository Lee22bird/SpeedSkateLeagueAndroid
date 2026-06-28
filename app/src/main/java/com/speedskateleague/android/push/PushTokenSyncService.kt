package com.speedskateleague.android.push

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.util.Log
import com.speedskateleague.android.BuildConfig
import com.speedskateleague.android.network.RegisterPushTokenRequest
import com.speedskateleague.android.network.SslApiClient
import com.speedskateleague.android.network.UnregisterPushTokenRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Sends this device's FCM token to the SpeedSkateLeague backend so it can target this device
 * with push, mirroring PushTokenSyncService in Speed_Skate_League_APPApp.swift (which does the
 * same for APNs tokens). Registration is silently skipped if there's no signed-in session yet —
 * SslApiClient simply omits the Authorization header and the backend rejects it; the token stays
 * cached locally by FCM and onNewToken (or a future explicit retry) will fire again later.
 */
class PushTokenSyncService(private val context: Context, private val apiClient: SslApiClient) {

    suspend fun register(token: String): Boolean {
        if (!apiClient.isSignedIn()) {
            Log.w("SslPush", "register: not signed in, skipping")
            return false
        }
        return withContext(Dispatchers.IO) {
            val result = runCatching {
                apiClient.api.registerPushToken(
                    RegisterPushTokenRequest(
                        token = token,
                        platform = "android",
                        environment = if (BuildConfig.DEBUG) "debug" else "production",
                        deviceId = deviceId(),
                        appVersion = BuildConfig.VERSION_NAME,
                    ),
                )
            }
            result.onSuccess {
                Log.d("SslPush", "register: backend call succeeded (token length=${token.length})")
            }.onFailure { error ->
                Log.e("SslPush", "register: backend call FAILED: ${error.javaClass.simpleName}: ${error.message}", error)
            }
            result.isSuccess
        }
    }

    suspend fun unregister(token: String) {
        withContext(Dispatchers.IO) {
            runCatching { apiClient.api.unregisterPushToken(UnregisterPushTokenRequest(token)) }
        }
    }

    @SuppressLint("HardwareIds")
    private fun deviceId(): String =
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: ""
}
