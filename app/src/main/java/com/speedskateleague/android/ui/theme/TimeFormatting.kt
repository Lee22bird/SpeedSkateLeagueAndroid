package com.speedskateleague.android.ui.theme

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/** Short relative-time label ("3h ago", "2d ago"), falling back to a date once it's old enough. */
fun relativeTimeLabel(millis: Long?): String {
    if (millis == null) return ""
    val delta = System.currentTimeMillis() - millis
    if (delta < 0) return "just now"
    val minutes = TimeUnit.MILLISECONDS.toMinutes(delta)
    val hours = TimeUnit.MILLISECONDS.toHours(delta)
    val days = TimeUnit.MILLISECONDS.toDays(delta)
    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> SimpleDateFormat("MMM d", Locale.US).format(Date(millis))
    }
}

fun dateLabel(millis: Long?): String {
    if (millis == null) return "Date unavailable"
    return SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(millis))
}

fun monthDayParts(millis: Long?): Pair<String, String> {
    if (millis == null) return "—" to "—"
    val date = Date(millis)
    val month = SimpleDateFormat("MMM", Locale.US).format(date).uppercase()
    val day = SimpleDateFormat("d", Locale.US).format(date)
    return month to day
}
