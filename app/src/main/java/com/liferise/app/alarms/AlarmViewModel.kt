package com.liferise.app.alarms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.liferise.app.alarms.scheduler.AlarmScheduler
import com.liferise.app.data.repository.AlarmRepository
import com.liferise.app.domain.model.AlarmCategory
import com.liferise.app.domain.model.AlarmDifficulty
import com.liferise.app.domain.model.AlarmItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlarmViewModel(
    private val repository: AlarmRepository,
    private val scheduler: AlarmScheduler
) : ViewModel() {

    val alarms: StateFlow<List<AlarmItem>> = repository.getAllAlarms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun canScheduleExact(): Boolean = scheduler.canScheduleExact()

    suspend fun getAlarmById(id: Long): AlarmItem? = repository.getAlarmById(id)

    fun saveAlarm(
        id: Long = 0,
        hour: Int,
        minute: Int,
        label: String,
        repeatDays: Set<Int>,
        category: AlarmCategory,
        difficulty: AlarmDifficulty,
        requiredCorrect: Int,
        isSnoozeEnabled: Boolean
    ) {
        viewModelScope.launch {
            val alarm = AlarmItem(
                id = id,
                hour = hour,
                minute = minute,
                label = label.trim().ifBlank { "Wake Up" },
                isEnabled = true,
                repeatDays = repeatDays,
                challengeCategory = category,
                difficulty = difficulty,
                requiredCorrectAnswers = requiredCorrect,
                isSnoozeEnabled = isSnoozeEnabled
            )
            if (id == 0L) {
                repository.createAlarm(alarm)
            } else {
                repository.updateAlarm(alarm)
            }
        }
    }

    fun toggleAlarm(id: Long, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.toggleAlarmEnabled(id, isEnabled)
        }
    }

    fun deleteAlarm(id: Long) {
        viewModelScope.launch {
            repository.deleteAlarm(id)
        }
    }

    companion object {
        fun provideFactory(
            repository: AlarmRepository,
            scheduler: AlarmScheduler
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AlarmViewModel(repository, scheduler) as T
            }
        }
    }
}
