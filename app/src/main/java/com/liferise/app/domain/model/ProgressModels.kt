package com.liferise.app.domain.model

data class DailyProgress(
    val dateIso: String,
    val score: Int, // 0 - 100
    val tasksCompleted: Int,
    val tasksTotal: Int,
    val missionsCompleted: Int,
    val missionsTotal: Int,
    val diaryEntriesCount: Int,
    val quizAccuracy: Float, // 0.0 - 1.0 (tracked separately from productivity)
    val quizCompletedCount: Int,
    val recordedAt: Long = System.currentTimeMillis()
)

data class ProgressBreakdown(
    val overallScore: Int,
    val taskScoreWeighted: Float,       // Max 35 points
    val missionScoreWeighted: Float,    // Max 30 points
    val diaryScoreWeighted: Float,      // Max 20 points
    val reflectionScoreWeighted: Float, // Max 15 points
    val formulaExplanation: String,
    val positiveHighlights: List<String>,
    val areasForImprovement: List<String>
)

data class ProgressDashboardState(
    val todayProgress: DailyProgress,
    val yesterdayProgress: DailyProgress?,
    val scoreDelta: Int, // e.g. +12 or -5
    val sevenDayHistory: List<DailyProgress>,
    val breakdown: ProgressBreakdown
)
