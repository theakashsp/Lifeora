package com.liferise.app.missions.generator

import com.liferise.app.domain.model.DailyMission
import com.liferise.app.domain.model.MissionCategory

object MissionGenerator {

    private val missionPool = listOf(
        // Focus & Productivity
        Triple(
            "Conquer High-Priority Focus",
            "Identify and complete your single most impactful task today.",
            MissionCategory.FOCUS
        ),
        Triple(
            "Zero Inbox / Clean Desk",
            "Spend 15 focused minutes clearing clutter from your workspace or inbox.",
            MissionCategory.FOCUS
        ),
        Triple(
            "Deep Work Sprint",
            "Dedicate 45 minutes of uninterrupted focus without checking phone notifications.",
            MissionCategory.FOCUS
        ),

        // Mindfulness & Diary
        Triple(
            "Reflect & Record",
            "Write a private diary entry summarizing today's key wins and thoughts.",
            MissionCategory.REFLECTION
        ),
        Triple(
            "Gratitude Anchor",
            "Jot down three specific things you are grateful for today.",
            MissionCategory.REFLECTION
        ),
        Triple(
            "Evening Wind-Down",
            "Spend 10 minutes reviewing your day and listing one lesson learned.",
            MissionCategory.REFLECTION
        ),

        // Cognitive Challenge
        Triple(
            "Mental Spark Challenge",
            "Solve a challenge alarm quiz in Math or Kannada riddles.",
            MissionCategory.BRAIN
        ),
        Triple(
            "Curiosity Dive",
            "Learn one new Kannada proverb (ಗಾದೆ) or interesting scientific concept.",
            MissionCategory.BRAIN
        ),
        Triple(
            "Pattern & Logic Drill",
            "Solve 3 mental math problems without using a calculator.",
            MissionCategory.BRAIN
        ),

        // Habit & Wellness
        Triple(
            "Hydration & Movement",
            "Drink at least 2 liters of water and take a 20-minute brisk walk.",
            MissionCategory.WELLNESS
        ),
        Triple(
            "Posture & Stretch Reset",
            "Take two 5-minute stretch breaks during your work sessions.",
            MissionCategory.WELLNESS
        )
    )

    fun generateDefaultMissionsForDate(dateIso: String): List<DailyMission> {
        val focusCandidates = missionPool.filter { it.third == MissionCategory.FOCUS }
        val reflectionCandidates = missionPool.filter { it.third == MissionCategory.REFLECTION }
        val brainCandidates = missionPool.filter { it.third == MissionCategory.BRAIN }

        val hash = dateIso.hashCode().let { if (it < 0) -it else it }

        val focus = focusCandidates[hash % focusCandidates.size]
        val reflection = reflectionCandidates[(hash / 3) % reflectionCandidates.size]
        val brain = brainCandidates[(hash / 7) % brainCandidates.size]

        return listOf(
            DailyMission(
                id = 0,
                dateIso = dateIso,
                title = focus.first,
                description = focus.second,
                category = focus.third,
                isCompleted = false,
                targetPoints = 10
            ),
            DailyMission(
                id = 0,
                dateIso = dateIso,
                title = reflection.first,
                description = reflection.second,
                category = reflection.third,
                isCompleted = false,
                targetPoints = 10
            ),
            DailyMission(
                id = 0,
                dateIso = dateIso,
                title = brain.first,
                description = brain.second,
                category = brain.third,
                isCompleted = false,
                targetPoints = 10
            )
        )
    }

    fun getReplacementMission(dateIso: String, existingTitles: Set<String>, category: MissionCategory): DailyMission {
        val candidates = missionPool.filter { it.third == category && !existingTitles.contains(it.first) }
        val chosen = candidates.randomOrNull() ?: missionPool.first { it.third == category }

        return DailyMission(
            id = 0,
            dateIso = dateIso,
            title = chosen.first,
            description = chosen.second,
            category = chosen.third,
            isCompleted = false,
            targetPoints = 10
        )
    }
}
