package com.liferise.app.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.liferise.app.data.repository.ProgressRepository
import com.liferise.app.domain.model.ProgressDashboardState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProgressViewModel(
    private val repository: ProgressRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ProgressDashboardState?>(null)
    val state: StateFlow<ProgressDashboardState?> = _state.asStateFlow()

    init {
        refreshDashboard()
    }

    fun refreshDashboard() {
        viewModelScope.launch {
            repository.getDashboardStateFlow().collect { dashboardState ->
                _state.value = dashboardState
            }
        }
    }

    companion object {
        fun provideFactory(repository: ProgressRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProgressViewModel(repository) as T
                }
            }
    }
}
