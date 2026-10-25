package com.liferise.app

import com.liferise.app.domain.model.AlarmCategory
import com.liferise.app.domain.model.AlarmDifficulty
import com.liferise.app.domain.model.QuizLanguage
import com.liferise.app.domain.model.QuizQuestion
import com.liferise.app.quizzes.QuestionPacks
import com.liferise.app.quizzes.QuizEngine
import org.junit.Assert.*
import org.junit.Test

class QuizEngineTest {

    private val engine = QuizEngine()

    @Test
    fun testOptionShufflingPreservesCorrectAnswer() {
        val question = QuizQuestion(
            id = "test_01",
            category = AlarmCategory.MATH,
            language = QuizLanguage.ENGLISH,
            questionText = "What is 27 × 4?",
            options = listOf("108", "98", "118", "104"),
            correctOptionIndex = 0,
            difficulty = AlarmDifficulty.EASY
        )

        val prepared = engine.prepareQuestion(question, seed = 42L)

        assertEquals("Total options count must remain 4", 4, prepared.shuffledOptions.size)
        val selectedAnswer = prepared.shuffledOptions[prepared.newCorrectIndex]
        assertEquals("Shuffled index must point to the original correct text", "108", selectedAnswer)

        // Validate answer checks
        assertTrue("Selecting newCorrectIndex should return true", engine.validateAnswer(prepared, prepared.newCorrectIndex))
        val wrongIndex = if (prepared.newCorrectIndex == 0) 1 else 0
        assertFalse("Selecting an incorrect option must return false", engine.validateAnswer(prepared, wrongIndex))
    }

    @Test
    fun testKannadaUnicodeRenderingAndValidation() {
        val kannadaQuestion = QuestionPacks.starterQuestions.first { it.language == QuizLanguage.KANNADA }

        assertNotNull("Kannada question must be present in starter packs", kannadaQuestion)
        assertTrue("Question text must contain authentic Kannada characters", kannadaQuestion.questionText.contains("ಗಾದೆ") || kannadaQuestion.questionText.contains("ಒಗಟು"))

        val prepared = engine.prepareQuestion(kannadaQuestion, seed = 99L)
        val correctText = prepared.shuffledOptions[prepared.newCorrectIndex]

        assertEquals(
            "Target answer in Kannada must match original correct option",
            kannadaQuestion.options[kannadaQuestion.correctOptionIndex],
            correctText
        )
    }

    @Test
    fun testAccuracyAndResultComputation() {
        val result = engine.computeResult(
            category = AlarmCategory.MATH,
            totalQuestions = 4,
            correctAnswers = 3,
            durationSeconds = 25
        )

        assertEquals(75f, result.accuracyPercentage, 0.01f)
        assertEquals(4, result.totalQuestions)
        assertEquals(3, result.correctAnswers)
        assertEquals(25L, result.durationSeconds)
    }
}
