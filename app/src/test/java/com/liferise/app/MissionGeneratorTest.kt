package com.liferise.app

import com.liferise.app.domain.model.MissionCategory
import com.liferise.app.missions.generator.MissionGenerator
import org.junit.Assert.*
import org.junit.Test

class MissionGeneratorTest {

    @Test
    fun testGenerateThreeDailyMissions() {
        val missions = MissionGenerator.generateDefaultMissionsForDate("2026-10-24")

        assertEquals("Must generate exactly 3 daily missions", 3, missions.size)
        val categories = missions.map { it.category }.toSet()
        assertEquals("Each mission should belong to a distinct category", 3, categories.size)
        assertTrue(categories.contains(MissionCategory.FOCUS))
        assertTrue(categories.contains(MissionCategory.REFLECTION))
        assertTrue(categories.contains(MissionCategory.BRAIN))
    }

    @Test
    fun testMissionReplacement() {
        val dateIso = "2026-10-24"
        val initialMissions = MissionGenerator.generateDefaultMissionsForDate(dateIso)
        val targetMission = initialMissions.first { it.category == MissionCategory.FOCUS }

        val replacement = MissionGenerator.getReplacementMission(
            dateIso = dateIso,
            existingTitles = setOf(targetMission.title),
            category = MissionCategory.FOCUS
        )

        assertNotNull(replacement)
        assertEquals(MissionCategory.FOCUS, replacement.category)
        assertFalse("Replacement title should be different from replaced title", replacement.title == targetMission.title)
    }
}
