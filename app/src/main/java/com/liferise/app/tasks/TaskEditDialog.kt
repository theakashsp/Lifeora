package com.liferise.app.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.liferise.app.R
import com.liferise.app.core.util.DateUtils
import com.liferise.app.domain.model.Priority
import com.liferise.app.domain.model.Recurrence
import com.liferise.app.domain.model.Task
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditDialog(
    taskToEdit: Task?,
    onDismiss: () -> Unit,
    onSave: (id: Long, title: String, description: String, dueDate: Long, hasReminder: Boolean, priority: Priority, recurrence: Recurrence) -> Unit
) {
    var title by remember { mutableStateOf(taskToEdit?.title ?: "") }
    var description by remember { mutableStateOf(taskToEdit?.description ?: "") }
    var dueDate by remember { mutableStateOf(taskToEdit?.dueDate ?: (System.currentTimeMillis() + 3600000L)) }
    var hasReminder by remember { mutableStateOf(taskToEdit?.hasReminder ?: true) }
    var priority by remember { mutableStateOf(taskToEdit?.priority ?: Priority.MEDIUM) }
    var recurrence by remember { mutableStateOf(taskToEdit?.recurrence ?: Recurrence.NONE) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (taskToEdit != null) "Edit Task" else "Create Task",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = null
                    },
                    label = { Text("Task Title *") },
                    placeholder = { Text("e.g. Finish quarterly project brief") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    placeholder = { Text("Key steps or checklist items") },
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Priority Selection
                Text("Priority Level", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Priority.values().forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p.displayName) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                // Due Date Display
                Text("Due: ${DateUtils.formatToDateTimeString(dueDate)}", style = MaterialTheme.typography.bodySmall)

                // Reminder toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Schedule Alarm Reminder", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = hasReminder,
                        onCheckedChange = { hasReminder = it }
                    )
                }

                // Recurrence Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Recurrence.values().forEach { r ->
                        FilterChip(
                            selected = recurrence == r,
                            onClick = { recurrence = r },
                            label = { Text(r.displayName) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        errorMessage = "Task title cannot be empty."
                        return@Button
                    }
                    onSave(
                        taskToEdit?.id ?: 0L,
                        title,
                        description,
                        dueDate,
                        hasReminder,
                        priority,
                        recurrence
                    )
                    onDismiss()
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}
