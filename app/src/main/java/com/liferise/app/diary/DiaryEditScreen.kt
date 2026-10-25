package com.liferise.app.diary

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.liferise.app.R
import com.liferise.app.core.designsystem.LifeRiseTopAppBar
import com.liferise.app.core.designsystem.PrivacyBadge
import com.liferise.app.domain.model.Mood

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryEditScreen(
    entryId: Long,
    viewModel: DiaryViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf(Mood.NEUTRAL) }
    var tagsInput by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load existing entry if editing
    LaunchedEffect(entryId) {
        if (entryId > 0) {
            val entry = viewModel.getEntryById(entryId)
            if (entry != null) {
                title = entry.title
                content = entry.content
                selectedMood = entry.mood
                tagsInput = entry.tags.joinToString(", ")
            }
        }
    }

    Scaffold(
        topBar = {
            LifeRiseTopAppBar(
                title = if (entryId > 0) stringResource(R.string.diary_edit_entry) else stringResource(R.string.diary_new_entry),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (content.isBlank()) {
                                errorMessage = "Diary content cannot be empty."
                                return@IconButton
                            }
                            isSaving = true
                            val tagList = tagsInput.split(",")
                                .map { it.trim().removePrefix("#") }
                                .filter { it.isNotBlank() }

                            viewModel.saveEntry(
                                id = if (entryId > 0) entryId else 0L,
                                title = title,
                                content = content,
                                mood = selectedMood,
                                tags = tagList
                            ) {
                                isSaving = false
                                onNavigateBack()
                            }
                        },
                        enabled = !isSaving
                    ) {
                        Icon(Icons.Default.Check, contentDescription = stringResource(R.string.action_save))
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Privacy Assurance Card
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PrivacyBadge()
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Your reflections are private and kept on your device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mood Selector
            Text(
                text = "How are you feeling?",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Mood.values().forEach { mood ->
                    FilterChip(
                        selected = selectedMood == mood,
                        onClick = { selectedMood = mood },
                        label = { Text("${mood.emoji} ${mood.displayName}") },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title Field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title (Optional)") },
                placeholder = { Text("e.g. Breakthroughs and lessons from today") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tags Field
            OutlinedTextField(
                value = tagsInput,
                onValueChange = { tagsInput = it },
                label = { Text("Tags (comma separated)") },
                placeholder = { Text("growth, discipline, work, wellness") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Content Editor
            OutlinedTextField(
                value = content,
                onValueChange = {
                    content = it
                    errorMessage = null
                },
                label = { Text("Reflection & Thoughts *") },
                placeholder = {
                    Text("What went well today? What challenges arose? What will you do differently tomorrow?")
                },
                minLines = 10,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (content.isBlank()) {
                        errorMessage = "Diary content cannot be empty."
                        return@Button
                    }
                    isSaving = true
                    val tagList = tagsInput.split(",")
                        .map { it.trim().removePrefix("#") }
                        .filter { it.isNotBlank() }

                    viewModel.saveEntry(
                        id = if (entryId > 0) entryId else 0L,
                        title = title,
                        content = content,
                        mood = selectedMood,
                        tags = tagList
                    ) {
                        isSaving = false
                        onNavigateBack()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.action_save),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
