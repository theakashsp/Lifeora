package com.liferise.app.domain.model

enum class AppTheme {
    SYSTEM,
    LIGHT,
    DARK
}

enum class DiaryLockType {
    NONE,
    PIN,
    BIOMETRIC
}

data class UserSettings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val languageCode: String = "en",
    val diaryLockType: DiaryLockType = DiaryLockType.NONE,
    val isAiProcessingConsented: Boolean = false,
    val taskRemindersEnabled: Boolean = true,
    val dailyMissionReminderTimeMillis: Long? = null,
    val soundVolume: Float = 0.8f,
    val vibrateOnAlarm: Boolean = true
)
