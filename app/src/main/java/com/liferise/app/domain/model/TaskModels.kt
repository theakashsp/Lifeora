package com.liferise.app.domain.model

enum class Priority(val displayName: String) {
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low")
}

enum class Recurrence(val displayName: String) {
    NONE("None"),
    DAILY("Daily"),
    WEEKLY("Weekly")
}

data class Task(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val dueDate: Long, // Epoch millis
    val hasReminder: Boolean = false,
    val isCompleted: Boolean = false,
    val priority: Priority = Priority.MEDIUM,
    val recurrence: Recurrence = Recurrence.NONE,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
