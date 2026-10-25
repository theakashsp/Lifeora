package com.liferise.app.progress

import com.liferise.app.domain.model.DailyProgress
import com.liferise.app.domain.model.ProgressBreakdown
import kotlin.math.roundToInt

object ProgressCalculator {

    const val MAX_TASK_POINTS = 35f
    const val MAX_MISSION_POINTS = 30f
    const val MAX_DIARY_POINTS = 20f
    const val MAX_REFLECTION_POINTS = 15f

    /**
     * Calculates the daily progress score based on real user activity.
     * Normalized between 0 and 100.
     * Missing historical data is handled gracefully without penalizing.
     */
    fun calculateScore(
        tasksCompleted: Int,
        tasksTotal: Int,
        missionsCompleted: Int,
        missionsTotal: Int,
        diaryEntriesCount: Int,
        hasReflectedToday: Boolean = diaryEntriesCount > 0
    ): ProgressBreakdown {
        // 1. Task Score (Max 35 points)
        val taskScore = when {
            tasksTotal == 0 && missionsCompleted > 0 -> 25f // Graceful handling when no explicit tasks created
            tasksTotal == 0 -> 0f
            else -> {
                val ratio = (tasksCompleted.toFloat() / tasksTotal.toFloat()).coerceIn(0f, 1f)
                ratio * MAX_TASK_POINTS
            }
        }

        // 2. Daily Mission Score (Max 30 points: 10 points per completed mission, standard 3 missions)
        val missionRatio = if (missionsTotal > 0) {
            (missionsCompleted.toFloat() / missionsTotal.toFloat()).coerceIn(0f, 1f)
        } else 0f
        val missionScore = missionRatio * MAX_MISSION_POINTS

        // 3. Diary Consistency Score (Max 20 points)
        val diaryScore = if (diaryEntriesCount > 0) MAX_DIARY_POINTS else 0f

        // 4. Mindfulness & Habit Reflection Score (Max 15 points)
        val reflectionScore = if (hasReflectedToday) MAX_REFLECTION_POINTS else 0f

        val totalScore = (taskScore + missionScore + diaryScore + reflectionScore).roundToInt().coerceIn(0, 100)

        // Generate dynamic positive highlights and areas for improvement
        val positiveHighlights = mutableListOf<String>()
        val areasForImprovement = mutableListOf<String>()

        if (diaryEntriesCount > 0) {
            positiveHighlights.add("Daily Diary completed: Maintained personal reflection streak (+20 pts).")
        } else {
            areasForImprovement.add("Capture a brief diary entry to unlock up to 35 reflection points.")
        }

        if (missionsCompleted == missionsTotal && missionsTotal > 0) {
            positiveHighlights.add("Perfect Mission Record: All $missionsTotal daily missions conquered (+30 pts).")
        } else if (missionsCompleted > 0) {
            positiveHighlights.add("Completed $missionsCompleted of $missionsTotal daily missions.")
        } else {
            areasForImprovement.add("Check off today's 3 micro-missions to build consistent momentum.")
        }

        if (tasksTotal > 0 && tasksCompleted >= tasksTotal) {
            positiveHighlights.add("Flawless execution: 100% of planned tasks completed (+35 pts).")
        } else if (tasksTotal > 0 && tasksCompleted < tasksTotal) {
            val remaining = tasksTotal - tasksCompleted
            areasForImprovement.add("$remaining task(s) remaining today. Finish them before the day ends.")
        }

        val explanation = "Score is calculated using weighted normalization: Tasks (35%), Daily Missions (30%), Diary Continuity (20%), and Reflection Habit (15%). Total max is 100 points."

        return ProgressBreakdown(
            overallScore = totalScore,
            taskScoreWeighted = taskScore,
            missionScoreWeighted = missionScore,
            diaryScoreWeighted = diaryScore,
            reflectionScoreWeighted = reflectionScore,
            formulaExplanation = explanation,
            positiveHighlights = positiveHighlights,
            areasForImprovement = areasForImprovement
        )
    }

    fun calculateDelta(today: DailyProgress, yesterday: DailyProgress?): Int {
        if (yesterday == null) return 0
        return today.score - yesterday.score
    }
}
