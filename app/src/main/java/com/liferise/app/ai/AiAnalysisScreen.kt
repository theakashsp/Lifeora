package com.liferise.app.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.liferise.app.R
import com.liferise.app.core.designsystem.*
import com.liferise.app.core.util.Resource
import com.liferise.app.domain.model.DiaryAnalysis

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAnalysisScreen(
    entryId: Long,
    viewModel: AiViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val analysisState by viewModel.analysisState.collectAsState()
    val entry by viewModel.entry.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(entryId) {
        viewModel.loadAnalysis(entryId)
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete AI Analysis") },
            text = { Text("Are you sure you want to delete this stored insight? Your original diary entry will not be affected.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteAnalysis(entryId) {
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            LifeRiseTopAppBar(
                title = stringResource(R.string.ai_insights_title),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.reanalyze(entryId) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Re-analyze")
                    }
                    if (analysisState is Resource.Success) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete analysis", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = analysisState) {
                is Resource.Loading -> {
                    LoadingStateView(message = stringResource(R.string.ai_analyzing))
                }
                is Resource.Error -> {
                    ErrorStateView(
                        message = state.message,
                        onRetry = { viewModel.reanalyze(entryId) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is Resource.Success -> {
                    val analysis = state.data
                    AnalysisContent(
                        analysis = analysis,
                        entryTitle = entry?.title ?: "Today's Entry",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun AnalysisContent(
    analysis: DiaryAnalysis,
    entryTitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode Banner
        if (analysis.isDemoRuleBased) {
            Surface(
                color = WarningAmberLight,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = WarningAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.ai_demo_notice),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF5D4037)
                    )
                }
            }
        }

        // Action for Tomorrow Callout (High Priority)
        LifeRiseCard(
            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
            elevation = 2.dp
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.ai_tomorrow_action),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = analysis.actionForTomorrow,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        // Daily Summary Card
        InsightSectionCard(
            title = stringResource(R.string.ai_summary_title),
            icon = Icons.Default.Notes,
            iconTint = IndigoPrimary
        ) {
            Text(
                text = analysis.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Achievements Card
        InsightSectionCard(
            title = stringResource(R.string.ai_achievements_title),
            icon = Icons.Default.CheckCircle,
            iconTint = PositiveGreen
        ) {
            analysis.achievements.forEach { achievement ->
                BulletPoint(text = achievement)
            }
        }

        // Difficulties & Hurdles Card
        InsightSectionCard(
            title = stringResource(R.string.ai_difficulties_title),
            icon = Icons.Default.Warning,
            iconTint = WarningAmber
        ) {
            analysis.difficulties.forEach { difficulty ->
                BulletPoint(text = difficulty)
            }
        }

        // Lessons Learned Card
        InsightSectionCard(
            title = stringResource(R.string.ai_lessons_title),
            icon = Icons.Default.Lightbulb,
            iconTint = AccentGold
        ) {
            analysis.lessonsLearned.forEach { lesson ->
                BulletPoint(text = lesson)
            }
        }

        // Suggested Improvements Card
        InsightSectionCard(
            title = stringResource(R.string.ai_improvements_title),
            icon = Icons.Default.TrendingUp,
            iconTint = TealSecondary
        ) {
            analysis.suggestedImprovements.forEach { improvement ->
                BulletPoint(text = improvement)
            }
        }
    }
}

@Composable
fun InsightSectionCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    LifeRiseCard(elevation = 1.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        content()
    }
}

@Composable
fun BulletPoint(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
