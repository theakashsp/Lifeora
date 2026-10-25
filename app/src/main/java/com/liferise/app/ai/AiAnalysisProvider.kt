package com.liferise.app.ai

import com.liferise.app.core.util.Resource
import com.liferise.app.domain.model.DiaryAnalysis
import com.liferise.app.domain.model.DiaryEntry

interface AiAnalysisProvider {
    val providerName: String
    val isDemoMode: Boolean

    suspend fun analyzeEntry(entry: DiaryEntry): Resource<DiaryAnalysis>
}
