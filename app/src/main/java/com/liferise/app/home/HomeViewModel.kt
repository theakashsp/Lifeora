package com.liferise.app.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.liferise.app.core.util.DateUtils
import com.liferise.app.data.repository.*
import com.liferise.app.domain.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class HomeDashboardData(
    val todayProgress: DailyProgress,
    val missions: List<DailyMission>,
    val todayTasks: List<Task>,
    val nextAlarm: AlarmItem?,
    val hasWrittenDiaryToday: Boolean,
    val quoteOfTheDay: String
)

class HomeViewModel(
    private val progressRepository: ProgressRepository,
    private val missionRepository: MissionRepository,
    private val taskRepository: TaskRepository,
    private val alarmRepository: AlarmRepository,
    private val diaryRepository: DiaryRepository
) : ViewModel() {

    private val _dashboardData = MutableStateFlow<HomeDashboardData?>(null)
    val dashboardData: StateFlow<HomeDashboardData?> = _dashboardData.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            val progress = progressRepository.calculateAndSaveTodayProgress()
            val todayIso = DateUtils.formatToIsoDate(System.currentTimeMillis())
            val missions = missionRepository.getOrGenerateMissions(todayIso)
            val todayTasks = taskRepository.getTodayTasks().first()
            val alarms = alarmRepository.getAllAlarms().first()
            val nextAlarm = alarms.filter { it.isEnabled }.minByOrNull { it.hour * 60 + it.minute }

            val now = System.currentTimeMillis()
            val start = DateUtils.getStartOfDayMillis(now)
            val end = DateUtils.getEndOfDayMillis(now)
            val diaryCount = diaryRepository.getEntriesCount(start, end)

            val quote = "ನಿನ್ನ ಕನಸುಗಳನ್ನು ನನಸಾಗಿಸಲು ಇಂದೇ ಮೊದಲ ಹೆಜ್ಜೆ ಇಡು. (Every long journey begins with a focused step today.)"

            _dashboardData.value = HomeDashboardData(
                todayProgress = progress,
                missions = missions,
                todayTasks = todayTasks,
                nextAlarm = nextAlarm,
                hasWrittenDiaryToday = diaryCount > 0,
                quoteOfTheDay = quote
            )
        }
    }

    fun toggleMission(mission: DailyMission) {
        viewModelScope.launch {
            missionRepository.toggleMissionCompleted(mission.id, !mission.isCompleted)
            loadDashboard()
        }
    }

    fun replaceMission(missionId: Long) {
        viewModelScope.launch {
            val todayIso = DateUtils.formatToIsoDate(System.currentTimeMillis())
            missionRepository.replaceMission(missionId, todayIso)
            loadDashboard()
        }
    }

    fun toggleTask(task: Task) {
        viewModelScope.launch {
            taskRepository.toggleTaskCompletion(task.id, !task.isCompleted)
            loadDashboard()
        }
    }

    companion object {
        fun provideFactory(
            progressRepository: ProgressRepository,
            missionRepository: MissionRepository,
            taskRepository: TaskRepository,
            alarmRepository: AlarmRepository,
            diaryRepository: DiaryRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(
                    progressRepository,
                    missionRepository,
                    taskRepository,
                    alarmRepository,
                    diaryRepository
                ) as T
            }
        }
    }
}
