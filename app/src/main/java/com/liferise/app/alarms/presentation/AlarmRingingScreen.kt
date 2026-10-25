package com.liferise.app.alarms.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liferise.app.R
import com.liferise.app.core.designsystem.*
import com.liferise.app.domain.model.AlarmCategory
import com.liferise.app.domain.model.AlarmDifficulty
import com.liferise.app.domain.model.QuizQuestion
import com.liferise.app.quizzes.QuizEngine
import com.liferise.app.quizzes.ShuffledQuizQuestion

@Composable
fun AlarmRingingScreen(
    alarmLabel: String,
    category: AlarmCategory,
    difficulty: AlarmDifficulty,
    targetCorrectCount: Int,
    questions: List<QuizQuestion>,
    quizEngine: QuizEngine,
    onChallengeCompleted: (correctCount: Int, totalAttempts: Int) -> Unit,
    onEmergencyDismiss: () -> Unit,
    onSnooze: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var correctCount by remember { mutableIntStateOf(0) }
    var totalAttempts by remember { mutableIntStateOf(0) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var isCorrectFeedback by remember { mutableStateOf(false) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }

    val currentRawQuestion = questions.getOrNull(currentIndex % questions.size.coerceAtLeast(1))
    val preparedQuestion = remember(currentRawQuestion, currentIndex) {
        currentRawQuestion?.let { quizEngine.prepareQuestion(it) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Bar
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.alarm_wake_up),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = alarmLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Progress Indicator
                LinearProgressIndicator(
                    progress = { (correctCount.toFloat() / targetCorrectCount.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = PositiveGreen,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Goal: $correctCount / $targetCorrectCount correct answers",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Question Card
            if (preparedQuestion != null) {
                LifeRiseCard(
                    shape = RoundedCornerShape(20.dp),
                    elevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Category: ${category.displayName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = preparedQuestion.original.questionText,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Options A, B, C, D
                    preparedQuestion.shuffledOptions.forEachIndexed { index, optionText ->
                        val optionPrefix = ('A' + index).toString()
                        val isSelected = selectedOptionIndex == index

                        OutlinedButton(
                            onClick = {
                                if (selectedOptionIndex != null) return@OutlinedButton
                                selectedOptionIndex = index
                                totalAttempts++

                                val isCorrect = quizEngine.validateAnswer(preparedQuestion, index)
                                if (isCorrect) {
                                    isCorrectFeedback = true
                                    correctCount++
                                    feedbackMessage = "Correct! Well done."

                                    if (correctCount >= targetCorrectCount) {
                                        onChallengeCompleted(correctCount, totalAttempts)
                                    } else {
                                        // Next question
                                        currentIndex++
                                        selectedOptionIndex = null
                                        feedbackMessage = null
                                    }
                                } else {
                                    isCorrectFeedback = false
                                    val explanation = preparedQuestion.original.explanation.ifBlank { "Try the next question to continue waking up!" }
                                    feedbackMessage = "Incorrect. $explanation"
                                    // Move to next question after wrong answer
                                    currentIndex++
                                    selectedOptionIndex = null
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$optionPrefix.",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = optionText,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    if (feedbackMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = if (isCorrectFeedback) PositiveGreenLight else ErrorRedLight,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isCorrectFeedback) Icons.Default.Check else Icons.Default.Close,
                                    contentDescription = null,
                                    tint = if (isCorrectFeedback) PositiveGreen else ErrorRed
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = feedbackMessage!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isCorrectFeedback) PositiveGreen else ErrorRed
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Emergency Stops & Snooze Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onSnooze,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(stringResource(R.string.action_snooze))
                }

                TextButton(
                    onClick = onEmergencyDismiss,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.alarm_emergency_dismiss),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
