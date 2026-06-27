package com.speedskateleague.android.push

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.speedskateleague.android.network.SslApiClient
import kotlinx.coroutines.suspendCancellableCoroutine

private const val TAG = "SslPush"

/**
 * Fetches the current FCM token and registers/unregisters it with the backend. Called after a
 * successful sign-in and before sign-out, mirroring the retrySyncIfTokenCached /
 * unregisterCachedTokenIfPresent calls around session changes in Speed_Skate_League_APPApp.swift.
 */
object PushBootstrap {
    suspend fun registerCurrentToken(context: Context, apiClient: SslApiClient) {
        Log.d(TAG, "registerCurrentToken: fetching FCM token...")
        val token = currentToken()
        if (token == null) {
            Log.w(TAG, "registerCurrentToken: no token available, skipping registration")
            return
        }
        Log.d(TAG, "registerCurrentToken: got token (len=${token.length}), calling backend")
        val result = runCatching { PushTokenSyncService(context, apiClient).register(token) }
        Log.d(TAG, "registerCurrentToken: backend call result = $result")
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
}
