package com.liferise.app.alarms.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.liferise.app.MainActivity
import com.liferise.app.alarms.receiver.AlarmReceiver
import com.liferise.app.domain.model.AlarmItem
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarm(alarm: AlarmItem) {
        if (!alarm.isEnabled) {
            cancelAlarm(alarm.id)
            return
        }

        val triggerTime = computeNextTriggerTime(alarm.hour, alarm.minute, alarm.repeatDays)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_FIRE_ALARM
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarm.id)
            putExtra(AlarmReceiver.EXTRA_ALARM_LABEL, alarm.label)
            putExtra(AlarmReceiver.EXTRA_ALARM_CATEGORY, alarm.challengeCategory.name)
            putExtra(AlarmReceiver.EXTRA_ALARM_DIFFICULTY, alarm.difficulty.name)
            putExtra(AlarmReceiver.EXTRA_ALARM_REQUIRED_CORRECT, alarm.requiredCorrectAnswers)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Show intent for AlarmClockInfo
        val showIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_to", "alarm")
        }
        val showPendingIntent = PendingIntent.getActivity(
            context,
            alarm.id.toInt(),
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            // AlarmClockInfo is the official Android mechanism for reliable waking even during Doze
            val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        } catch (_: SecurityException) {
            // Fallback if exact alarm permission is restricted
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        }
    }

    fun cancelAlarm(alarmId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_FIRE_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun snoozeAlarm(alarmId: Long, minutes: Int = 10) {
        val triggerTime = System.currentTimeMillis() + (minutes * 60 * 1000L)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_FIRE_ALARM
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmReceiver.EXTRA_ALARM_LABEL, "Snoozed Alarm")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } catch (_: Exception) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    fun canScheduleExact(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    companion object {
        fun computeNextTriggerTime(hour: Int, minute: Int, repeatDays: Set<Int>): Long {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (repeatDays.isEmpty()) {
                if (target.timeInMillis <= now.timeInMillis) {
                    target.add(Calendar.DAY_OF_YEAR, 1)
                }
                return target.timeInMillis
            }

            // Find closest repeat day
            var daysToAdd = 0
            while (daysToAdd < 7) {
                val checkCal = (target.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, daysToAdd) }
                val dayOfWeek = checkCal.get(Calendar.DAY_OF_WEEK)
                if (repeatDays.contains(dayOfWeek)) {
                    if (checkCal.timeInMillis > now.timeInMillis) {
                        return checkCal.timeInMillis
                    }
                }
                daysToAdd++
            }

            target.add(Calendar.DAY_OF_YEAR, 7)
            return target.timeInMillis
        }
    }
}
