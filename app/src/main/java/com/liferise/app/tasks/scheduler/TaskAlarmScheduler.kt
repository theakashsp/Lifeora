package com.liferise.app.tasks.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.liferise.app.domain.model.Task
import com.liferise.app.tasks.receiver.TaskReminderReceiver

class TaskAlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleTaskReminder(task: Task) {
        if (!task.hasReminder || task.isCompleted || task.dueDate <= System.currentTimeMillis()) {
            return
        }

        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            action = TaskReminderReceiver.ACTION_TASK_REMINDER
            putExtra(TaskReminderReceiver.EXTRA_TASK_ID, task.id)
            putExtra(TaskReminderReceiver.EXTRA_TASK_TITLE, task.title)
            putExtra(TaskReminderReceiver.EXTRA_TASK_PRIORITY, task.priority.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        task.dueDate,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        task.dueDate,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    task.dueDate,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            // Permission denied fallback
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                task.dueDate,
                pendingIntent
            )
        }
    }

    fun cancelTaskReminder(taskId: Long) {
        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            action = TaskReminderReceiver.ACTION_TASK_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
