package com.liferise.app.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.liferise.app.R
import com.liferise.app.core.designsystem.*
import com.liferise.app.core.util.DateUtils
import com.liferise.app.domain.model.Priority
import com.liferise.app.domain.model.Task

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allTasks by viewModel.allTasks.collectAsState()
    val todayTasks by viewModel.todayTasks.collectAsState()
    val overdueTasks by viewModel.overdueTasks.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Today, 1: Overdue, 2: All
    var showCreateDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

    if (showCreateDialog || taskToEdit != null) {
        TaskEditDialog(
            taskToEdit = taskToEdit,
            onDismiss = {
                showCreateDialog = false
                taskToEdit = null
            },
            onSave = { id, title, desc, due, reminder, priority, recurrence ->
                viewModel.saveTask(id, title, desc, due, reminder, priority, recurrence)
            }
        )
    }

    Scaffold(
        topBar = {
            LifeRiseTopAppBar(
                title = stringResource(R.string.tasks_title),
                subtitle = "${todayTasks.count { it.isCompleted }}/${todayTasks.size} completed today",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.tasks_new))
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Today (${todayTasks.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "Overdue (${overdueTasks.size})",
                            color = if (overdueTasks.isNotEmpty()) MaterialTheme.colorScheme.error else Color.Unspecified
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("All (${allTasks.size})") }
                )
            }

            val currentList = when (selectedTab) {
                0 -> todayTasks
                1 -> overdueTasks
                else -> allTasks
            }

            if (currentList.isEmpty()) {
                val emptyDesc = when (selectedTab) {
                    0 -> "No tasks scheduled for today. Create one to power up your daily score!"
                    1 -> "Great job! You have no overdue tasks."
                    else -> "No tasks recorded. Start setting your goals."
                }
                EmptyStateView(
                    icon = Icons.Default.CheckCircle,
                    title = "All Clear",
                    description = emptyDesc,
                    actionLabel = "Add a Task",
                    onActionClick = { showCreateDialog = true },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(currentList, key = { it.id }) { task ->
                        TaskItemRow(
                            task = task,
                            onToggleCompletion = { viewModel.toggleTaskCompletion(task) },
                            onEdit = { taskToEdit = task },
                            onDelete = { viewModel.deleteTask(task.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskItemRow(
    task: Task,
    onToggleCompletion: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val priorityColor = when (task.priority) {
        Priority.HIGH -> ErrorRed
        Priority.MEDIUM -> WarningAmber
        Priority.LOW -> TealSecondary
    }

    LifeRiseCard(
        elevation = 1.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggleCompletion() }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = priorityColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = task.priority.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = priorityColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (task.hasReminder) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = "Reminder set",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Due ${DateUtils.formatToDateTimeString(task.dueDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (!task.isCompleted && DateUtils.isOverdue(task.dueDate)) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit task", modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete task", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
            }
        }
    }
}
