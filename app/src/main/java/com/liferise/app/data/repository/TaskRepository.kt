package com.liferise.app.data.repository

import com.liferise.app.core.util.DateUtils
import com.liferise.app.data.local.dao.TaskDao
import com.liferise.app.data.local.entity.TaskEntity
import com.liferise.app.domain.model.Priority
import com.liferise.app.domain.model.Recurrence
import com.liferise.app.domain.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface TaskRepository {
    fun getAllTasks(): Flow<List<Task>>
    fun getTodayTasks(): Flow<List<Task>>
    fun getOverdueTasks(): Flow<List<Task>>
    suspend fun getTaskById(id: Long): Task?
    suspend fun createTask(task: Task): Long
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(id: Long)
    suspend fun toggleTaskCompletion(id: Long, isCompleted: Boolean)
    suspend fun getCompletedTasksCount(startMillis: Long, endMillis: Long): Int
    suspend fun getTotalTasksCount(startMillis: Long, endMillis: Long): Int
}

class TaskRepositoryImpl(
    private val taskDao: TaskDao
) : TaskRepository {

    override fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasksFlow().map { list -> list.map { it.toDomain() } }
    }

    override fun getTodayTasks(): Flow<List<Task>> {
        val now = System.currentTimeMillis()
        val start = DateUtils.getStartOfDayMillis(now)
        val end = DateUtils.getEndOfDayMillis(now)
        return taskDao.getTodayTasksFlow(start, end).map { list -> list.map { it.toDomain() } }
    }

    override fun getOverdueTasks(): Flow<List<Task>> {
        return taskDao.getOverdueTasksFlow(System.currentTimeMillis()).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getTaskById(id: Long): Task? {
        return taskDao.getTaskById(id)?.toDomain()
    }

    override suspend fun createTask(task: Task): Long {
        return taskDao.insertTask(task.toEntity())
    }

    override suspend fun updateTask(task: Task) {
        taskDao.updateTask(task.toEntity())
    }

    override suspend fun deleteTask(id: Long) {
        taskDao.deleteTaskById(id)
    }

    override suspend fun toggleTaskCompletion(id: Long, isCompleted: Boolean) {
        val current = taskDao.getTaskById(id) ?: return
        val updated = current.copy(
            isCompleted = isCompleted,
            completedAt = if (isCompleted) System.currentTimeMillis() else null
        )
        taskDao.updateTask(updated)
    }

    override suspend fun getCompletedTasksCount(startMillis: Long, endMillis: Long): Int {
        return taskDao.getCompletedTasksCount(startMillis, endMillis)
    }

    override suspend fun getTotalTasksCount(startMillis: Long, endMillis: Long): Int {
        return taskDao.getTotalTasksCount(startMillis, endMillis)
    }
}

fun TaskEntity.toDomain() = Task(
    id = id,
    title = title,
    description = description,
    dueDate = dueDate,
    hasReminder = hasReminder,
    isCompleted = isCompleted,
    priority = priority,
    recurrence = recurrence,
    completedAt = completedAt,
    createdAt = createdAt
)

fun Task.toEntity() = TaskEntity(
    id = id,
    title = title,
    description = description,
    dueDate = dueDate,
    hasReminder = hasReminder,
    isCompleted = isCompleted,
    priority = priority,
    recurrence = recurrence,
    completedAt = completedAt,
    createdAt = createdAt
)
