package com.liferise.app.data.local

import androidx.room.TypeConverter
import com.liferise.app.domain.model.*

class Converters {

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return value?.joinToString("|||") ?: ""
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split("|||")
    }

    @TypeConverter
    fun fromIntSet(value: Set<Int>?): String {
        return value?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toIntSet(value: String?): Set<Int> {
        if (value.isNullOrBlank()) return emptySet()
        return value.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
    }

    @TypeConverter
    fun fromMood(value: Mood): String = value.name

    @TypeConverter
    fun toMood(value: String): Mood = try {
        Mood.valueOf(value)
    } catch (_: Exception) {
        Mood.NEUTRAL
    }

    @TypeConverter
    fun fromPriority(value: Priority): String = value.name

    @TypeConverter
    fun toPriority(value: String): Priority = try {
        Priority.valueOf(value)
    } catch (_: Exception) {
        Priority.MEDIUM
    }

    @TypeConverter
    fun fromRecurrence(value: Recurrence): String = value.name

    @TypeConverter
    fun toRecurrence(value: String): Recurrence = try {
        Recurrence.valueOf(value)
    } catch (_: Exception) {
        Recurrence.NONE
    }

    @TypeConverter
    fun fromMissionCategory(value: MissionCategory): String = value.name

    @TypeConverter
    fun toMissionCategory(value: String): MissionCategory = try {
        MissionCategory.valueOf(value)
    } catch (_: Exception) {
        MissionCategory.FOCUS
    }

    @TypeConverter
    fun fromAlarmCategory(value: AlarmCategory): String = value.name

    @TypeConverter
    fun toAlarmCategory(value: String): AlarmCategory = try {
        AlarmCategory.valueOf(value)
    } catch (_: Exception) {
        AlarmCategory.MATH
    }

    @TypeConverter
    fun fromAlarmDifficulty(value: AlarmDifficulty): String = value.name

    @TypeConverter
    fun toAlarmDifficulty(value: String): AlarmDifficulty = try {
        AlarmDifficulty.valueOf(value)
    } catch (_: Exception) {
        AlarmDifficulty.MEDIUM
    }

    @TypeConverter
    fun fromQuizLanguage(value: QuizLanguage): String = value.name

    @TypeConverter
    fun toQuizLanguage(value: String): QuizLanguage = try {
        QuizLanguage.valueOf(value)
    } catch (_: Exception) {
        QuizLanguage.ENGLISH
    }

    @TypeConverter
    fun fromPostVisibility(value: PostVisibility): String = value.name

    @TypeConverter
    fun toPostVisibility(value: String): PostVisibility = try {
        PostVisibility.valueOf(value)
    } catch (_: Exception) {
        PostVisibility.PUBLIC
    }

    @TypeConverter
    fun fromAppTheme(value: AppTheme): String = value.name

    @TypeConverter
    fun toAppTheme(value: String): AppTheme = try {
        AppTheme.valueOf(value)
    } catch (_: Exception) {
        AppTheme.SYSTEM
    }

    @TypeConverter
    fun fromDiaryLockType(value: DiaryLockType): String = value.name

    @TypeConverter
    fun toDiaryLockType(value: String): DiaryLockType = try {
        DiaryLockType.valueOf(value)
    } catch (_: Exception) {
        DiaryLockType.NONE
    }
}
