package com.liferise.app.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.liferise.app.data.repository.TaskRepository
import com.liferise.app.domain.model.Priority
import com.liferise.app.domain.model.Recurrence
import com.liferise.app.domain.model.Task
import com.liferise.app.tasks.scheduler.TaskAlarmScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(
    private val repository: TaskRepository,
    private val scheduler: TaskAlarmScheduler
) : ViewModel() {

    val allTasks: StateFlow<List<Task>> = repository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayTasks: StateFlow<List<Task>> = repository.getTodayTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val overdueTasks: StateFlow<List<Task>> = repository.getOverdueTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveTask(
        id: Long = 0,
        title: String,
        description: String,
        dueDate: Long,
        hasReminder: Boolean,
        priority: Priority,
        recurrence: Recurrence = Recurrence.NONE
    ) {
        viewModelScope.launch {
            val task = Task(
                id = id,
                title = title.trim(),
                description = description.trim(),
                dueDate = dueDate,
                hasReminder = hasReminder,
                priority = priority,
                recurrence = recurrence,
                isCompleted = false
            )
            val savedId = if (id == 0L) {
                repository.createTask(task)
            } else {
                repository.updateTask(task)
                id
            }

            val scheduledTask = task.copy(id = savedId)
            if (hasReminder) {
                scheduler.scheduleTaskReminder(scheduledTask)
            } else {
                scheduler.cancelTaskReminder(savedId)
            }
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            val nextState = !task.isCompleted
            repository.toggleTaskCompletion(task.id, nextState)
            if (nextState) {
                scheduler.cancelTaskReminder(task.id)
            } else if (task.hasReminder) {
                scheduler.scheduleTaskReminder(task.copy(isCompleted = false))
            }
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            scheduler.cancelTaskReminder(taskId)
            repository.deleteTask(taskId)
        }
    }

    companion object {
        fun provideFactory(
            repository: TaskRepository,
            scheduler: TaskAlarmScheduler
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TaskViewModel(repository, scheduler) as T
            }
        }
    }
}
