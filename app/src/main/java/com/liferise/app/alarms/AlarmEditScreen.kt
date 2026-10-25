package com.liferise.app.alarms

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liferise.app.R
import com.liferise.app.core.designsystem.LifeRiseCard
import com.liferise.app.core.designsystem.LifeRiseTopAppBar
import com.liferise.app.domain.model.AlarmCategory
import com.liferise.app.domain.model.AlarmDifficulty
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditScreen(
    alarmId: Long,
    viewModel: AlarmViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hour by remember { mutableIntStateOf(7) }
    var minute by remember { mutableIntStateOf(0) }
    var label by remember { mutableStateOf("Morning Wake Up") }
    var selectedCategory by remember { mutableStateOf(AlarmCategory.MATH) }
    var selectedDifficulty by remember { mutableStateOf(AlarmDifficulty.MEDIUM) }
    var requiredAnswers by remember { mutableIntStateOf(2) }
    var isSnoozeEnabled by remember { mutableStateOf(true) }
    var selectedDays by remember { mutableStateOf(setOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY)) }

    LaunchedEffect(alarmId) {
        if (alarmId > 0) {
            val existing = viewModel.getAlarmById(alarmId)
            if (existing != null) {
                hour = existing.hour
                minute = existing.minute
                label = existing.label
                selectedCategory = existing.challengeCategory
                selectedDifficulty = existing.difficulty
                requiredAnswers = existing.requiredCorrectAnswers
                isSnoozeEnabled = existing.isSnoozeEnabled
                selectedDays = existing.repeatDays
            }
        }
    }

    val formattedTime = remember(hour, minute) {
        val h = if (hour == 0 || hour == 12) 12 else hour % 12
        val m = String.format("%02d", minute)
        val amPm = if (hour < 12) "AM" else "PM"
        "$h:$m $amPm"
    }

    Scaffold(
        topBar = {
            LifeRiseTopAppBar(
                title = if (alarmId > 0) "Edit Alarm" else "New Challenge Alarm",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.saveAlarm(
                                id = if (alarmId > 0) alarmId else 0L,
                                hour = hour,
                                minute = minute,
                                label = label,
                                repeatDays = selectedDays,
                                category = selectedCategory,
                                difficulty = selectedDifficulty,
                                requiredCorrect = requiredAnswers,
                                isSnoozeEnabled = isSnoozeEnabled
                            )
                            onNavigateBack()
                        }
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Big Time Selector Display
            LifeRiseCard(
                onClick = {
                    val dialog = TimePickerDialog(
                        context,
                        { _, selectedH, selectedM ->
                            hour = selectedH
                            minute = selectedM
                        },
                        hour,
                        minute,
                        false
                    )
                    dialog.show()
                },
                elevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Alarm Time (Tap to Change)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formattedTime,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Label
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Alarm Label") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Repeat Days
            Text("Repeat Days", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            val days = listOf(
                Pair(Calendar.MONDAY, "M"),
                Pair(Calendar.TUESDAY, "T"),
                Pair(Calendar.WEDNESDAY, "W"),
                Pair(Calendar.THURSDAY, "T"),
                Pair(Calendar.FRIDAY, "F"),
                Pair(Calendar.SATURDAY, "S"),
                Pair(Calendar.SUNDAY, "S")
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                days.forEach { (calDay, shortLabel) ->
                    val isSelected = selectedDays.contains(calDay)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedDays = if (isSelected) selectedDays - calDay else selectedDays + calDay
                        },
                        label = { Text(shortLabel, fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            // Challenge Category
            Text("Challenge Category", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                AlarmCategory.values().forEach { cat ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedCategory = cat }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(cat.displayName, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            // Difficulty Level
            Text("Challenge Difficulty", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AlarmDifficulty.values().forEach { diff ->
                    FilterChip(
                        selected = selectedDifficulty == diff,
                        onClick = { selectedDifficulty = diff },
                        label = { Text(diff.displayName) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            // Number of Correct Answers Required
            Text("Correct Answers to Dismiss ($requiredAnswers)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Slider(
                value = requiredAnswers.toFloat(),
                onValueChange = { requiredAnswers = it.toInt() },
                valueRange = 1f..5f,
                steps = 3
            )

            // Snooze toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Allow 10-Minute Snooze", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = isSnoozeEnabled, onCheckedChange = { isSnoozeEnabled = it })
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.saveAlarm(
                        id = if (alarmId > 0) alarmId else 0L,
                        hour = hour,
                        minute = minute,
                        label = label,
                        repeatDays = selectedDays,
                        category = selectedCategory,
                        difficulty = selectedDifficulty,
                        requiredCorrect = requiredAnswers,
                        isSnoozeEnabled = isSnoozeEnabled
                    )
                    onNavigateBack()
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.action_save), fontWeight = FontWeight.Bold)
            }
        }
    }
}
