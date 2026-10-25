package com.liferise.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.liferise.app.domain.model.*

@Entity(
    tableName = "diary_entries",
    indices = [Index(value = ["createdAt"])]
)
data class DiaryEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val mood: Mood,
    val tags: List<String>,
    val imageUris: List<String>,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(
    tableName = "diary_analyses",
    foreignKeys = [
        ForeignKey(
            entity = DiaryEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["entryId"], unique = true)]
)
data class DiaryAnalysisEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entryId: Long,
    val summary: String,
    val achievements: List<String>,
    val difficulties: List<String>,
    val lessonsLearned: List<String>,
    val suggestedImprovements: List<String>,
    val actionForTomorrow: String,
    val isDemoRuleBased: Boolean,
    val analyzedAt: Long
)

@Entity(
    tableName = "tasks",
    indices = [Index(value = ["dueDate"]), Index(value = ["isCompleted"])]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val dueDate: Long,
    val hasReminder: Boolean,
    val isCompleted: Boolean,
    val priority: Priority,
    val recurrence: Recurrence,
    val completedAt: Long?,
    val createdAt: Long
)

@Entity(
    tableName = "daily_missions",
    indices = [Index(value = ["dateIso", "category"], unique = true)]
)
data class DailyMissionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateIso: String,
    val title: String,
    val description: String,
    val category: MissionCategory,
    val isCompleted: Boolean,
    val targetPoints: Int,
    val completedAt: Long?
)

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val label: String,
    val isEnabled: Boolean,
    val repeatDays: Set<Int>,
    val soundUri: String,
    val challengeCategory: AlarmCategory,
    val difficulty: AlarmDifficulty,
    val requiredCorrectAnswers: Int,
    val isSnoozeEnabled: Boolean,
    val snoozeCount: Int
)

@Entity(tableName = "quiz_questions")
data class QuizQuestionEntity(
    @PrimaryKey val id: String,
    val category: AlarmCategory,
    val language: QuizLanguage,
    val questionText: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val difficulty: AlarmDifficulty,
    val explanation: String,
    val sourceOrAttribution: String
)

@Entity(tableName = "quiz_history")
data class QuizHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: AlarmCategory,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val accuracyPercentage: Float,
    val durationSeconds: Long,
    val timestamp: Long
)

@Entity(
    tableName = "daily_progress",
    indices = [Index(value = ["dateIso"], unique = true)]
)
data class DailyProgressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateIso: String,
    val score: Int,
    val tasksCompleted: Int,
    val tasksTotal: Int,
    val missionsCompleted: Int,
    val missionsTotal: Int,
    val diaryEntriesCount: Int,
    val quizAccuracy: Float,
    val quizCompletedCount: Int,
    val recordedAt: Long
)

@Entity(tableName = "cached_posts")
data class CachedPostEntity(
    @PrimaryKey val id: String,
    val authorId: String,
    val authorUsername: String,
    val authorDisplayName: String,
    val authorAvatarUrl: String?,
    val content: String,
    val caption: String,
    val imageUrl: String?,
    val hashtags: List<String>,
    val visibility: PostVisibility,
    val likesCount: Int,
    val commentsCount: Int,
    val isLikedByMe: Boolean,
    val createdAt: Long
)

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val theme: AppTheme,
    val languageCode: String,
    val diaryLockType: DiaryLockType,
    val isAiProcessingConsented: Boolean,
    val taskRemindersEnabled: Boolean,
    val dailyMissionReminderTimeMillis: Long?,
    val soundVolume: Float,
    val vibrateOnAlarm: Boolean
)
