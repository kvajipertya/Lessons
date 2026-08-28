package com.kvajipertya.lessons

import android.os.Bundle
import android.os.Build
import android.Manifest
import android.provider.Settings as AndroidSettings
import android.content.Intent
import android.net.Uri
import com.kvajipertya.lessons.data.Repository
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Handle result
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // Notification permission request (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Exact Alarm permission check (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(AndroidSettings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
        }

        // Re-sync all reminders and cleanup old ones
        val repository = Repository.instance
        repository.cleanupPastReminders()
        repository.reminders.value.forEach { reminder ->
            if (!reminder.isCompleted) {
                NotificationHelper.scheduleReminders(
                    this, reminder.id, reminder.title, reminder.subject, reminder.dueDate
                )
            }
        }

        setContent {
            App(
                onScheduleReminder = { reminder ->
                    NotificationHelper.scheduleReminders(
                        context = this,
                        reminderId = reminder.id,
                        title = reminder.title,
                        subject = reminder.subject,
                        dueDateString = reminder.dueDate
                    )
                },
                onCancelReminder = { id ->
                    NotificationHelper.cancelReminders(this, id)
                }
            )
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
