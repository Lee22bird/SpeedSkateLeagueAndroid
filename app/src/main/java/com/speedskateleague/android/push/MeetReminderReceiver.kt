package com.speedskateleague.android.push

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.speedskateleague.android.MainActivity
import com.speedskateleague.android.R

/** Fires the local "meet today" notification scheduled by MeetReminderScheduler. */
class MeetReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_MEET_TITLE).orEmpty()
        val location = intent.getStringExtra(EXTRA_MEET_LOCATION).orEmpty()

        ensureSslNotificationChannel(context)
        val manager = context.getSystemService(NotificationManager::class.java)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, SSL_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Meet Today: $title")
            .setContentText(location.ifBlank { "Don't forget to check in." })
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(title.hashCode(), notification)
    }
}
