package com.speedskateleague.android.network

import android.content.Context
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.create
import java.util.concurrent.TimeUnit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

private const val BASE_URL = "https://speedskateleague.com"
private const val RETRY_HEADER = "X-SSL-Retry"

/**
 * Android equivalent of SSLAPIClient (SSLNetworking.swift:169): bearer-token auth with
 * automatic refresh-and-retry, backed by EncryptedSharedPreferences instead of Keychain.
 *
 * Refresh-and-retry is implemented as an Interceptor rather than an OkHttp Authenticator
 * because this backend's getSessionUser() returns HTTP 403 ("Forbidden") for an expired or
 * invalid session token — not 401. OkHttp's Authenticator API only ever fires for 401, so an
 * Authenticator-based approach (the original implementation) silently never refreshes once a
 * token ages out; every request just permanently 403s until the user manually re-logs in.
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

    private fun refreshAndRetry(chain: okhttp3.Interceptor.Chain, response: Response): Response? {
        if (response.request.header(RETRY_HEADER) != null) return null
        val session = sessionStore.load() ?: return null
        val refreshed = runCatching {
            runBlocking { plainApi.refresh(RefreshRequest(session.refreshToken)) }
        }.getOrNull() ?: run {
            sessionStore.clear()
            return null
        }
        sessionStore.save(refreshed.session)
        val retryRequest = response.request.newBuilder()
            .header("Authorization", "Bearer ${refreshed.session.accessToken}")
            .header(RETRY_HEADER, "1")
            .build()
        response.close()
        return chain.proceed(retryRequest)
    }

    private val authedClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val session = sessionStore.load()
            val request: Request = if (session != null) {
                chain.request().newBuilder()
                    .header("Authorization", "Bearer ${session.accessToken}")
                    .build()
            } else {
                chain.request()
            }
            val response = chain.proceed(request)
            if (response.code == 401 || response.code == 403) {
                refreshAndRetry(chain, response) ?: response
            } else {
                response
            }
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
