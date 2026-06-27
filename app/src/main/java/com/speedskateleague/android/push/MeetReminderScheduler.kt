package com.speedskateleague.android.push

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.speedskateleague.android.network.SslMeet
import java.util.Calendar

private const val PREFS_NAME = "ssl_meet_reminders"
private const val PREFS_KEY_SCHEDULED_IDS = "scheduled_meet_ids"

const val EXTRA_MEET_TITLE = "meet_title"
const val EXTRA_MEET_LOCATION = "meet_location"

/**
 * Schedules a local notification for 8:00 AM on the day of each upcoming meet, mirroring
 * MeetReminderService in UpcomingMeets.swift. Uses AlarmManager instead of
 * UNCalendarNotificationTrigger; since this is a best-effort reminder (not time-critical), it
 * uses the inexact-while-idle alarm so it doesn't need the SCHEDULE_EXACT_ALARM permission.
 */
object MeetReminderScheduler {

    fun scheduleReminders(context: Context, meets: List<SslMeet>) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val previousIds = prefs.getStringSet(PREFS_KEY_SCHEDULED_IDS, emptySet()).orEmpty()

        val relevantMeets = meets.filter { it.dateMillis != null }
        val currentIds = relevantMeets.map { it.id }.toSet()

        (previousIds - currentIds).forEach { staleId ->
            alarmManager.cancel(pendingIntentFor(context, staleId, "", ""))
        }

        val scheduledIds = mutableSetOf<String>()
        val now = System.currentTimeMillis()
        for (meet in relevantMeets) {
            val reminderMillis = reminderTimeFor(meet.dateMillis!!) ?: continue
            if (reminderMillis <= now) continue
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderMillis,
                pendingIntentFor(context, meet.id, meet.title, meet.displayLocation),
            )
            scheduledIds.add(meet.id)
        }

        prefs.edit().putStringSet(PREFS_KEY_SCHEDULED_IDS, scheduledIds).apply()
    }

    fun cancelAllReminders(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val ids = prefs.getStringSet(PREFS_KEY_SCHEDULED_IDS, emptySet()).orEmpty()
        ids.forEach { id -> alarmManager.cancel(pendingIntentFor(context, id, "", "")) }
        prefs.edit().remove(PREFS_KEY_SCHEDULED_IDS).apply()
    }

    /** 8:00 AM on the day of the meet. */
    private fun reminderTimeFor(meetDateMillis: Long): Long? {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = meetDateMillis
        calendar.set(Calendar.HOUR_OF_DAY, 8)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun pendingIntentFor(context: Context, meetId: String, title: String, location: String): PendingIntent {
        val intent = Intent(context, MeetReminderReceiver::class.java).apply {
            putExtra(EXTRA_MEET_TITLE, title)
            putExtra(EXTRA_MEET_LOCATION, location)
        }
        return PendingIntent.getBroadcast(
            context,
            meetId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
