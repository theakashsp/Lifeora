package com.liferise.app.data.repository

import com.liferise.app.ai.AiAnalysisProvider
import com.liferise.app.ai.RuleBasedLocalAiProvider
import com.liferise.app.core.util.Resource
import com.liferise.app.data.local.dao.DiaryAnalysisDao
import com.liferise.app.data.local.entity.DiaryAnalysisEntity
import com.liferise.app.domain.model.DiaryAnalysis
import com.liferise.app.domain.model.DiaryEntry

interface AiDiaryAnalysisRepository {
    suspend fun analyzeEntry(entry: DiaryEntry, userConsented: Boolean, preferRemote: Boolean = false): Resource<DiaryAnalysis>
    suspend fun getCachedAnalysis(entryId: Long): DiaryAnalysis?
    suspend fun saveAnalysis(analysis: DiaryAnalysis): Long
    suspend fun deleteAnalysis(entryId: Long)
}

class AiDiaryAnalysisRepositoryImpl(
    private val analysisDao: DiaryAnalysisDao,
    private val localProvider: AiAnalysisProvider = RuleBasedLocalAiProvider(),
    private val remoteProvider: AiAnalysisProvider? = null
) : AiDiaryAnalysisRepository {

    override suspend fun analyzeEntry(
        entry: DiaryEntry,
        userConsented: Boolean,
        preferRemote: Boolean
    ): Resource<DiaryAnalysis> {
        // If remote AI is preferred, explicit user consent is mandatory
        val provider = if (preferRemote && remoteProvider != null && !remoteProvider.isDemoMode) {
            if (!userConsented) {
                return Resource.Error("User consent is required before transmitting diary data to cloud AI services. Please enable consent in Settings.")
            }
            remoteProvider
        } else {
            localProvider
        }

        val result = provider.analyzeEntry(entry)
        if (result is Resource.Success) {
            saveAnalysis(result.data)
        }
        return result
    }

    override suspend fun getCachedAnalysis(entryId: Long): DiaryAnalysis? {
        return analysisDao.getAnalysisForEntry(entryId)?.toDomain()
    }

    override suspend fun saveAnalysis(analysis: DiaryAnalysis): Long {
        return analysisDao.insertAnalysis(analysis.toEntity())
    }

    override suspend fun deleteAnalysis(entryId: Long) {
        analysisDao.deleteAnalysisForEntry(entryId)
    }
}

fun DiaryAnalysisEntity.toDomain() = DiaryAnalysis(
    id = id,
    entryId = entryId,
    summary = summary,
    achievements = achievements,
    difficulties = difficulties,
    lessonsLearned = lessonsLearned,
    suggestedImprovements = suggestedImprovements,
    actionForTomorrow = actionForTomorrow,
    isDemoRuleBased = isDemoRuleBased,
    analyzedAt = analyzedAt
)

fun DiaryAnalysis.toEntity() = DiaryAnalysisEntity(
    id = id,
    entryId = entryId,
    summary = summary,
    achievements = achievements,
    difficulties = difficulties,
    lessonsLearned = lessonsLearned,
    suggestedImprovements = suggestedImprovements,
    actionForTomorrow = actionForTomorrow,
    isDemoRuleBased = isDemoRuleBased,
    analyzedAt = analyzedAt
)
