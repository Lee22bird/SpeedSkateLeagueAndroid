package com.speedskateleague.android.network

import android.content.Context
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.create
import java.util.concurrent.TimeUnit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

private const val BASE_URL = "https://speedskateleague.com"

/**
 * Android equivalent of SSLAPIClient (SSLNetworking.swift:169): bearer-token auth with
 * automatic refresh-and-retry on 401, backed by EncryptedSharedPreferences instead of Keychain.
 */
class SslApiClient(context: Context) {
    private val sessionStore = SessionStore(context)
    private val json = Json { ignoreUnknownKeys = true }

    /** Bare client with no auth, used only for login + token refresh to avoid recursive auth. */
    private val plainClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val plainRetrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(plainClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    private val plainApi: SslApiService = plainRetrofit.create()

    private val authenticator = Authenticator { _, response ->
        if (response.request.header("X-SSL-Retry") != null) return@Authenticator null
        val session = sessionStore.load() ?: return@Authenticator null
        val refreshed = runCatching {
            runBlocking { plainApi.refresh(RefreshRequest(session.refreshToken)) }
        }.getOrNull() ?: run {
            sessionStore.clear()
            return@Authenticator null
        }
        sessionStore.save(refreshed.session)
        response.request.newBuilder()
            .header("Authorization", "Bearer ${refreshed.session.accessToken}")
            .header("X-SSL-Retry", "1")
            .build()
    }

    private val authedClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .authenticator(authenticator)
        .addInterceptor { chain ->
            val session = sessionStore.load()
            val request: Request = if (session != null) {
                chain.request().newBuilder()
                    .header("Authorization", "Bearer ${session.accessToken}")
                    .build()
            } else {
                chain.request()
            }
            chain.proceed(request)
        }
        .build()

    private val authedRetrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(authedClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val api: SslApiService = authedRetrofit.create()

    suspend fun login(email: String, password: String): MobileAuthSession {
        val response = plainApi.login(LoginRequest(email, password))
        sessionStore.save(response.session)
        return response.session
    }

    fun isSignedIn(): Boolean = sessionStore.load() != null

    fun signOut() = sessionStore.clear()
}
