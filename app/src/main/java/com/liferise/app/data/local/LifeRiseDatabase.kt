package com.liferise.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.liferise.app.data.local.dao.*
import com.liferise.app.data.local.entity.*

@Database(
    entities = [
        DiaryEntryEntity::class,
        DiaryAnalysisEntity::class,
        TaskEntity::class,
        DailyMissionEntity::class,
        AlarmEntity::class,
        QuizQuestionEntity::class,
        QuizHistoryEntity::class,
        DailyProgressEntity::class,
        CachedPostEntity::class,
        UserSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LifeRiseDatabase : RoomDatabase() {

    abstract fun diaryDao(): DiaryDao
    abstract fun diaryAnalysisDao(): DiaryAnalysisDao
    abstract fun taskDao(): TaskDao
    abstract fun missionDao(): MissionDao
    abstract fun alarmDao(): AlarmDao
    abstract fun quizDao(): QuizDao
    abstract fun progressDao(): ProgressDao
    abstract fun postDao(): PostDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        private const val DATABASE_NAME = "liferise_database.db"

        @Volatile
        private var INSTANCE: LifeRiseDatabase? = null

        fun getInstance(context: Context): LifeRiseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LifeRiseDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
