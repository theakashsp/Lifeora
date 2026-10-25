package com.liferise.app.data.repository

import com.liferise.app.data.local.LifeRiseDatabase
import com.liferise.app.data.local.dao.SettingsDao
import com.liferise.app.data.local.entity.UserSettingsEntity
import com.liferise.app.domain.model.AppTheme
import com.liferise.app.domain.model.DiaryLockType
import com.liferise.app.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface SettingsRepository {
    fun getSettingsFlow(): Flow<UserSettings>
    suspend fun getSettings(): UserSettings
    suspend fun updateSettings(settings: UserSettings)
    suspend fun clearAllLocalData()
}

class SettingsRepositoryImpl(
    private val settingsDao: SettingsDao,
    private val database: LifeRiseDatabase
) : SettingsRepository {

    override fun getSettingsFlow(): Flow<UserSettings> {
        return settingsDao.getSettingsFlow().map { entity ->
            entity?.toDomain() ?: UserSettings()
        }
    }

    override suspend fun getSettings(): UserSettings {
        return settingsDao.getSettings()?.toDomain() ?: UserSettings()
    }

    override suspend fun updateSettings(settings: UserSettings) {
        settingsDao.saveSettings(settings.toEntity())
    }

    override suspend fun clearAllLocalData() {
        database.clearAllTables()
    }
}

fun UserSettingsEntity.toDomain() = UserSettings(
    theme = theme,
    languageCode = languageCode,
    diaryLockType = diaryLockType,
    isAiProcessingConsented = isAiProcessingConsented,
    taskRemindersEnabled = taskRemindersEnabled,
    dailyMissionReminderTimeMillis = dailyMissionReminderTimeMillis,
    soundVolume = soundVolume,
    vibrateOnAlarm = vibrateOnAlarm
)

fun UserSettings.toEntity() = UserSettingsEntity(
    id = 1,
    theme = theme,
    languageCode = languageCode,
    diaryLockType = diaryLockType,
    isAiProcessingConsented = isAiProcessingConsented,
    taskRemindersEnabled = taskRemindersEnabled,
    dailyMissionReminderTimeMillis = dailyMissionReminderTimeMillis,
    soundVolume = soundVolume,
    vibrateOnAlarm = vibrateOnAlarm
)
