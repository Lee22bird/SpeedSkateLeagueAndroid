package com.speedskateleague.android.push

enum class SslNotificationKind { ANNOUNCEMENT, MEET, PROFILE, RESULTS, UNKNOWN }

private fun kindFromRawType(rawType: String?): SslNotificationKind {
    val value = rawType.orEmpty().lowercase()
    return when {
        "announcement" in value -> SslNotificationKind.ANNOUNCEMENT
        "meet" in value -> SslNotificationKind.MEET
        "profile" in value -> SslNotificationKind.PROFILE
        "result" in value -> SslNotificationKind.RESULTS
        else -> SslNotificationKind.UNKNOWN
    }
}

/**
 * A notification's routing intent, parsed from the push payload's top-level keys. Mirrors
 * SSLPendingNotificationRoute in Speed_Skate_League_APPApp.swift.
 */
data class PendingNotificationRoute(
    val kind: SslNotificationKind,
    val sourceId: String?,
    val actionUrl: String?,
) {
    companion object {
        fun from(data: Map<String, String>): PendingNotificationRoute? {
            val rawType = data["type"] ?: data["source_type"]
            val announcementId = data["announcement_id"]
            val meetId = data["meet_id"]
            val sourceId = data["source_id"]
            val actionUrl = data["action_url"]

            if (rawType == null && announcementId == null && meetId == null && sourceId == null && actionUrl == null) {
                return null
            }

            return PendingNotificationRoute(
                kind = kindFromRawType(rawType),
                sourceId = sourceId ?: announcementId ?: meetId,
                actionUrl = actionUrl,
            )
        }
    }
}

/**
 * Holds a notification's routing intent until the Compose UI is ready to consume it, so a cold
 * launch never loses the deep link to a "nobody was listening yet" race. Mirrors
 * PendingNotificationRouteStore in Speed_Skate_League_APPApp.swift.
 */
object PendingNotificationRouteStore {
    @Volatile
    private var pendingRoute: PendingNotificationRoute? = null

    fun capture(data: Map<String, String>) {
        val route = PendingNotificationRoute.from(data) ?: return
        pendingRoute = route
    }

    /** Returns the pending route, if any, and clears it — a route is only ever consumed once. */
    fun consume(): PendingNotificationRoute? {
        val route = pendingRoute
        pendingRoute = null
        return route
    }
}
