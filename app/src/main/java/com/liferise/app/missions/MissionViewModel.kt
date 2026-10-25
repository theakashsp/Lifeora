package com.liferise.app.missions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.liferise.app.core.util.DateUtils
import com.liferise.app.data.repository.MissionRepository
import com.liferise.app.domain.model.DailyMission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MissionViewModel(
    private val repository: MissionRepository
) : ViewModel() {

    private val _missions = MutableStateFlow<List<DailyMission>>(emptyList())
    val missions: StateFlow<List<DailyMission>> = _missions.asStateFlow()

    init {
        loadTodayMissions()
    }

    fun loadTodayMissions() {
        viewModelScope.launch {
            val todayIso = DateUtils.formatToIsoDate(System.currentTimeMillis())
            val list = repository.getOrGenerateMissions(todayIso)
            _missions.value = list
        }
    }

    fun toggleMission(mission: DailyMission) {
        viewModelScope.launch {
            val nextState = !mission.isCompleted
            repository.toggleMissionCompleted(mission.id, nextState)
            loadTodayMissions()
        }
    }

    fun replaceMission(missionId: Long) {
        viewModelScope.launch {
            val todayIso = DateUtils.formatToIsoDate(System.currentTimeMillis())
            repository.replaceMission(missionId, todayIso)
            loadTodayMissions()
        }
    }

    companion object {
        fun provideFactory(repository: MissionRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MissionViewModel(repository) as T
                }
            }
    }
}
