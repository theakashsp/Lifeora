package com.liferise.app.tasks.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.liferise.app.MainActivity
import com.liferise.app.R

class TaskReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_TASK_REMINDER) return

        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        val title = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Task Reminder"
        val priority = intent.getStringExtra(EXTRA_TASK_PRIORITY) ?: "MEDIUM"

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createChannel(notificationManager)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "tasks")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Task Due: $title")
            .setContentText("Priority: $priority • Tap to view and mark complete.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(taskId.toInt(), notification)
    }

    private fun createChannel(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for scheduled tasks and deadlines"
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val ACTION_TASK_REMINDER = "com.liferise.app.ACTION_TASK_REMINDER"
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
        const val EXTRA_TASK_PRIORITY = "extra_task_priority"
        const val CHANNEL_ID = "liferise_tasks_channel"
    }
}
