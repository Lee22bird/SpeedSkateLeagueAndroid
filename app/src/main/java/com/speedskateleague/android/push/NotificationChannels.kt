package com.speedskateleague.android.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

/**
 * Channel id bumped to v2 + IMPORTANCE_HIGH so this shows a heads-up banner (matching iOS's
 * .banner presentation option), not just a silent shade entry. Channel importance is immutable
 * once created on a device, so changing IMPORTANCE_DEFAULT on the old "ssl_notifications" id in
 * code would have had no effect on devices that already created it — a new id is required.
 */
const val SSL_NOTIFICATION_CHANNEL_ID = "ssl_notifications_v2"

fun ensureSslNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val manager = context.getSystemService(NotificationManager::class.java)
    val channel = NotificationChannel(
        SSL_NOTIFICATION_CHANNEL_ID,
        "Speed Skate League",
        NotificationManager.IMPORTANCE_HIGH,
    )
    manager.createNotificationChannel(channel)
}
