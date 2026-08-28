package com.kvajipertya.lessons

import android.app.Application
import android.app.Activity
import android.os.Bundle
import com.russhwolf.settings.SharedPreferencesSettings
import com.kvajipertya.lessons.data.Repository

class LessonsApp : Application(), Application.ActivityLifecycleCallbacks {

    companion object {
        var isAppInForeground: Boolean = false
            private set
    }

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
        
        // Initialize Repository with actual Android storage
        val sharedPreferences = getSharedPreferences("lessons_prefs", MODE_PRIVATE)
        val settings = SharedPreferencesSettings(sharedPreferences)
        Repository.init(settings)
        
        // Setup Notifications
        NotificationHelper.createNotificationChannel(this)
        NotificationHelper.scheduleDailyCheck(this)
        NotificationHelper.scheduleSchoolMode(this)
    }

    override fun onActivityResumed(activity: Activity) {
        isAppInForeground = true
    }

    override fun onActivityPaused(activity: Activity) {
        isAppInForeground = false
    }

    // Required overrides but unused
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}
