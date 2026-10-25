package com.liferise.app.diary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.liferise.app.R
import com.liferise.app.core.designsystem.EmptyStateView
import com.liferise.app.core.designsystem.LifeRiseCard
import com.liferise.app.core.designsystem.LifeRiseTopAppBar
import com.liferise.app.core.designsystem.PrivacyBadge
import com.liferise.app.core.util.DateUtils
import com.liferise.app.domain.model.DiaryEntry
import com.liferise.app.domain.model.Mood

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryListScreen(
    viewModel: DiaryViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val entries by viewModel.entries.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedMood by viewModel.selectedMoodFilter.collectAsState()

    Scaffold(
        topBar = {
            LifeRiseTopAppBar(
                title = stringResource(R.string.diary_title),
                subtitle = "${entries.size} reflections • Saved securely on device",
                actions = {
                    PrivacyBadge(modifier = Modifier.padding(end = 12.dp))
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.diary_new_entry)
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text(stringResource(R.string.diary_search_hint)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Mood Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedMood == null,
                    onClick = { viewModel.setMoodFilter(null) },
                    label = { Text("All") },
                    shape = RoundedCornerShape(10.dp)
                )
                Mood.values().forEach { mood ->
                    FilterChip(
                        selected = selectedMood == mood,
                        onClick = { viewModel.setMoodFilter(mood) },
                        label = { Text("${mood.emoji} ${mood.displayName}") },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (entries.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Book,
                    title = stringResource(R.string.diary_empty_title),
                    description = stringResource(R.string.diary_empty_desc),
                    actionLabel = stringResource(R.string.diary_new_entry),
                    onActionClick = onNavigateToCreate,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
                ) {
                    items(entries, key = { it.id }) { entry ->
                        DiaryEntryCard(
                            entry = entry,
                            onClick = { onNavigateToDetail(entry.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DiaryEntryCard(
    entry: DiaryEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LifeRiseCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = entry.mood.emoji,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = entry.mood.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = DateUtils.formatToDateString(entry.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = entry.title.ifBlank { "Untitled Reflection" },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = entry.content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )

        if (entry.tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                entry.tags.take(3).forEach { tag ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "#$tag",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
