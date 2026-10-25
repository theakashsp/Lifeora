package com.liferise.app.data.repository

import com.liferise.app.data.local.dao.QuizDao
import com.liferise.app.data.local.entity.QuizHistoryEntity
import com.liferise.app.data.local.entity.QuizQuestionEntity
import com.liferise.app.domain.model.*
import com.liferise.app.quizzes.QuestionPacks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface QuizRepository {
    suspend fun initializeStarterQuestions()
    suspend fun getQuestionsForChallenge(category: AlarmCategory, difficulty: AlarmDifficulty, count: Int): List<QuizQuestion>
    suspend fun recordQuizResult(result: QuizResult): Long
    fun getRecentHistory(): Flow<List<QuizResult>>
}

class QuizRepositoryImpl(
    private val quizDao: QuizDao
) : QuizRepository {

    override suspend fun initializeStarterQuestions() {
        val existing = quizDao.getAllQuestions()
        if (existing.isEmpty()) {
            val entities = QuestionPacks.starterQuestions.map { it.toEntity() }
            quizDao.insertQuestions(entities)
        }
    }

    override suspend fun getQuestionsForChallenge(
        category: AlarmCategory,
        difficulty: AlarmDifficulty,
        count: Int
    ): List<QuizQuestion> {
        val allQuestions = if (category == AlarmCategory.MIXED) {
            quizDao.getAllQuestions().map { it.toDomain() }
        } else {
            val list = quizDao.getQuestions(category, difficulty).map { it.toDomain() }
            if (list.isEmpty()) {
                quizDao.getQuestionsByCategory(category).map { it.toDomain() }
            } else list
        }

        val pool = if (allQuestions.isEmpty()) {
            QuestionPacks.starterQuestions
        } else {
            allQuestions
        }

        return pool.shuffled().take(count.coerceAtLeast(1))
    }

    override suspend fun recordQuizResult(result: QuizResult): Long {
        val entity = QuizHistoryEntity(
            id = 0,
            category = result.category,
            totalQuestions = result.totalQuestions,
            correctAnswers = result.correctAnswers,
            accuracyPercentage = result.accuracyPercentage,
            durationSeconds = result.durationSeconds,
            timestamp = result.timestamp
        )
        return quizDao.insertQuizHistory(entity)
    }

    override fun getRecentHistory(): Flow<List<QuizResult>> {
        return quizDao.getRecentQuizHistoryFlow().map { entities ->
            entities.map {
                QuizResult(
                    id = it.id,
                    category = it.category,
                    totalQuestions = it.totalQuestions,
                    correctAnswers = it.correctAnswers,
                    accuracyPercentage = it.accuracyPercentage,
                    durationSeconds = it.durationSeconds,
                    timestamp = it.timestamp
                )
            }
        }
    }
}

fun QuizQuestionEntity.toDomain() = QuizQuestion(
    id = id,
    category = category,
    language = language,
    questionText = questionText,
    options = options,
    correctOptionIndex = correctOptionIndex,
    difficulty = difficulty,
    explanation = explanation,
    sourceOrAttribution = sourceOrAttribution
)

fun QuizQuestion.toEntity() = QuizQuestionEntity(
    id = id,
    category = category,
    language = language,
    questionText = questionText,
    options = options,
    correctOptionIndex = correctOptionIndex,
    difficulty = difficulty,
    explanation = explanation,
    sourceOrAttribution = sourceOrAttribution
)
