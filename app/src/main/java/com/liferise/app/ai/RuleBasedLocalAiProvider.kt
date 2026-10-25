package com.liferise.app.ai

import com.liferise.app.core.util.Resource
import com.liferise.app.domain.model.DiaryAnalysis
import com.liferise.app.domain.model.DiaryEntry
import com.liferise.app.domain.model.Mood
import java.util.Locale

/**
 * Local rule-based analyzer that runs completely on-device without contacting any remote server.
 * Clearly labeled as a Demo/Rule-Based mode.
 * Strictly separates explicitly mentioned sentences from inferred constructive suggestions.
 */
class RuleBasedLocalAiProvider : AiAnalysisProvider {

    override val providerName: String = "LifeRise Local Rule-Based Engine (Demo Mode)"
    override val isDemoMode: Boolean = true

    override suspend fun analyzeEntry(entry: DiaryEntry): Resource<DiaryAnalysis> {
        val text = entry.content.trim()
        if (text.length < 15) {
            return Resource.Error("Entry is too brief for analysis. Write at least a couple of sentences to receive meaningful insights.")
        }

        val sentences = text.split(Regex("(?<=[.!?\\n])\\s+"))
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val achievements = mutableListOf<String>()
        val difficulties = mutableListOf<String>()
        val lessons = mutableListOf<String>()
        val suggestions = mutableListOf<String>()

        val achievementKeywords = listOf(
            "finished", "completed", "solved", "achieved", "won", "built", "learned",
            "managed", "great", "proud", "happy", "success", "exercised", "read", "shipped"
        )
        val difficultyKeywords = listOf(
            "struggled", "hard", "tired", "failed", "late", "stressed", "anxious", "missed",
            "confused", "delay", "distracted", "exhausted", "problem", "difficult", "tough"
        )
        val lessonKeywords = listOf(
            "realized", "learned", "understood", "next time", "important", "found out",
            "discovered", "better to", "should have", "remember"
        )

        for (sentence in sentences) {
            val lower = sentence.lowercase(Locale.ROOT)
            if (achievementKeywords.any { lower.contains(it) }) {
                achievements.add("Stated Fact: $sentence")
            }
            if (difficultyKeywords.any { lower.contains(it) }) {
                difficulties.add("Reported Challenge: $sentence")
            }
            if (lessonKeywords.any { lower.contains(it) }) {
                lessons.add("Extracted Lesson: $sentence")
            }
        }

        // Add mood-specific observations
        when (entry.mood) {
            Mood.EXCELLENT, Mood.GOOD -> {
                if (achievements.isEmpty()) {
                    achievements.add("Positive State: Reflected an upbeat mood (${entry.mood.displayName}).")
                }
                suggestions.add("Anchor this momentum: Take note of what contributed to today's positive energy.")
            }
            Mood.STRESSED, Mood.LOW, Mood.TIRED -> {
                if (difficulties.isEmpty()) {
                    difficulties.add("State: Noted feeling ${entry.mood.displayName.lowercase(Locale.ROOT)} during today's reflection.")
                }
                suggestions.add("Recovery Focus: Prioritize 7-8 hours of restful sleep and physical hydration.")
            }
            else -> {
                suggestions.add("Maintain consistency: Balanced pace observed in daily reflections.")
            }
        }

        if (achievements.isEmpty()) {
            achievements.add("Committed to self-reflection by recording thoughts today.")
        }
        if (difficulties.isEmpty()) {
            difficulties.add("No critical friction points or blockers were explicitly mentioned in this entry.")
        }
        if (lessons.isEmpty()) {
            lessons.add("Consistent daily documentation fosters long-term clarity and emotional resilience.")
        }

        // Action for tomorrow derived from observations
        val actionForTomorrow = if (difficulties.any { it.contains("distracted", ignoreCase = true) || it.contains("late", ignoreCase = true) }) {
            "Schedule a 45-minute uninterrupted deep-work block first thing in the morning."
        } else if (entry.mood == Mood.TIRED || entry.mood == Mood.STRESSED) {
            "Take a 15-minute unplugged walk and establish an early wind-down routine tonight."
        } else {
            "Pick the single most impactful task for tomorrow morning and finish it before noon."
        }

        val summary = "On ${entry.title.ifBlank { "this day" }}, reflection highlighted ${achievements.size} milestone(s) with an overall ${entry.mood.displayName.lowercase(Locale.ROOT)} disposition."

        return Resource.Success(
            DiaryAnalysis(
                id = 0,
                entryId = entry.id,
                summary = summary,
                achievements = achievements.take(4),
                difficulties = difficulties.take(4),
                lessonsLearned = lessons.take(3),
                suggestedImprovements = suggestions.take(3),
                actionForTomorrow = actionForTomorrow,
                isDemoRuleBased = true,
                analyzedAt = System.currentTimeMillis()
            )
        )
    }
}
