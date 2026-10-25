package com.liferise.app.alarms.presentation

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.liferise.app.LifeRiseApplication
import com.liferise.app.alarms.receiver.AlarmReceiver
import com.liferise.app.core.designsystem.LifeRiseTheme
import com.liferise.app.domain.model.AlarmCategory
import com.liferise.app.domain.model.AlarmDifficulty
import com.liferise.app.domain.model.QuizQuestion
import com.liferise.app.domain.model.QuizResult
import kotlinx.coroutines.launch

class AlarmRingingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wakeScreenAndUnlock()

        val appContainer = (application as LifeRiseApplication).container
        val alarmId = intent.getLongExtra(AlarmReceiver.EXTRA_ALARM_ID, -1L)
        val label = intent.getStringExtra(AlarmReceiver.EXTRA_ALARM_LABEL) ?: "Wake Up"
        val categoryStr = intent.getStringExtra(AlarmReceiver.EXTRA_ALARM_CATEGORY) ?: "MATH"
        val difficultyStr = intent.getStringExtra(AlarmReceiver.EXTRA_ALARM_DIFFICULTY) ?: "MEDIUM"
        val requiredCorrect = intent.getIntExtra(AlarmReceiver.EXTRA_ALARM_REQUIRED_CORRECT, 2)

        val category = try { AlarmCategory.valueOf(categoryStr) } catch (_: Exception) { AlarmCategory.MATH }
        val difficulty = try { AlarmDifficulty.valueOf(difficultyStr) } catch (_: Exception) { AlarmDifficulty.MEDIUM }

        setContent {
            LifeRiseTheme {
                var questions by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }

                LaunchedEffect(Unit) {
                    val loaded = appContainer.quizRepository.getQuestionsForChallenge(category, difficulty, 10)
                    questions = loaded
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    if (questions.isNotEmpty()) {
                        val startTime = remember { System.currentTimeMillis() }

                        AlarmRingingScreen(
                            alarmLabel = label,
                            category = category,
                            difficulty = difficulty,
                            targetCorrectCount = requiredCorrect,
                            questions = questions,
                            quizEngine = appContainer.quizEngine,
                            onChallengeCompleted = { correctCount, totalAttempts ->
                                val durationSec = (System.currentTimeMillis() - startTime) / 1000L
                                lifecycleScope.launch {
                                    val result = appContainer.quizEngine.computeResult(
                                        category = category,
                                        totalQuestions = totalAttempts,
                                        correctAnswers = correctCount,
                                        durationSeconds = durationSec
                                    )
                                    appContainer.quizRepository.recordQuizResult(result)

                                    // Stop alarm audio service
                                    val dismissIntent = Intent(this@AlarmRingingActivity, AlarmReceiver::class.java).apply {
                                        action = AlarmReceiver.ACTION_DISMISS_ALARM
                                        putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
                                    }
                                    sendBroadcast(dismissIntent)
                                    finish()
                                }
                            },
                            onEmergencyDismiss = {
                                val dismissIntent = Intent(this@AlarmRingingActivity, AlarmReceiver::class.java).apply {
                                    action = AlarmReceiver.ACTION_DISMISS_ALARM
                                    putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
                                }
                                sendBroadcast(dismissIntent)
                                finish()
                            },
                            onSnooze = {
                                val snoozeIntent = Intent(this@AlarmRingingActivity, AlarmReceiver::class.java).apply {
                                    action = AlarmReceiver.ACTION_SNOOZE_ALARM
                                    putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
                                }
                                sendBroadcast(snoozeIntent)
                                finish()
                            }
                        )
                    }
                }
            }
        }
    }

    private fun wakeScreenAndUnlock() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }
}
