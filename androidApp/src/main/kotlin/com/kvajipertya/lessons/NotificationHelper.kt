package com.kvajipertya.lessons

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.kvajipertya.lessons.data.Repository
import kotlinx.datetime.*
import java.util.concurrent.TimeUnit

object NotificationHelper {
    private const val CHANNEL_ID = "school_reminders_v6"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "School Reminders"
            val descriptionText = "Notifications for homework and projects"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(true)
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(context: Context, title: String, message: String, reminderId: String? = null) {
        if (LessonsApp.isAppInForeground) return

        val notificationId = reminderId?.hashCode() ?: System.currentTimeMillis().toInt()
        
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)

        if (reminderId != null) {
            val intent = Intent(context, ReminderActionReceiver::class.java).apply {
                action = "MARK_AS_DONE"
                putExtra("reminderId", reminderId)
                putExtra("notificationId", notificationId)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, 
                notificationId, 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.ic_menu_save, "Mark as Done", pendingIntent)
        }

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(notificationId, builder.build())
            } catch (e: SecurityException) {
            }
        }
    }

    fun showSchoolNotification(context: Context, title: String, message: String) {
        if (LessonsApp.isAppInForeground) return

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW) // "Don't make it annoying"
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(999, builder.build()) // Use a fixed ID for school mode so it overwrites
            } catch (e: SecurityException) {
            }
        }
    }

    fun scheduleReminders(context: Context, reminderId: String, title: String, subject: String, dueDateString: String) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        
        val canNotifyNow = now.hour in 10..23
        
        if (canNotifyNow) {
            scheduleAlarm(context, reminderId, title, subject, System.currentTimeMillis() + 5000)
        } else {
            val triggerDateTime = if (now.hour < 10) {
                now.date.atTime(10, 0)
            } else {
                now.date.plus(1, DateTimeUnit.DAY).atTime(10, 0)
            }
            val triggerMillis = triggerDateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            scheduleAlarm(context, reminderId, title, subject, triggerMillis)
        }
    }

    fun scheduleNextNag(context: Context, reminderId: String, title: String, subject: String) {
        val nextTime = Clock.System.now().plus(5, DateTimeUnit.HOUR)
        val nextLocalDateTime = nextTime.toLocalDateTime(TimeZone.currentSystemDefault())
        
        val canNotifyNow = nextLocalDateTime.hour in 10..23
        
        val finalTriggerMillis = if (canNotifyNow) {
            nextTime.toEpochMilliseconds()
        } else {
            val triggerDate = if (nextLocalDateTime.hour < 10) {
                nextLocalDateTime.date
            } else {
                nextLocalDateTime.date.plus(1, DateTimeUnit.DAY)
            }
            triggerDate.atTime(10, 0).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        }
        
        scheduleAlarm(context, reminderId, title, subject, finalTriggerMillis)
    }

    private fun scheduleAlarm(context: Context, reminderId: String, title: String, subject: String, time: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("subject", subject)
            putExtra("reminderId", reminderId)
        }
        
        val requestCode = reminderId.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context, 
            requestCode, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
        }
    }
    
    fun cancelReminders(context: Context, reminderId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 
            reminderId.hashCode(), 
            intent, 
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }

    fun scheduleDailyCheck(context: Context) {
        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyCheckWorker>(5, TimeUnit.HOURS)
            .addTag("daily_check")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "daily_check",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyWorkRequest
        )
    }

    fun scheduleSchoolMode(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val repo = Repository.instance
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val currentDayName = now.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
        
        val todaySubjects = repo.timetable.value.filter { it.day.name == currentDayName }

        val morningTrigger = now.date.atTime(8, 30).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        if (System.currentTimeMillis() < morningTrigger) {
            val intent = Intent(context, SchoolModeAlarmReceiver::class.java).apply {
                putExtra("type", "MORNING_CHECK")
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, 1001, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, morningTrigger, pendingIntent)
        }

        todaySubjects.forEachIndexed { index, subject ->
            try {
                // Start Time
                val startParts = subject.startTime.split(":")
                val startTrigger = now.date.atTime(startParts[0].toInt(), startParts[1].toInt())
                    .toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
                
                if (System.currentTimeMillis() < startTrigger) {
                    val intent = Intent(context, SchoolModeAlarmReceiver::class.java).apply {
                        putExtra("type", "SUBJECT_START")
                        putExtra("subjectName", subject.subject)
                    }
                    val pendingIntent = PendingIntent.getBroadcast(
                        context, 2000 + index, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startTrigger, pendingIntent)
                }

                // End Time + 2 Minutes
                val endParts = subject.endTime.split(":")
                val endTrigger = now.date.atTime(endParts[0].toInt(), endParts[1].toInt())
                    .toInstant(TimeZone.currentSystemDefault())
                    .plus(2, DateTimeUnit.MINUTE).toEpochMilliseconds()
                
                if (System.currentTimeMillis() < endTrigger) {
                    val intent = Intent(context, SchoolModeAlarmReceiver::class.java).apply {
                        putExtra("type", "SUBJECT_END")
                    }
                    val pendingIntent = PendingIntent.getBroadcast(
                        context, 3000 + index, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endTrigger, pendingIntent)
                }
            } catch (e: Exception) {}
        }
    }
}
