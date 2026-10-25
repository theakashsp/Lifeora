package com.liferise.app.core.di

import android.content.Context
import com.liferise.app.ai.RemoteBackendAiProvider
import com.liferise.app.ai.RuleBasedLocalAiProvider
import com.liferise.app.alarms.scheduler.AlarmScheduler
import com.liferise.app.core.security.SecurityManager
import com.liferise.app.data.local.LifeRiseDatabase
import com.liferise.app.data.repository.*
import com.liferise.app.quizzes.QuizEngine
import com.liferise.app.tasks.scheduler.TaskAlarmScheduler

interface AppContainer {
    val database: LifeRiseDatabase
    val diaryRepository: DiaryRepository
    val aiDiaryAnalysisRepository: AiDiaryAnalysisRepository
    val taskRepository: TaskRepository
    val missionRepository: MissionRepository
    val alarmRepository: AlarmRepository
    val quizRepository: QuizRepository
    val progressRepository: ProgressRepository
    val socialRepository: SocialRepository
    val settingsRepository: SettingsRepository
    val alarmScheduler: AlarmScheduler
    val taskAlarmScheduler: TaskAlarmScheduler
    val securityManager: SecurityManager
    val quizEngine: QuizEngine
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val database: LifeRiseDatabase by lazy {
        LifeRiseDatabase.getInstance(context)
    }

    override val alarmScheduler: AlarmScheduler by lazy {
        AlarmScheduler(context)
    }

    override val taskAlarmScheduler: TaskAlarmScheduler by lazy {
        TaskAlarmScheduler(context)
    }

    override val securityManager: SecurityManager by lazy {
        SecurityManager(context)
    }

    override val quizEngine: QuizEngine by lazy {
        QuizEngine()
    }

    override val diaryRepository: DiaryRepository by lazy {
        DiaryRepositoryImpl(database.diaryDao())
    }

    override val aiDiaryAnalysisRepository: AiDiaryAnalysisRepository by lazy {
        AiDiaryAnalysisRepositoryImpl(
            analysisDao = database.diaryAnalysisDao(),
            localProvider = RuleBasedLocalAiProvider(),
            remoteProvider = RemoteBackendAiProvider()
        )
    }

    override val taskRepository: TaskRepository by lazy {
        TaskRepositoryImpl(database.taskDao())
    }

    override val missionRepository: MissionRepository by lazy {
        MissionRepositoryImpl(database.missionDao())
    }

    override val alarmRepository: AlarmRepository by lazy {
        AlarmRepositoryImpl(database.alarmDao(), alarmScheduler)
    }

    override val quizRepository: QuizRepository by lazy {
        QuizRepositoryImpl(database.quizDao())
    }

    override val progressRepository: ProgressRepository by lazy {
        ProgressRepositoryImpl(
            progressDao = database.progressDao(),
            taskDao = database.taskDao(),
            missionDao = database.missionDao(),
            diaryDao = database.diaryDao(),
            quizDao = database.quizDao()
        )
    }

    override val socialRepository: SocialRepository by lazy {
        SocialRepositoryImpl(database.postDao())
    }

    override val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(database.settingsDao(), database)
    }
}
