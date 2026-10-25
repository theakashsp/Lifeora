package com.liferise.app.settings

import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.liferise.app.R
import com.liferise.app.core.designsystem.*
import com.liferise.app.domain.model.AppTheme
import com.liferise.app.domain.model.DiaryLockType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()

    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var showEraseDialog by remember { mutableStateOf(false) }

    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Set 4-Digit Diary PIN") },
            text = {
                Column {
                    Text("Enter a 4-digit numeric PIN to protect your daily reflections.")
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinInput = it },
                        label = { Text("PIN") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput.length == 4) {
                            viewModel.updateDiaryLock(DiaryLockType.PIN, pinInput)
                            showPinDialog = false
                            pinInput = ""
                        }
                    },
                    enabled = pinInput.length == 4
                ) {
                    Text("Save PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEraseDialog) {
        AlertDialog(
            onDismissRequest = { showEraseDialog = false },
            title = { Text("Erase All Local Data") },
            text = {
                Text(
                    "Are you completely sure? This will wipe all local diary entries, tasks, missions, and alarm configurations from your device. This action cannot be reversed.",
                    color = MaterialTheme.colorScheme.error
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEraseDialog = false
                        viewModel.eraseAllData {
                            Toast.makeText(context, "All data erased successfully.", Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Erase Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEraseDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            LifeRiseTopAppBar(
                title = stringResource(R.string.settings_title),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Theme Section
            LifeRiseCard(elevation = 1.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.settings_theme), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppTheme.values().forEach { theme ->
                        FilterChip(
                            selected = settings.theme == theme,
                            onClick = { viewModel.updateTheme(theme) },
                            label = { Text(theme.name) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            // Language Section
            LifeRiseCard(elevation = 1.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.settings_language), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = settings.languageCode == "en",
                        onClick = { viewModel.updateLanguage("en") },
                        label = { Text("English") },
                        shape = RoundedCornerShape(8.dp)
                    )
                    FilterChip(
                        selected = settings.languageCode == "kn",
                        onClick = { viewModel.updateLanguage("kn") },
                        label = { Text("ಕನ್ನಡ (Kannada)") },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            // Security & Diary Lock
            LifeRiseCard(elevation = 1.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.settings_diary_lock), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Require authentication before opening private daily reflections.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = settings.diaryLockType == DiaryLockType.NONE,
                        onClick = { viewModel.updateDiaryLock(DiaryLockType.NONE) },
                        label = { Text("None") },
                        shape = RoundedCornerShape(8.dp)
                    )
                    FilterChip(
                        selected = settings.diaryLockType == DiaryLockType.PIN,
                        onClick = { showPinDialog = true },
                        label = { Text("PIN Code") },
                        shape = RoundedCornerShape(8.dp)
                    )
                    if (viewModel.isBiometricAvailable) {
                        FilterChip(
                            selected = settings.diaryLockType == DiaryLockType.BIOMETRIC,
                            onClick = { viewModel.updateDiaryLock(DiaryLockType.BIOMETRIC) },
                            label = { Text("Biometrics") },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            // AI Consent & Privacy
            LifeRiseCard(elevation = 1.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.settings_ai_consent), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Enable AI analysis of your reflections. When disabled, analysis stays strictly on-device in rule-based demo mode.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.isAiProcessingConsented,
                        onCheckedChange = { viewModel.updateAiConsent(it) }
                    )
                }
            }

            // Notification Preferences
            LifeRiseCard(elevation = 1.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Task Reminders", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Show alarm and banner notifications when tasks are due.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = settings.taskRemindersEnabled,
                        onCheckedChange = { viewModel.updateTaskRemindersEnabled(it) }
                    )
                }
            }

            // Data Export & Deletion
            LifeRiseCard(elevation = 1.dp) {
                Text("Data Management", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        viewModel.exportData(context) { file ->
                            Toast.makeText(context, "Data exported to ${file.name}", Toast.LENGTH_LONG).show()
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.settings_export_data))
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { showEraseDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.settings_clear_data), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
