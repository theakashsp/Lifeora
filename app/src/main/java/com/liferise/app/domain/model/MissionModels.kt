package com.liferise.app.domain.model

enum class MissionCategory(val title: String) {
    FOCUS("Focus & Productivity"),
    REFLECTION("Mindfulness & Diary"),
    BRAIN("Cognitive Challenge"),
    WELLNESS("Habit & Wellness")
}

data class DailyMission(
    val id: Long = 0,
    val dateIso: String, // e.g. "2026-10-24"
    val title: String,
    val description: String,
    val category: MissionCategory,
    val isCompleted: Boolean = false,
    val targetPoints: Int = 10,
    val completedAt: Long? = null
)
