package com.liferise.app

import com.liferise.app.domain.model.DailyProgress
import com.liferise.app.progress.ProgressCalculator
import org.junit.Assert.*
import org.junit.Test

class ProgressCalculatorTest {

    @Test
    fun testPerfectScoreCalculation() {
        val breakdown = ProgressCalculator.calculateScore(
            tasksCompleted = 5,
            tasksTotal = 5,
            missionsCompleted = 3,
            missionsTotal = 3,
            diaryEntriesCount = 1,
            hasReflectedToday = true
        )

        assertEquals("Full completion should yield 100 points", 100, breakdown.overallScore)
        assertEquals(35f, breakdown.taskScoreWeighted, 0.01f)
        assertEquals(30f, breakdown.missionScoreWeighted, 0.01f)
        assertEquals(20f, breakdown.diaryScoreWeighted, 0.01f)
        assertEquals(15f, breakdown.reflectionScoreWeighted, 0.01f)
    }

    @Test
    fun testPartialCompletionCalculation() {
        val breakdown = ProgressCalculator.calculateScore(
            tasksCompleted = 2,
            tasksTotal = 4, // 50% tasks -> 17.5 pts
            missionsCompleted = 1,
            missionsTotal = 3, // 33% missions -> 10 pts
            diaryEntriesCount = 0, // 0 pts
            hasReflectedToday = false // 0 pts
        )

        // 17.5 + 10 = 27.5 -> rounded to 28
        assertEquals(28, breakdown.overallScore)
        assertEquals(17.5f, breakdown.taskScoreWeighted, 0.01f)
        assertEquals(10f, breakdown.missionScoreWeighted, 0.01f)
        assertEquals(0f, breakdown.diaryScoreWeighted, 0.01f)
    }

    @Test
    fun testGracefulHandlingWhenNoTasksScheduled() {
        val breakdown = ProgressCalculator.calculateScore(
            tasksCompleted = 0,
            tasksTotal = 0, // No tasks scheduled
            missionsCompleted = 2,
            missionsTotal = 3,
            diaryEntriesCount = 1,
            hasReflectedToday = true
        )

        assertTrue("User should not receive zero when no tasks were planned", breakdown.overallScore > 0)
        assertTrue("Score should remain between 0 and 100", breakdown.overallScore in 0..100)
    }

    @Test
    fun testScoreDeltaCalculation() {
        val today = DailyProgress("2026-10-24", score = 85, 4, 4, 3, 3, 1, 90f, 2)
        val yesterday = DailyProgress("2026-10-23", score = 70, 3, 4, 2, 3, 1, 80f, 1)

        val delta = ProgressCalculator.calculateDelta(today, yesterday)
        assertEquals(15, delta)

        val deltaNullYesterday = ProgressCalculator.calculateDelta(today, null)
        assertEquals(0, deltaNullYesterday)
    }
}
