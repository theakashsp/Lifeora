package com.liferise.app

import com.liferise.app.ai.RuleBasedLocalAiProvider
import com.liferise.app.core.util.Resource
import com.liferise.app.domain.model.DiaryEntry
import com.liferise.app.domain.model.Mood
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class RuleBasedAiAnalyzerTest {

    private val provider = RuleBasedLocalAiProvider()

    @Test
    fun testShortEntryValidation() = runBlocking {
        val entry = DiaryEntry(
            id = 1,
            title = "Hi",
            content = "Too short",
            mood = Mood.NEUTRAL
        )

        val result = provider.analyzeEntry(entry)
        assertTrue("Analysis should fail when text is under minimum length", result is Resource.Error)
    }

    @Test
    fun testExtractionOfAchievementsAndDifficulties() = runBlocking {
        val entry = DiaryEntry(
            id = 2,
            title = "Productive Day with Obstacles",
            content = "I completed my quarterly report and shipped the updated feature. However, I struggled with afternoon fatigue and felt distracted by notifications. I realized that time-blocking is essential.",
            mood = Mood.GOOD
        )

        val result = provider.analyzeEntry(entry)
        assertTrue("Analysis should succeed for valid reflection text", result is Resource.Success)

        val data = (result as Resource.Success).data
        assertTrue("Demo mode flag must be true for rule-based analysis", data.isDemoRuleBased)
        assertTrue("Must extract at least one explicit achievement", data.achievements.isNotEmpty())
        assertTrue("Must extract at least one difficulty", data.difficulties.isNotEmpty())
        assertTrue("Must extract lesson learned", data.lessonsLearned.isNotEmpty())
        assertNotNull("Must generate tomorrow action plan", data.actionForTomorrow)
        assertTrue("Action plan cannot be blank", data.actionForTomorrow.isNotBlank())
    }
}
