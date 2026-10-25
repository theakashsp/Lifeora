package com.liferise.app.quizzes

import com.liferise.app.domain.model.AlarmCategory
import com.liferise.app.domain.model.AlarmDifficulty
import com.liferise.app.domain.model.QuizQuestion
import com.liferise.app.domain.model.QuizResult
import java.util.Collections
import java.util.Random

data class ShuffledQuizQuestion(
    val original: QuizQuestion,
    val shuffledOptions: List<String>,
    val newCorrectIndex: Int
)

class QuizEngine {

    /**
     * Shuffles the answer options of a question while preserving the correct answer index.
     */
    fun prepareQuestion(question: QuizQuestion, seed: Long? = null): ShuffledQuizQuestion {
        val originalCorrectText = question.options[question.correctOptionIndex]
        val optionsWithOriginalIndex = question.options.mapIndexed { index, text -> Pair(index, text) }.toMutableList()

        if (seed != null) {
            Collections.shuffle(optionsWithOriginalIndex, Random(seed))
        } else {
            optionsWithOriginalIndex.shuffle()
        }

        val shuffledTexts = optionsWithOriginalIndex.map { it.second }
        val newIndex = shuffledTexts.indexOf(originalCorrectText)

        return ShuffledQuizQuestion(
            original = question,
            shuffledOptions = shuffledTexts,
            newCorrectIndex = newIndex
        )
    }

    /**
     * Verifies if the selected option index matches the correct answer.
     */
    fun validateAnswer(prepared: ShuffledQuizQuestion, selectedOptionIndex: Int): Boolean {
        return selectedOptionIndex == prepared.newCorrectIndex
    }

    /**
     * Computes the final quiz performance result.
     */
    fun computeResult(
        category: AlarmCategory,
        totalQuestions: Int,
        correctAnswers: Int,
        durationSeconds: Long
    ): QuizResult {
        val accuracy = if (totalQuestions > 0) {
            (correctAnswers.toFloat() / totalQuestions.toFloat()) * 100f
        } else 0f

        return QuizResult(
            id = 0,
            category = category,
            totalQuestions = totalQuestions,
            correctAnswers = correctAnswers,
            accuracyPercentage = accuracy,
            durationSeconds = durationSeconds,
            timestamp = System.currentTimeMillis()
        )
    }
}
