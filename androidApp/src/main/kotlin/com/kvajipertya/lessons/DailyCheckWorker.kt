package com.kvajipertya.lessons

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.kvajipertya.lessons.data.Repository
import kotlinx.datetime.*
import kotlinx.coroutines.runBlocking

class DailyCheckWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        
        val canNotifyNow = now.hour in 10..23
        
        runBlocking {
            Repository.instance.cleanupPastReminders()
        }

        if (canNotifyNow) {
            val reminders = runBlocking {
                Repository.instance.reminders.value
            }
            
            val upcoming = reminders.filter { !it.isCompleted }.filter {
                try {
                    val dueDate = LocalDate.parse(it.dueDate)
                    val days = dueDate.toEpochDays() - now.date.toEpochDays()
                    days in 0..3
                } catch (e: Exception) {
                    false
                }
            }

            if (upcoming.isNotEmpty()) {
                val language = Repository.instance.language.value
                val count = upcoming.size
                val subjects = upcoming.joinToString(", ") { it.subject }
                
                val title = if (language == "Georgian") "დღიური გეგმა" else "Daily School Agenda"
                val message = if (language == "Georgian") {
                    "თქვენ გაქვთ $count შეხსენება: $subjects"
                } else {
                    "You have $count item(s) due soon: $subjects"
                }

                NotificationHelper.showNotification(
                    applicationContext, 
                    title, 
                    message
                )
            }
        }
        
        return Result.success()
    }
}
