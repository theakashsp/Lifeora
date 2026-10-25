package com.liferise.app.domain.model

enum class AlarmCategory(val displayName: String) {
    MATH("Mathematics"),
    KANNADA("Kannada Riddles & Proverbs"),
    GENERAL_KNOWLEDGE("General Knowledge"),
    MIXED("Mixed Questions")
}

enum class AlarmDifficulty(val displayName: String) {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard")
}

data class AlarmItem(
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val label: String = "Wake Up",
    val isEnabled: Boolean = true,
    val repeatDays: Set<Int> = emptySet(), // Calendar.MONDAY (2), etc.
    val soundUri: String = "",
    val challengeCategory: AlarmCategory = AlarmCategory.MATH,
    val difficulty: AlarmDifficulty = AlarmDifficulty.MEDIUM,
    val requiredCorrectAnswers: Int = 2,
    val isSnoozeEnabled: Boolean = true,
    val snoozeCount: Int = 0
) {
    val formattedTime: String
        get() {
            val h = if (hour == 0 || hour == 12) 12 else hour % 12
            val m = String.format("%02d", minute)
            val amPm = if (hour < 12) "AM" else "PM"
            return "$h:$m $amPm"
        }
}
