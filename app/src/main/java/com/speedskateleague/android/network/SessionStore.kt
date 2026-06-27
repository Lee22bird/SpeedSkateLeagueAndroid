package com.speedskateleague.android.network

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Android equivalent of MobileSessionStore (SSLNetworking.swift:48), which used the
 * Keychain. EncryptedSharedPreferences is the standard Android analog.
 */
class SessionStore(context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "ssl_mobile_session",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    fun save(session: MobileAuthSession) {
        prefs.edit().putString(KEY_SESSION, json.encodeToString(session)).apply()
    }

    fun load(): MobileAuthSession? {
        val raw = prefs.getString(KEY_SESSION, null) ?: return null
        return runCatching { json.decodeFromString(MobileAuthSession.serializer(), raw) }.getOrNull()
    }

    fun clear() {
        prefs.edit().remove(KEY_SESSION).apply()
    }

    companion object {
        private const val KEY_SESSION = "ssl-mobile-session"
    }
}
