package com.liferise.app.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.liferise.app.core.util.Resource
import com.liferise.app.data.repository.AiDiaryAnalysisRepository
import com.liferise.app.data.repository.DiaryRepository
import com.liferise.app.data.repository.SettingsRepository
import com.liferise.app.domain.model.DiaryAnalysis
import com.liferise.app.domain.model.DiaryEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AiViewModel(
    private val analysisRepository: AiDiaryAnalysisRepository,
    private val diaryRepository: DiaryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _analysisState = MutableStateFlow<Resource<DiaryAnalysis>>(Resource.Loading)
    val analysisState: StateFlow<Resource<DiaryAnalysis>> = _analysisState.asStateFlow()

    private val _entry = MutableStateFlow<DiaryEntry?>(null)
    val entry: StateFlow<DiaryEntry?> = _entry.asStateFlow()

    fun loadAnalysis(entryId: Long) {
        viewModelScope.launch {
            _analysisState.value = Resource.Loading
            val loadedEntry = diaryRepository.getEntryById(entryId)
            _entry.value = loadedEntry

            if (loadedEntry == null) {
                _analysisState.value = Resource.Error("Diary entry not found.")
                return@launch
            }

            // Check cached analysis first
            val cached = analysisRepository.getCachedAnalysis(entryId)
            if (cached != null) {
                _analysisState.value = Resource.Success(cached)
            } else {
                analyze(loadedEntry)
            }
        }
    }

    fun reanalyze(entryId: Long) {
        viewModelScope.launch {
            val loadedEntry = _entry.value ?: diaryRepository.getEntryById(entryId)
            if (loadedEntry != null) {
                analyze(loadedEntry)
            }
        }
    }

    private suspend fun analyze(entry: DiaryEntry) {
        _analysisState.value = Resource.Loading
        val settings = settingsRepository.getSettings()
        val result = analysisRepository.analyzeEntry(
            entry = entry,
            userConsented = settings.isAiProcessingConsented,
            preferRemote = settings.isAiProcessingConsented
        )
        _analysisState.value = result
    }

    fun deleteAnalysis(entryId: Long, onDone: () -> Unit) {
        viewModelScope.launch {
            analysisRepository.deleteAnalysis(entryId)
            _analysisState.value = Resource.Error("Analysis was removed by user.")
            onDone()
        }
    }

    companion object {
        fun provideFactory(
            analysisRepository: AiDiaryAnalysisRepository,
            diaryRepository: DiaryRepository,
            settingsRepository: SettingsRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AiViewModel(analysisRepository, diaryRepository, settingsRepository) as T
            }
        }
    }
}
