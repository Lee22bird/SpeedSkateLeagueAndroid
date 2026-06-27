package com.speedskateleague.android.network

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Locale
import java.util.TimeZone

/**
 * Parses dates that may arrive as ISO8601 (with/without fractional seconds), yyyy-MM-dd, or
 * MM/dd/yyyy, returning epoch millis. Mirrors SSLDateParser / MeetDateParser in SSLNetworking.swift.
 * Unrecognized formats return null, matching the iOS fallback-to-end-of-list behavior.
 */
object SslDateParser {
    private val legacyFormats = listOf("yyyy-MM-dd", "MM/dd/yyyy")

    fun parseMillis(raw: String?): Long? {
        if (raw.isNullOrBlank()) return null
        runCatching { return Instant.parse(raw).toEpochMilli() }
        runCatching { return LocalDateTime.parse(raw).toInstant(ZoneOffset.UTC).toEpochMilli() }
        for (pattern in legacyFormats) {
            runCatching {
                val sdf = SimpleDateFormat(pattern, Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                return sdf.parse(raw)?.time
            }
        }
        return null
    }
}
