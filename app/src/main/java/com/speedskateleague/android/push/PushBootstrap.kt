package com.speedskateleague.android.push

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.speedskateleague.android.network.SslApiClient
import kotlinx.coroutines.suspendCancellableCoroutine

private const val TAG = "SslPush"
private const val PUSH_PREFS = "ssl_push_bootstrap"
private const val TOKEN_GENERATION_KEY = "fcm_token_generation"
private const val CURRENT_TOKEN_GENERATION = 2

/**
 * Fetches the current FCM token and registers/unregisters it with the backend. Called after a
 * successful sign-in and before sign-out, mirroring the retrySyncIfTokenCached /
 * unregisterCachedTokenIfPresent calls around session changes in Speed_Skate_League_APPApp.swift.
 */
object PushBootstrap {
    suspend fun registerCurrentToken(context: Context, apiClient: SslApiClient) {
        val preferences = context.getSharedPreferences(PUSH_PREFS, Context.MODE_PRIVATE)
        if (preferences.getInt(TOKEN_GENERATION_KEY, 0) < CURRENT_TOKEN_GENERATION) {
            rotateAndRegisterToken(context, apiClient, preferences)
            return
        }
        Log.d(TAG, "registerCurrentToken: fetching FCM token...")
        val token = currentToken()
        if (token == null) {
            Log.w(TAG, "registerCurrentToken: no token available, skipping registration")
            return
        }
        Log.d(TAG, "registerCurrentToken: got token (len=${token.length}), calling backend")
        val registered = PushTokenSyncService(context, apiClient).register(token)
        Log.d(TAG, "registerCurrentToken: backend accepted token = $registered")
    }

    suspend fun unregisterCurrentToken(context: Context, apiClient: SslApiClient) {
        val token = currentToken() ?: return
        PushTokenSyncService(context, apiClient).unregister(token)
    }

    private suspend fun currentToken(): String? = suspendCancellableCoroutine { continuation ->
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener {
                Log.d(TAG, "FirebaseMessaging.token success")
                continuation.resumeWith(Result.success(it))
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "FirebaseMessaging.token FAILED: ${error.javaClass.simpleName}: ${error.message}", error)
                continuation.resumeWith(Result.success(null))
            }
    }

    private suspend fun rotateAndRegisterToken(
        context: Context,
        apiClient: SslApiClient,
        preferences: android.content.SharedPreferences,
    ) {
        Log.d(TAG, "rotateAndRegisterToken: replacing legacy FCM token")
        currentToken()?.let { oldToken ->
            runCatching { PushTokenSyncService(context, apiClient).unregister(oldToken) }
        }
        if (!deleteCurrentToken()) {
            Log.w(TAG, "rotateAndRegisterToken: Firebase token deletion failed; will retry next launch")
            return
        }
        val freshToken = currentToken()
        if (freshToken == null) {
            Log.w(TAG, "rotateAndRegisterToken: Firebase did not return a fresh token")
            return
        }
        val registered = PushTokenSyncService(context, apiClient).register(freshToken)
        if (registered) {
            preferences.edit().putInt(TOKEN_GENERATION_KEY, CURRENT_TOKEN_GENERATION).apply()
            Log.d(TAG, "rotateAndRegisterToken: fresh token registered (length=${freshToken.length})")
        } else {
            Log.w(TAG, "rotateAndRegisterToken: backend registration failed; will retry next launch")
        }
    }

    private suspend fun deleteCurrentToken(): Boolean = suspendCancellableCoroutine { continuation ->
        FirebaseMessaging.getInstance().deleteToken()
            .addOnSuccessListener { continuation.resumeWith(Result.success(true)) }
            .addOnFailureListener { error ->
                Log.e(TAG, "FirebaseMessaging.deleteToken FAILED: ${error.javaClass.simpleName}: ${error.message}", error)
                continuation.resumeWith(Result.success(false))
            }
    }
}
