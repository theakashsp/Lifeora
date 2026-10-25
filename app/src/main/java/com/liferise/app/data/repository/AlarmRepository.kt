package com.liferise.app.data.repository

import com.liferise.app.alarms.scheduler.AlarmScheduler
import com.liferise.app.data.local.dao.AlarmDao
import com.liferise.app.data.local.entity.AlarmEntity
import com.liferise.app.domain.model.AlarmCategory
import com.liferise.app.domain.model.AlarmDifficulty
import com.liferise.app.domain.model.AlarmItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface AlarmRepository {
    fun getAllAlarms(): Flow<List<AlarmItem>>
    suspend fun getAlarmById(id: Long): AlarmItem?
    suspend fun createAlarm(alarm: AlarmItem): Long
    suspend fun updateAlarm(alarm: AlarmItem)
    suspend fun deleteAlarm(id: Long)
    suspend fun toggleAlarmEnabled(id: Long, isEnabled: Boolean)
}

class AlarmRepositoryImpl(
    private val alarmDao: AlarmDao,
    private val alarmScheduler: AlarmScheduler
) : AlarmRepository {

    override fun getAllAlarms(): Flow<List<AlarmItem>> {
        return alarmDao.getAllAlarmsFlow().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getAlarmById(id: Long): AlarmItem? {
        return alarmDao.getAlarmById(id)?.toDomain()
    }

    override suspend fun createAlarm(alarm: AlarmItem): Long {
        val id = alarmDao.insertAlarm(alarm.toEntity())
        val saved = alarm.copy(id = id)
        if (saved.isEnabled) {
            alarmScheduler.scheduleAlarm(saved)
        }
        return id
    }

    override suspend fun updateAlarm(alarm: AlarmItem) {
        alarmDao.updateAlarm(alarm.toEntity())
        if (alarm.isEnabled) {
            alarmScheduler.scheduleAlarm(alarm)
        } else {
            alarmScheduler.cancelAlarm(alarm.id)
        }
    }

    override suspend fun deleteAlarm(id: Long) {
        alarmScheduler.cancelAlarm(id)
        alarmDao.deleteAlarmById(id)
    }

    override suspend fun toggleAlarmEnabled(id: Long, isEnabled: Boolean) {
        val current = alarmDao.getAlarmById(id) ?: return
        val updated = current.copy(isEnabled = isEnabled)
        alarmDao.updateAlarm(updated)
        val domain = updated.toDomain()
        if (isEnabled) {
            alarmScheduler.scheduleAlarm(domain)
        } else {
            alarmScheduler.cancelAlarm(id)
        }
    }
}

fun AlarmEntity.toDomain() = AlarmItem(
    id = id,
    hour = hour,
    minute = minute,
    label = label,
    isEnabled = isEnabled,
    repeatDays = repeatDays,
    soundUri = soundUri,
    challengeCategory = challengeCategory,
    difficulty = difficulty,
    requiredCorrectAnswers = requiredCorrectAnswers,
    isSnoozeEnabled = isSnoozeEnabled,
    snoozeCount = snoozeCount
)

fun AlarmItem.toEntity() = AlarmEntity(
    id = id,
    hour = hour,
    minute = minute,
    label = label,
    isEnabled = isEnabled,
    repeatDays = repeatDays,
    soundUri = soundUri,
    challengeCategory = challengeCategory,
    difficulty = difficulty,
    requiredCorrectAnswers = requiredCorrectAnswers,
    isSnoozeEnabled = isSnoozeEnabled,
    snoozeCount = snoozeCount
)
