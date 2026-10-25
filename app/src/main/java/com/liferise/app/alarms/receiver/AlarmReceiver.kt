package com.liferise.app.alarms.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.liferise.app.alarms.scheduler.AlarmScheduler
import com.liferise.app.alarms.service.AlarmAudioService

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)

        when (action) {
            ACTION_FIRE_ALARM -> {
                val serviceIntent = Intent(context, AlarmAudioService::class.java).apply {
                    putExtras(intent)
                }
                ContextCompat.startForegroundService(context, serviceIntent)
            }
            ACTION_DISMISS_ALARM -> {
                val stopIntent = Intent(context, AlarmAudioService::class.java).apply {
                    this.action = AlarmAudioService.ACTION_STOP_ALARM
                }
                context.startService(stopIntent)

                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.cancel(AlarmAudioService.NOTIFICATION_ID)
            }
            ACTION_SNOOZE_ALARM -> {
                val stopIntent = Intent(context, AlarmAudioService::class.java).apply {
                    this.action = AlarmAudioService.ACTION_STOP_ALARM
                }
                context.startService(stopIntent)

                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.cancel(AlarmAudioService.NOTIFICATION_ID)

                if (alarmId > 0) {
                    AlarmScheduler(context).snoozeAlarm(alarmId, minutes = 10)
                }
            }
        }
    }

    companion object {
        const val ACTION_FIRE_ALARM = "com.liferise.app.ACTION_FIRE_ALARM"
        const val ACTION_DISMISS_ALARM = "com.liferise.app.ACTION_DISMISS_ALARM"
        const val ACTION_SNOOZE_ALARM = "com.liferise.app.ACTION_SNOOZE_ALARM"

        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_ALARM_LABEL = "extra_alarm_label"
        const val EXTRA_ALARM_CATEGORY = "extra_alarm_category"
        const val EXTRA_ALARM_DIFFICULTY = "extra_alarm_difficulty"
        const val EXTRA_ALARM_REQUIRED_CORRECT = "extra_alarm_required_correct"
    }
}
