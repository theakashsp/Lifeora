package com.liferise.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.liferise.app.alarms.service.AlarmAudioService
import com.liferise.app.core.di.AppContainer
import com.liferise.app.core.di.DefaultAppContainer
import com.liferise.app.tasks.receiver.TaskReminderReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LifeRiseApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)

        createNotificationChannels()

        // Asynchronously populate starter questions if not already present
        CoroutineScope(Dispatchers.IO).launch {
            container.quizRepository.initializeStarterQuestions()
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // 1. Task Reminders Channel
            val taskChannel = NotificationChannel(
                TaskReminderReceiver.CHANNEL_ID,
                "Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for scheduled tasks and deadlines"
                enableVibration(true)
            }

            // 2. Alarm Channel
            val alarmChannel = NotificationChannel(
                AlarmAudioService.CHANNEL_ID,
                "Challenge Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority wake-up alarms with quiz challenges"
                setSound(null, null)
                enableVibration(false)
            }

            notificationManager.createNotificationChannels(listOf(taskChannel, alarmChannel))
        }
    }
}
