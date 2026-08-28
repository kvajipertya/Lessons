package com.kvajipertya.lessons

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.kvajipertya.lessons.data.Repository

class ReminderActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getStringExtra("reminderId") ?: return
        val action = intent.action

        if (action == "MARK_AS_DONE") {
            // Update repository
            Repository.instance.toggleReminder(reminderId)
            
            // Cancel the notification
            val notificationId = intent.getIntExtra("notificationId", 0)
            NotificationManagerCompat.from(context).cancel(notificationId)
        }
    }
}
