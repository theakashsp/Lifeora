package com.liferise.app.alarms

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
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
import com.liferise.app.core.designsystem.*
import com.liferise.app.domain.model.AlarmItem
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmListScreen(
    viewModel: AlarmViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val alarms by viewModel.alarms.collectAsState()
    val context = LocalContext.current
    val canScheduleExact = remember { viewModel.canScheduleExact() }

    Scaffold(
        topBar = {
            LifeRiseTopAppBar(
                title = stringResource(R.string.alarm_title),
                subtitle = "Mind-stimulating alarms with math & riddles"
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.alarm_new))
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Android 12+ Exact Alarm Permission Advisory
            if (!canScheduleExact && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Surface(
                    color = WarningAmberLight,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = WarningAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Exact Alarm Permission Needed",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "For alarms to ring with second-precision, allow LifeRise in Alarms & Reminders settings.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        TextButton(
                            onClick = {
                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                context.startActivity(intent)
                            }
                        ) {
                            Text("Enable")
                        }
                    }
                }
            }

            if (alarms.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Alarm,
                    title = "No Challenge Alarms",
                    description = "Wake your brain up with interactive mental math or Kannada riddles before getting out of bed.",
                    actionLabel = stringResource(R.string.alarm_new),
                    onActionClick = onNavigateToCreate,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(alarms, key = { it.id }) { alarm ->
                        AlarmItemCard(
                            alarm = alarm,
                            onToggle = { isEnabled -> viewModel.toggleAlarm(alarm.id, isEnabled) },
                            onClick = { onNavigateToEdit(alarm.id) },
                            onDelete = { viewModel.deleteAlarm(alarm.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AlarmItemCard(
    alarm: AlarmItem,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    LifeRiseCard(
        onClick = onClick,
        elevation = 2.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alarm.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (alarm.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = alarm.formattedTime,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (alarm.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = alarm.challengeCategory.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${alarm.requiredCorrectAnswers} correct needed",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = onToggle
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete alarm",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
