package com.liferise.app.domain.model

enum class Mood(val displayName: String, val emoji: String) {
    EXCELLENT("Excellent", "🌟"),
    GOOD("Good", "😊"),
    PEACEFUL("Peaceful", "🌿"),
    NEUTRAL("Neutral", "😐"),
    TIRED("Tired", "🥱"),
    STRESSED("Stressed", "😰"),
    LOW("Low", "😔")
}

data class DiaryEntry(
    val id: Long = 0,
    val title: String,
    val content: String,
    val mood: Mood = Mood.NEUTRAL,
    val tags: List<String> = emptyList(),
    val imageUris: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class DiaryAnalysis(
    val id: Long = 0,
    val entryId: Long,
    val summary: String,
    val achievements: List<String>,
    val difficulties: List<String>,
    val lessonsLearned: List<String>,
    val suggestedImprovements: List<String>,
    val actionForTomorrow: String,
    val isDemoRuleBased: Boolean = true,
    val analyzedAt: Long = System.currentTimeMillis()
)
