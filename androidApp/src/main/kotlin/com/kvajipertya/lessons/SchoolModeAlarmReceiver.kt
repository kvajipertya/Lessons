package com.kvajipertya.lessons

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kvajipertya.lessons.data.Repository
import kotlinx.datetime.*

class SchoolModeAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra("type") ?: return
        
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val currentDayName = now.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
        val repo = Repository.instance
        val timetable = repo.timetable.value
        val todaySubjects = timetable.filter { it.day.name == currentDayName }
            .sortedBy { it.startTime }

        when (type) {
            "MORNING_CHECK" -> {
                if (todaySubjects.isEmpty()) {
                    NotificationHelper.showNotification(
                        context, 
                        "School Mode", 
                        "Nothing planned for today! Enjoy your day."
                    )
                }
            }
            "SUBJECT_END" -> {
                val currentTimeStr = "${now.hour.toString().padStart(2, '0')}:${now.minute.toString().padStart(2, '0')}"
                val nextSubject = todaySubjects.find { it.startTime > currentTimeStr }
                
                if (nextSubject != null) {
                    NotificationHelper.showNotification(
                        context, 
                        "Next Subject", 
                        "Upcoming: ${nextSubject.subject} at ${nextSubject.startTime}"
                    )
                }
            }
        }
    }
}
