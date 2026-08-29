package com.kvajipertya.lessons

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kvajipertya.lessons.data.Repository
import com.kvajipertya.lessons.ui.Strings
import kotlinx.datetime.*

class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Reminder"
        val subject = intent.getStringExtra("subject") ?: ""
        val reminderId = intent.getStringExtra("reminderId") ?: return

        val repo = Repository.instance
        val language = repo.language.value
        val reminder = repo.reminders.value.find { it.id == reminderId }
        
        if (reminder != null && !reminder.isCompleted) {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val dueDate = LocalDate.parse(reminder.dueDate)
            val daysLeft = dueDate.toEpochDays() - now.toEpochDays()
            
            val timeText = when {
                daysLeft < 0 -> Strings.get("overdue", language)
                daysLeft == 0 -> Strings.get("today", language)
                daysLeft == 1 -> Strings.get("tomorrow", language)
                else -> "$daysLeft ${Strings.get("days_left", language)}"
            }

            NotificationHelper.showNotification(context, title, "${Strings.get("due_date", language)}: $timeText", reminderId)
            NotificationHelper.scheduleNextNag(context, reminderId, title, subject)
        } else if (reminder == null) {
            NotificationHelper.cancelReminders(context, reminderId)
        }
    }
}
