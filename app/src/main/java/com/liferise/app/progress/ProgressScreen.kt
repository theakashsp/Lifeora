package com.liferise.app.progress

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liferise.app.R
import com.liferise.app.core.designsystem.*
import com.liferise.app.domain.model.DailyProgress
import com.liferise.app.domain.model.ProgressDashboardState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    var showExplanationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refreshDashboard()
    }

    if (showExplanationDialog && state != null) {
        val breakdown = state!!.breakdown
        AlertDialog(
            onDismissRequest = { showExplanationDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.progress_how_calculated),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = breakdown.formulaExplanation,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tasks Execution (Max 35):", style = MaterialTheme.typography.bodyMedium)
                        Text("${breakdown.taskScoreWeighted.toInt()} pts", fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Daily 3 Missions (Max 30):", style = MaterialTheme.typography.bodyMedium)
                        Text("${breakdown.missionScoreWeighted.toInt()} pts", fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Diary Consistency (Max 20):", style = MaterialTheme.typography.bodyMedium)
                        Text("${breakdown.diaryScoreWeighted.toInt()} pts", fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Mindful Reflection (Max 15):", style = MaterialTheme.typography.bodyMedium)
                        Text("${breakdown.reflectionScoreWeighted.toInt()} pts", fontWeight = FontWeight.Bold)
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Overall Normalized Score:", fontWeight = FontWeight.Bold)
                        Text("${breakdown.overallScore} / 100", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    }

                    Text(
                        text = "* Note: Alarm quiz performance is recorded separately as a cognitive metric so brain teasers do not unfairly penalize productivity.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showExplanationDialog = false }) {
                    Text("Got It")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            LifeRiseTopAppBar(
                title = stringResource(R.string.progress_title),
                subtitle = "Evidence-based growth metrics & historical trends",
                actions = {
                    IconButton(onClick = { viewModel.refreshDashboard() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (state == null) {
            LoadingStateView(modifier = Modifier.padding(innerPadding))
        } else {
            val dashboard = state!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Today's Score Hero Card
                LifeRiseCard(
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    elevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.progress_today_score),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${dashboard.todayProgress.score}",
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "/100",
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                                )
                            }
                        }

                        // Delta badge vs yesterday
                        val delta = dashboard.scoreDelta
                        val deltaColor = if (delta >= 0) PositiveGreen else WarningAmber
                        val deltaText = if (delta > 0) "+$delta vs yesterday" else if (delta < 0) "$delta vs yesterday" else "Equal to yesterday"

                        Surface(
                            color = deltaColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (delta >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    tint = deltaColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = deltaText,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = deltaColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.progress_how_calculated),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { showExplanationDialog = true }
                    )
                }

                // 4 Grid Metric Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Tasks",
                        value = "${dashboard.todayProgress.tasksCompleted}/${dashboard.todayProgress.tasksTotal}",
                        icon = Icons.Default.CheckCircle,
                        iconTint = IndigoPrimary,
                        subtitle = "Target: 100%",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Missions",
                        value = "${dashboard.todayProgress.missionsCompleted}/${dashboard.todayProgress.missionsTotal}",
                        icon = Icons.Default.AutoAwesome,
                        iconTint = PositiveGreen,
                        subtitle = "+30 points",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Diary Reflection",
                        value = if (dashboard.todayProgress.diaryEntriesCount > 0) "Completed" else "Pending",
                        icon = Icons.Default.Book,
                        iconTint = TealSecondary,
                        subtitle = "${dashboard.todayProgress.diaryEntriesCount} entry today",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Quiz Accuracy",
                        value = if (dashboard.todayProgress.quizAccuracy > 0f) "${dashboard.todayProgress.quizAccuracy.toInt()}%" else "Ready",
                        icon = Icons.Default.Psychology,
                        iconTint = AccentPurple,
                        subtitle = "Cognitive Challenge",
                        modifier = Modifier.weight(1f)
                    )
                }

                // 7-Day History Chart
                LifeRiseCard(elevation = 1.dp) {
                    Text(
                        text = stringResource(R.string.progress_7day_trend),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    SevenDayTrendChart(history = dashboard.sevenDayHistory)
                }

                // Positive Highlights
                if (dashboard.breakdown.positiveHighlights.isNotEmpty()) {
                    LifeRiseCard(elevation = 1.dp) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ThumbUp, contentDescription = null, tint = PositiveGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Positive Highlights", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        dashboard.breakdown.positiveHighlights.forEach { highlight ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("✓", color = PositiveGreen, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(highlight, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                // Areas for Improvement
                if (dashboard.breakdown.areasForImprovement.isNotEmpty()) {
                    LifeRiseCard(elevation = 1.dp) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Suggested Areas for Improvement", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        dashboard.breakdown.areasForImprovement.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("→", color = WarningAmber, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(item, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SevenDayTrendChart(history: List<DailyProgress>) {
    val items = if (history.isEmpty()) {
        listOf(DailyProgress("Today", 60, 2, 3, 2, 3, 1, 80f, 1))
    } else history.reversed()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        items.takeLast(7).forEach { item ->
            val heightRatio = (item.score / 100f).coerceIn(0.1f, 1f)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.fillMaxHeight()
            ) {
                Text(
                    text = "${item.score}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(22.dp)
                        .fillMaxHeight(heightRatio)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(
                            if (item.score >= 70) PositiveGreen else MaterialTheme.colorScheme.primary
                        )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.dateIso.takeLast(2),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
