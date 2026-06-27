package com.speedskateleague.android.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.speedskateleague.android.MainActivity
import com.speedskateleague.android.R

private const val CHANNEL_ID = "ssl_notifications"

/** Fires the local "meet today" notification scheduled by MeetReminderScheduler. */
class MeetReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_MEET_TITLE).orEmpty()
        val location = intent.getStringExtra(EXTRA_MEET_LOCATION).orEmpty()

        val manager = context.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Speed Skate League", NotificationManager.IMPORTANCE_DEFAULT),
            )
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Meet Today: $title")
            .setContentText(location.ifBlank { "Don't forget to check in." })
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(title.hashCode(), notification)
    }
}
