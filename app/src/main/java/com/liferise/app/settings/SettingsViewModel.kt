package com.liferise.app.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.liferise.app.core.security.SecurityManager
import com.liferise.app.data.repository.DiaryRepository
import com.liferise.app.data.repository.SettingsRepository
import com.liferise.app.data.repository.TaskRepository
import com.liferise.app.domain.model.AppTheme
import com.liferise.app.domain.model.DiaryLockType
import com.liferise.app.domain.model.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val diaryRepository: DiaryRepository,
    private val taskRepository: TaskRepository,
    private val securityManager: SecurityManager
) : ViewModel() {

    val settings: StateFlow<UserSettings> = settingsRepository.getSettingsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    val isBiometricAvailable: Boolean = securityManager.canAuthenticateWithBiometrics()

    private val _exportResult = MutableStateFlow<File?>(null)
    val exportResult: StateFlow<File?> = _exportResult.asStateFlow()

    fun updateTheme(theme: AppTheme) {
        viewModelScope.launch {
            val current = settings.value
            settingsRepository.updateSettings(current.copy(theme = theme))
        }
    }

    fun updateLanguage(langCode: String) {
        viewModelScope.launch {
            val current = settings.value
            settingsRepository.updateSettings(current.copy(languageCode = langCode))
        }
    }

    fun updateDiaryLock(lockType: DiaryLockType, pin: String? = null) {
        viewModelScope.launch {
            if (lockType == DiaryLockType.PIN && !pin.isNullOrBlank()) {
                securityManager.savePin(pin)
            } else if (lockType == DiaryLockType.NONE) {
                securityManager.clearPin()
            }
            val current = settings.value
            settingsRepository.updateSettings(current.copy(diaryLockType = lockType))
        }
    }

    fun updateAiConsent(consented: Boolean) {
        viewModelScope.launch {
            val current = settings.value
            settingsRepository.updateSettings(current.copy(isAiProcessingConsented = consented))
        }
    }

    fun updateTaskRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val current = settings.value
            settingsRepository.updateSettings(current.copy(taskRemindersEnabled = enabled))
        }
    }

    fun exportData(context: Context, onComplete: (File) -> Unit) {
        viewModelScope.launch {
            val diaryEntries = diaryRepository.getAllEntries().first()
            val tasks = taskRepository.getAllTasks().first()
            val result = ExportImportManager.exportDataToJson(context, diaryEntries, tasks)
            if (result.isSuccess) {
                val file = result.getOrThrow()
                _exportResult.value = file
                onComplete(file)
            }
        }
    }

    fun eraseAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            securityManager.clearPin()
            settingsRepository.clearAllLocalData()
            onComplete()
        }
    }

    companion object {
        fun provideFactory(
            settingsRepository: SettingsRepository,
            diaryRepository: DiaryRepository,
            taskRepository: TaskRepository,
            securityManager: SecurityManager
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(
                    settingsRepository,
                    diaryRepository,
                    taskRepository,
                    securityManager
                ) as T
            }
        }
    }
}
