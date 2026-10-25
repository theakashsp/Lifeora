package com.liferise.app.data.repository

import com.liferise.app.core.util.DateUtils
import com.liferise.app.data.local.dao.*
import com.liferise.app.data.local.entity.DailyProgressEntity
import com.liferise.app.domain.model.DailyProgress
import com.liferise.app.domain.model.ProgressDashboardState
import com.liferise.app.progress.ProgressCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface ProgressRepository {
    fun getDashboardStateFlow(): Flow<ProgressDashboardState>
    suspend fun calculateAndSaveTodayProgress(): DailyProgress
    suspend fun getProgressForDate(dateIso: String): DailyProgress?
}

class ProgressRepositoryImpl(
    private val progressDao: ProgressDao,
    private val taskDao: TaskDao,
    private val missionDao: MissionDao,
    private val diaryDao: DiaryDao,
    private val quizDao: QuizDao
) : ProgressRepository {

    override fun getDashboardStateFlow(): Flow<ProgressDashboardState> = flow {
        val todayProgress = calculateAndSaveTodayProgress()

        // Get yesterday ISO date
        val yesterdayMillis = DateUtils.getStartOfDayMillis() - 86400000L
        val yesterdayIso = DateUtils.formatToIsoDate(yesterdayMillis)
        val yesterdayProgress = progressDao.getProgressForDate(yesterdayIso)?.toDomain()

        val historyEntities = progressDao.getPast7DaysProgressList()
        val history = historyEntities.map { it.toDomain() }

        val breakdown = ProgressCalculator.calculateScore(
            tasksCompleted = todayProgress.tasksCompleted,
            tasksTotal = todayProgress.tasksTotal,
            missionsCompleted = todayProgress.missionsCompleted,
            missionsTotal = todayProgress.missionsTotal,
            diaryEntriesCount = todayProgress.diaryEntriesCount
        )

        val delta = ProgressCalculator.calculateDelta(todayProgress, yesterdayProgress)

        emit(
            ProgressDashboardState(
                todayProgress = todayProgress,
                yesterdayProgress = yesterdayProgress,
                scoreDelta = delta,
                sevenDayHistory = history,
                breakdown = breakdown
            )
        )
    }

    override suspend fun calculateAndSaveTodayProgress(): DailyProgress {
        val now = System.currentTimeMillis()
        val todayIso = DateUtils.formatToIsoDate(now)
        val startOfDay = DateUtils.getStartOfDayMillis(now)
        val endOfDay = DateUtils.getEndOfDayMillis(now)

        val tasksCompleted = taskDao.getCompletedTasksCount(startOfDay, endOfDay)
        val tasksTotal = taskDao.getTotalTasksCount(startOfDay, endOfDay)

        val missions = missionDao.getMissionsForDate(todayIso)
        val missionsCompleted = missions.count { it.isCompleted }
        val missionsTotal = if (missions.isNotEmpty()) missions.size else 3

        val diaryCount = diaryDao.getEntriesCount(startOfDay, endOfDay)

        val quizAccuracy = quizDao.getAverageAccuracy(startOfDay, endOfDay) ?: 0f

        val breakdown = ProgressCalculator.calculateScore(
            tasksCompleted = tasksCompleted,
            tasksTotal = tasksTotal,
            missionsCompleted = missionsCompleted,
            missionsTotal = missionsTotal,
            diaryEntriesCount = diaryCount
        )

        val entity = DailyProgressEntity(
            id = 0,
            dateIso = todayIso,
            score = breakdown.overallScore,
            tasksCompleted = tasksCompleted,
            tasksTotal = tasksTotal,
            missionsCompleted = missionsCompleted,
            missionsTotal = missionsTotal,
            diaryEntriesCount = diaryCount,
            quizAccuracy = quizAccuracy,
            quizCompletedCount = if (quizAccuracy > 0f) 1 else 0,
            recordedAt = now
        )

        progressDao.insertOrUpdateProgress(entity)
        return entity.toDomain()
    }

    override suspend fun getProgressForDate(dateIso: String): DailyProgress? {
        return progressDao.getProgressForDate(dateIso)?.toDomain()
    }
}

fun DailyProgressEntity.toDomain() = DailyProgress(
    dateIso = dateIso,
    score = score,
    tasksCompleted = tasksCompleted,
    tasksTotal = tasksTotal,
    missionsCompleted = missionsCompleted,
    missionsTotal = missionsTotal,
    diaryEntriesCount = diaryEntriesCount,
    quizAccuracy = quizAccuracy,
    quizCompletedCount = quizCompletedCount,
    recordedAt = recordedAt
)
