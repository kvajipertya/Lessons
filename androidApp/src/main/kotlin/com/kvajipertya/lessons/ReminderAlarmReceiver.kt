package com.kvajipertya.lessons

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kvajipertya.lessons.data.Repository

class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Reminder"
        val subject = intent.getStringExtra("subject") ?: ""
        val reminderId = intent.getStringExtra("reminderId") ?: return

        val repo = Repository.instance
        val reminder = repo.reminders.value.find { it.id == reminderId }
        
        if (reminder != null && !reminder.isCompleted) {
            NotificationHelper.showNotification(context, title, "Due: ${reminder.dueDate}", reminderId)
            NotificationHelper.scheduleNextNag(context, reminderId, title, subject)
        } else if (reminder == null) {
            NotificationHelper.cancelReminders(context, reminderId)
        }
    }
}
