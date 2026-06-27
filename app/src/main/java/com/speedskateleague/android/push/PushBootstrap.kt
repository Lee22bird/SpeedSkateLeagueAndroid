package com.speedskateleague.android.push

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import com.speedskateleague.android.network.SslApiClient
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Fetches the current FCM token and registers/unregisters it with the backend. Called after a
 * successful sign-in and before sign-out, mirroring the retrySyncIfTokenCached /
 * unregisterCachedTokenIfPresent calls around session changes in Speed_Skate_League_APPApp.swift.
 */
object PushBootstrap {
    suspend fun registerCurrentToken(context: Context, apiClient: SslApiClient) {
        val token = currentToken() ?: return
        PushTokenSyncService(context, apiClient).register(token)
    }

    suspend fun unregisterCurrentToken(context: Context, apiClient: SslApiClient) {
        val token = currentToken() ?: return
        PushTokenSyncService(context, apiClient).unregister(token)
    }

    private suspend fun currentToken(): String? = suspendCancellableCoroutine { continuation ->
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { continuation.resumeWith(Result.success(it)) }
            .addOnFailureListener { continuation.resumeWith(Result.success(null)) }
    }
}
