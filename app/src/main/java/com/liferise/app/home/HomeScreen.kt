package com.liferise.app.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liferise.app.R
import com.liferise.app.core.designsystem.*
import com.liferise.app.missions.DailyMissionsView
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToDiary: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToAlarms: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dashboardData by viewModel.dashboardData.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadDashboard()
    }

    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 4..11 -> "Good Morning 🌅"
            in 12..16 -> "Good Afternoon ☀️"
            in 17..21 -> "Good Evening 🌆"
            else -> "Night Reflection 🌙"
        }
    }

    Scaffold(
        topBar = {
            LifeRiseTopAppBar(
                title = stringResource(R.string.app_name),
                subtitle = "Rise to your potential",
                actions = {
                    IconButton(onClick = onNavigateToTasks) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Tasks")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (dashboardData == null) {
            LoadingStateView(modifier = Modifier.padding(innerPadding))
        } else {
            val data = dashboardData!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Greeting & Today's Score Hero Card
                LifeRiseCard(
                    onClick = onNavigateToProgress,
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    elevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = greeting,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Your daily momentum is building.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap to view detailed breakdown →",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Score Circle
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${data.todayProgress.score}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Score",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Daily 3 Missions Card
                DailyMissionsView(
                    missions = data.missions,
                    onToggle = { viewModel.toggleMission(it) },
                    onReplace = { viewModel.replaceMission(it) }
                )

                // Today's Priority Tasks
                LifeRiseCard(elevation = 1.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Tasks (${data.todayTasks.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Manage Tasks →",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { onNavigateToTasks() }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (data.todayTasks.isEmpty()) {
                        Text(
                            text = "No tasks due today. Add high-priority focus goals to elevate your progress.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        data.todayTasks.take(3).forEach { task ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = task.isCompleted,
                                    onCheckedChange = { viewModel.toggleTask(task) }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Daily Diary Call to Action
                LifeRiseCard(
                    onClick = onNavigateToDiary,
                    elevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(TealSecondary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Book, contentDescription = null, tint = TealSecondary)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (data.hasWrittenDiaryToday) "Today's Diary Recorded" else "Daily Reflection",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (data.hasWrittenDiaryToday) "Tap to review or view AI insights" else "Write today's thoughts in your private offline diary",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Next Scheduled Challenge Alarm
                LifeRiseCard(
                    onClick = onNavigateToAlarms,
                    elevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(IndigoPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Alarm, contentDescription = null, tint = IndigoPrimary)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Challenge Alarm",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (data.nextAlarm != null) "Next: ${data.nextAlarm.formattedTime} (${data.nextAlarm.challengeCategory.displayName})" else "No alarms enabled • Tap to create",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Kannada Inspiration Quote Card
                LifeRiseCard(
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    elevation = 0.dp
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FormatQuote, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Quote of the Day", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = data.quoteOfTheDay,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
