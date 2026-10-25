package com.liferise.app.alarms.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.liferise.app.R
import com.liferise.app.alarms.presentation.AlarmRingingActivity
import com.liferise.app.alarms.receiver.AlarmReceiver

class AlarmAudioService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "LifeRise:AlarmWakeLock").apply {
            setReferenceCounted(false)
            acquire(10 * 60 * 1000L) // 10 minutes max safety limit
        }

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP_ALARM) {
            stopSelf()
            return START_NOT_STICKY
        }

        val alarmId = intent?.getLongExtra(EXTRA_ALARM_ID, -1L) ?: -1L
        val label = intent?.getStringExtra(EXTRA_ALARM_LABEL) ?: "LifeRise Challenge Alarm"
        val category = intent?.getStringExtra(EXTRA_ALARM_CATEGORY) ?: "MATH"
        val difficulty = intent?.getStringExtra(EXTRA_ALARM_DIFFICULTY) ?: "MEDIUM"
        val required = intent?.getIntExtra(EXTRA_ALARM_REQUIRED_CORRECT, 2) ?: 2

        startForeground(NOTIFICATION_ID, buildForegroundNotification(alarmId, label, category, difficulty, required))
        playAlarmAudio()
        startVibration()

        return START_STICKY
    }

    private fun playAlarmAudio() {
        if (mediaPlayer == null) {
            try {
                val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

                mediaPlayer = MediaPlayer().apply {
                    setDataSource(applicationContext, alarmUri)
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    isLooping = true
                    prepare()
                    start()
                }
            } catch (_: Exception) {
                // If media player fails, vibration continues
            }
        }
    }

    private fun startVibration() {
        val pattern = longArrayOf(0, 800, 400, 800, 400)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    private fun buildForegroundNotification(
        alarmId: Long,
        label: String,
        category: String,
        difficulty: String,
        requiredCorrect: Int
    ): Notification {
        createNotificationChannel()

        val fullScreenIntent = Intent(this, AlarmRingingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmReceiver.EXTRA_ALARM_LABEL, label)
            putExtra(AlarmReceiver.EXTRA_ALARM_CATEGORY, category)
            putExtra(AlarmReceiver.EXTRA_ALARM_DIFFICULTY, difficulty)
            putExtra(AlarmReceiver.EXTRA_ALARM_REQUIRED_CORRECT, requiredCorrect)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            alarmId.toInt(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Emergency dismiss action directly from notification
        val dismissIntent = Intent(this, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_DISMISS_ALARM
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            this,
            alarmId.toInt(),
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⏰ $label")
            .setContentText("Solve quiz to turn off alarm • Tap to open")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .setOngoing(true)
            .addAction(R.drawable.ic_launcher_foreground, "Emergency Stop", dismissPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarm Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent full-screen challenge alarms"
                setSound(null, null) // Audio handled by MediaPlayer
                enableVibration(false) // Vibration handled by Vibrator
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        vibrator?.cancel()

        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "liferise_alarm_audio_channel"
        const val NOTIFICATION_ID = 9001
        const val ACTION_STOP_ALARM = "com.liferise.app.ACTION_STOP_ALARM"
        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_ALARM_LABEL = "extra_alarm_label"
        const val EXTRA_ALARM_CATEGORY = "extra_alarm_category"
        const val EXTRA_ALARM_DIFFICULTY = "extra_alarm_difficulty"
        const val EXTRA_ALARM_REQUIRED_CORRECT = "extra_alarm_required_correct"
    }
}
