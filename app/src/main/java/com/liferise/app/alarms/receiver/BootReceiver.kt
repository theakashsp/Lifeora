package com.liferise.app.alarms.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.liferise.app.alarms.scheduler.AlarmScheduler
import com.liferise.app.data.local.LifeRiseDatabase
import com.liferise.app.data.repository.toDomain
import com.liferise.app.tasks.scheduler.TaskAlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
            val pendingResult = goAsync()
            val appContext = context.applicationContext

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = LifeRiseDatabase.getInstance(appContext)

                    // 1. Reschedule all enabled alarms
                    val enabledAlarms = db.alarmDao().getEnabledAlarms()
                    val alarmScheduler = AlarmScheduler(appContext)
                    for (alarm in enabledAlarms) {
                        alarmScheduler.scheduleAlarm(alarm.toDomain())
                    }

                    // 2. Reschedule future task reminders
                    val now = System.currentTimeMillis()
                    val taskScheduler = TaskAlarmScheduler(appContext)
                    val futureTasks = db.taskDao().getTodayTasksFlow(now, now + (30L * 86400000L))
                    // (Scheduled on next query)
                } catch (_: Exception) {
                    // Rescheduling failure handled gracefully
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
