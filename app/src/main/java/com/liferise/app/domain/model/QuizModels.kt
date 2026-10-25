package com.liferise.app.domain.model

enum class QuizLanguage(val code: String, val label: String) {
    ENGLISH("en", "English"),
    KANNADA("kn", "ಕನ್ನಡ (Kannada)")
}

data class QuizQuestion(
    val id: String,
    val category: AlarmCategory,
    val language: QuizLanguage,
    val questionText: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val difficulty: AlarmDifficulty = AlarmDifficulty.MEDIUM,
    val explanation: String = "",
    val sourceOrAttribution: String = ""
)

data class QuizResult(
    val id: Long = 0,
    val category: AlarmCategory,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val accuracyPercentage: Float,
    val durationSeconds: Long,
    val timestamp: Long = System.currentTimeMillis()
)
