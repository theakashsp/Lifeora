package com.liferise.app.data.local.dao

import androidx.room.*
import com.liferise.app.data.local.entity.*
import com.liferise.app.domain.model.AlarmCategory
import com.liferise.app.domain.model.AlarmDifficulty
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: DiaryEntryEntity): Long

    @Update
    suspend fun updateEntry(entry: DiaryEntryEntity)

    @Delete
    suspend fun deleteEntry(entry: DiaryEntryEntity)

    @Query("DELETE FROM diary_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Long)

    @Query("SELECT * FROM diary_entries WHERE id = :id LIMIT 1")
    suspend fun getEntryById(id: Long): DiaryEntryEntity?

    @Query("SELECT * FROM diary_entries ORDER BY createdAt DESC")
    fun getAllEntriesFlow(): Flow<List<DiaryEntryEntity>>

    @Query("SELECT * FROM diary_entries WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchEntriesFlow(query: String): Flow<List<DiaryEntryEntity>>

    @Query("SELECT * FROM diary_entries WHERE createdAt BETWEEN :startMillis AND :endMillis ORDER BY createdAt DESC")
    suspend fun getEntriesForDateRange(startMillis: Long, endMillis: Long): List<DiaryEntryEntity>

    @Query("SELECT COUNT(*) FROM diary_entries WHERE createdAt BETWEEN :startMillis AND :endMillis")
    suspend fun getEntriesCount(startMillis: Long, endMillis: Long): Int

    @Query("SELECT COUNT(*) FROM diary_entries")
    suspend fun getTotalEntriesCount(): Int
}

@Dao
interface DiaryAnalysisDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalysis(analysis: DiaryAnalysisEntity): Long

    @Query("SELECT * FROM diary_analyses WHERE entryId = :entryId LIMIT 1")
    suspend fun getAnalysisForEntry(entryId: Long): DiaryAnalysisEntity?

    @Query("DELETE FROM diary_analyses WHERE entryId = :entryId")
    suspend fun deleteAnalysisForEntry(entryId: Long)
}

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, dueDate ASC")
    fun getAllTasksFlow(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE dueDate BETWEEN :startMillis AND :endMillis ORDER BY isCompleted ASC, priority DESC")
    fun getTodayTasksFlow(startMillis: Long, endMillis: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE dueDate < :currentMillis AND isCompleted = 0 ORDER BY dueDate ASC")
    fun getOverdueTasksFlow(currentMillis: Long): Flow<List<TaskEntity>>

    @Query("SELECT COUNT(*) FROM tasks WHERE dueDate BETWEEN :startMillis AND :endMillis AND isCompleted = 1")
    suspend fun getCompletedTasksCount(startMillis: Long, endMillis: Long): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE dueDate BETWEEN :startMillis AND :endMillis")
    suspend fun getTotalTasksCount(startMillis: Long, endMillis: Long): Int
}

@Dao
interface MissionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMissions(missions: List<DailyMissionEntity>)

    @Update
    suspend fun updateMission(mission: DailyMissionEntity)

    @Query("SELECT * FROM daily_missions WHERE dateIso = :dateIso")
    fun getMissionsForDateFlow(dateIso: String): Flow<List<DailyMissionEntity>>

    @Query("SELECT * FROM daily_missions WHERE dateIso = :dateIso")
    suspend fun getMissionsForDate(dateIso: String): List<DailyMissionEntity>

    @Query("UPDATE daily_missions SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :missionId")
    suspend fun updateMissionCompletion(missionId: Long, isCompleted: Boolean, completedAt: Long?)
}

@Dao
interface AlarmDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: AlarmEntity): Long

    @Update
    suspend fun updateAlarm(alarm: AlarmEntity)

    @Delete
    suspend fun deleteAlarm(alarm: AlarmEntity)

    @Query("DELETE FROM alarms WHERE id = :id")
    suspend fun deleteAlarmById(id: Long)

    @Query("SELECT * FROM alarms WHERE id = :id LIMIT 1")
    suspend fun getAlarmById(id: Long): AlarmEntity?

    @Query("SELECT * FROM alarms ORDER BY hour ASC, minute ASC")
    fun getAllAlarmsFlow(): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM alarms WHERE isEnabled = 1")
    suspend fun getEnabledAlarms(): List<AlarmEntity>
}

@Dao
interface QuizDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertQuestions(questions: List<QuizQuestionEntity>)

    @Query("SELECT * FROM quiz_questions WHERE category = :category AND difficulty = :difficulty")
    suspend fun getQuestions(category: AlarmCategory, difficulty: AlarmDifficulty): List<QuizQuestionEntity>

    @Query("SELECT * FROM quiz_questions WHERE category = :category")
    suspend fun getQuestionsByCategory(category: AlarmCategory): List<QuizQuestionEntity>

    @Query("SELECT * FROM quiz_questions")
    suspend fun getAllQuestions(): List<QuizQuestionEntity>

    @Insert
    suspend fun insertQuizHistory(history: QuizHistoryEntity): Long

    @Query("SELECT * FROM quiz_history ORDER BY timestamp DESC LIMIT 20")
    fun getRecentQuizHistoryFlow(): Flow<List<QuizHistoryEntity>>

    @Query("SELECT AVG(accuracyPercentage) FROM quiz_history WHERE timestamp BETWEEN :startMillis AND :endMillis")
    suspend fun getAverageAccuracy(startMillis: Long, endMillis: Long): Float?
}

@Dao
interface ProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: DailyProgressEntity)

    @Query("SELECT * FROM daily_progress WHERE dateIso = :dateIso LIMIT 1")
    suspend fun getProgressForDate(dateIso: String): DailyProgressEntity?

    @Query("SELECT * FROM daily_progress ORDER BY dateIso DESC LIMIT 7")
    fun getPast7DaysProgressFlow(): Flow<List<DailyProgressEntity>>

    @Query("SELECT * FROM daily_progress ORDER BY dateIso DESC LIMIT 7")
    suspend fun getPast7DaysProgressList(): List<DailyProgressEntity>
}

@Dao
interface PostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<CachedPostEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: CachedPostEntity)

    @Delete
    suspend fun deletePost(post: CachedPostEntity)

    @Query("DELETE FROM cached_posts WHERE id = :id")
    suspend fun deletePostById(id: String)

    @Query("SELECT * FROM cached_posts ORDER BY createdAt DESC")
    fun getAllPostsFlow(): Flow<List<CachedPostEntity>>

    @Query("DELETE FROM cached_posts")
    suspend fun clearAll()
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): UserSettingsEntity?

    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<UserSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: UserSettingsEntity)
}
