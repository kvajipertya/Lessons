package com.kvajipertya.lessons

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kvajipertya.lessons.data.Repository
import com.kvajipertya.lessons.ui.Strings
import kotlinx.datetime.*

class SchoolModeAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra("type") ?: return
        
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val currentDayName = now.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
        val repo = Repository.instance
        val language = repo.language.value
        val timetable = repo.timetable.value
        val todaySubjects = timetable.filter { it.day.name == currentDayName }
            .sortedBy { it.startTime }

        when (type) {
            "MORNING_CHECK" -> {
                val title = Strings.get("today_title", language)
                val message = if (todaySubjects.isEmpty()) {
                    Strings.get("nothing_planned", language)
                } else {
                    // Manual translation for lesson count to keep it simple
                    if (language == "Georgian") "დღეს გაქვთ ${todaySubjects.size} გაკვეთილი."
                    else "You have ${todaySubjects.size} lessons today."
                }
                NotificationHelper.showSchoolNotification(context, title, message)
            }
            "SUBJECT_START" -> {
                val subjectName = intent.getStringExtra("subjectName") ?: "Subject"
                NotificationHelper.showSchoolNotification(
                    context, 
                    Strings.get("active_lesson", language), 
                    "${Strings.get("today", language)}: $subjectName" // Using "today" as "Now" shortcut or just "Now"
                )
            }
            "SUBJECT_END" -> {
                val currentTimeStr = "${now.hour.toString().padStart(2, '0')}:${now.minute.toString().padStart(2, '0')}"
                val nextSubject = todaySubjects.find { it.startTime > currentTimeStr }
                
                if (nextSubject != null) {
                    NotificationHelper.showSchoolNotification(
                        context, 
                        Strings.get("next_lesson", language), 
                        "${nextSubject.subject} @ ${nextSubject.startTime}"
                    )
                } else {
                    NotificationHelper.showSchoolNotification(
                        context, 
                        Strings.get("school_over", language), 
                        Strings.get("no_subjects", language)
                    )
                }
            }
        }
    }
}
