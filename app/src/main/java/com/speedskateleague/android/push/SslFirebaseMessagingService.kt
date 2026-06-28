package com.speedskateleague.android.push

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.speedskateleague.android.MainActivity
import com.speedskateleague.android.R
import com.speedskateleague.android.SslApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val NOTIFICATION_ID = 1001

/**
 * Android equivalent of the AppDelegate push-handling code in Speed_Skate_League_APPApp.swift:
 * receives FCM messages, shows a system notification, and captures the routing intent so a tap
 * can deep-link into the right screen.
 */
class SslFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        val apiClient = (application as SslApplication).apiClient
        CoroutineScope(Dispatchers.IO).launch {
            PushTokenSyncService(applicationContext, apiClient).register(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        PendingNotificationRouteStore.capture(message.data)

        val title = message.notification?.title ?: message.data["title"] ?: "Speed Skate League"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        ensureSslNotificationChannel(this)
        val manager = getSystemService(NotificationManager::class.java)

        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, SSL_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }
}
