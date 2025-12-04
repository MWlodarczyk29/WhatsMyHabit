package com.wlodarczyk.whatsmyhabit.ui.theme.screens

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wlodarczyk.whatsmyhabit.R
import com.wlodarczyk.whatsmyhabit.SettingsActivity
import com.wlodarczyk.whatsmyhabit.model.Habit
import com.wlodarczyk.whatsmyhabit.model.HabitFrequency
import com.wlodarczyk.whatsmyhabit.ui.theme.components.*
import com.wlodarczyk.whatsmyhabit.utils.PermissionManager
import com.wlodarczyk.whatsmyhabit.viewmodel.HabitsViewModel
import kotlinx.coroutines.delay
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: HabitsViewModel,
    permissionManager: PermissionManager
) {
    val context = LocalContext.current
    val habits by viewModel.habits.collectAsState()

    // stan UI
    var recomposeKey by remember { mutableStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var habitToDelete by remember { mutableStateOf<Habit?>(null) }
    var pendingHabitData by remember { mutableStateOf<Triple<String, String, HabitFrequency>?>(null) }

    // stany dialogów uprawnień
    var showNotificationExplanation by remember { mutableStateOf(false) }
    var showAlarmExplanation by remember { mutableStateOf(false) }
    var showNotificationDeniedDialog by remember { mutableStateOf(false) }
    var showAlarmDeniedDialog by remember { mutableStateOf(false) }

    // konfiguracja scroll behavior dla top bar
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    // gradient tła
    val gradientColors = listOf(
        MaterialTheme.colorScheme.surface,
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.surface
    )

    // przeładowanie danych przy starcie
    LaunchedEffect(Unit) {
        viewModel.reloadFromDataStore()
    }

    // automatyczne odświeżanie przy zmianie dnia
    LaunchedEffect(Unit) {
        delay(1000)
        var lastCheckedDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        var lastCheckedYear = Calendar.getInstance().get(Calendar.YEAR)

        while (true) {
            delay(60000) // sprawdzaj co minutę
            val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)

            if (currentDay != lastCheckedDay || currentYear != lastCheckedYear) {
                Log.d("MainScreen", "Wykryto nowy dzień. Przeładowywanie danych...")
                viewModel.reloadFromDataStore()
                lastCheckedDay = currentDay
                lastCheckedYear = currentYear
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MainScreenTopBar(
                scrollBehavior = scrollBehavior,
                onSettingsClick = {
                    context.startActivity(Intent(context, SettingsActivity::class.java))
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.cd_add_habit),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = Brush.verticalGradient(colors = gradientColors))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                HabitStatsHeader(
                    doneCount = viewModel.getDoneCount(),
                    totalCount = viewModel.getTotalActiveTodayCount()
                )

                if (habits.isEmpty()) {
                    EmptyHabitsState()
                } else {
                    HabitsList(
                        habits = habits,
                        recomposeKey = recomposeKey,
                        onHabitCheckedChange = { habit, checked ->
                            viewModel.toggleHabitDone(habit.id, checked)
                        },
                        onHabitDelete = { habit ->
                            habitToDelete = habit
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // dialogi
        MainScreenDialogs(
            showAddDialog = showAddDialog,
            onDismissAddDialog = { showAddDialog = false },
            onConfirmAddDialog = { name, time, frequency ->
                showAddDialog = false
                pendingHabitData = Triple(name, time, frequency)
                handleAddHabitWithPermissions(
                    permissionManager = permissionManager,
                    onShowNotificationExplanation = { showNotificationExplanation = true },
                    onShowAlarmExplanation = { showAlarmExplanation = true },
                    onAddHabit = {
                        viewModel.addHabit(name, time, frequency)
                        Toast.makeText(
                            context,
                            context.getString(R.string.habit_added),
                            Toast.LENGTH_SHORT
                        ).show()
                        pendingHabitData = null
                    }
                )
            },
            habitToDelete = habitToDelete,
            onDismissDeleteDialog = { habitToDelete = null },
            onConfirmDelete = {
                habitToDelete?.let { habit ->
                    viewModel.removeHabit(habit)
                    Toast.makeText(
                        context,
                        context.getString(R.string.habit_deleted),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                habitToDelete = null
            },
            showNotificationExplanation = showNotificationExplanation,
            onDismissNotificationExplanation = {
                showNotificationExplanation = false
                addHabitWithoutReminders(context, viewModel, pendingHabitData)
                pendingHabitData = null
            },
            onConfirmNotificationPermission = {
                showNotificationExplanation = false
                permissionManager.requestNotificationPermission { granted ->
                    if (granted) {
                        if (!permissionManager.hasExactAlarmPermission()) {
                            showAlarmExplanation = true
                        } else {
                            addHabitWithConfirmation(context, viewModel, pendingHabitData)
                            pendingHabitData = null
                        }
                    } else {
                        showNotificationDeniedDialog = true
                    }
                }
            },
            showNotificationDeniedDialog = showNotificationDeniedDialog,
            onDismissNotificationDenied = {
                showNotificationDeniedDialog = false
                addHabitWithoutReminders(context, viewModel, pendingHabitData)
                pendingHabitData = null
            },
            onOpenSettingsFromNotificationDenied = {
                permissionManager.openAppSettings()
                showNotificationDeniedDialog = false
            },
            showAlarmExplanation = showAlarmExplanation,
            onDismissAlarmExplanation = {
                showAlarmExplanation = false
                addHabitWithDelayedReminders(context, viewModel, pendingHabitData)
                pendingHabitData = null
            },
            onConfirmAlarmPermission = {
                showAlarmExplanation = false
                permissionManager.requestExactAlarmPermission { granted ->
                    if (granted) {
                        addHabitWithConfirmation(context, viewModel, pendingHabitData)
                        pendingHabitData = null
                    } else {
                        showAlarmDeniedDialog = true
                    }
                }
            },
            showAlarmDeniedDialog = showAlarmDeniedDialog,
            onDismissAlarmDenied = {
                showAlarmDeniedDialog = false
                addHabitWithDelayedReminders(context, viewModel, pendingHabitData)
                pendingHabitData = null
            },
            onOpenSettingsFromAlarmDenied = {
                permissionManager.openAppSettings()
                showAlarmDeniedDialog = false
            }
        )
    }
}
// top bar ekranu głównego
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreenTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onSettingsClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(stringResource(R.string.my_habits))
            }
        },
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.cd_settings)
                )
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        )
    )
}

private fun handleAddHabitWithPermissions(
    permissionManager: PermissionManager,
    onShowNotificationExplanation: () -> Unit,
    onShowAlarmExplanation: () -> Unit,
    onAddHabit: () -> Unit
) {
    when {
        !permissionManager.hasNotificationPermission() -> onShowNotificationExplanation()
        !permissionManager.hasExactAlarmPermission() -> onShowAlarmExplanation()
        else -> onAddHabit()
    }
}

private fun addHabitWithoutReminders(
    context: android.content.Context,
    viewModel: HabitsViewModel,
    pendingData: Triple<String, String, HabitFrequency>?
) {
    pendingData?.let { (name, time, frequency) ->
        viewModel.addHabit(name, time, frequency)
        Toast.makeText(
            context,
            context.getString(R.string.habit_added_no_reminders),
            Toast.LENGTH_LONG
        ).show()
    }
}

private fun addHabitWithDelayedReminders(
    context: android.content.Context,
    viewModel: HabitsViewModel,
    pendingData: Triple<String, String, HabitFrequency>?
) {
    pendingData?.let { (name, time, frequency) ->
        viewModel.addHabit(name, time, frequency)
        Toast.makeText(
            context,
            context.getString(R.string.habit_added_reminders_delayed),
            Toast.LENGTH_LONG
        ).show()
    }
}

private fun addHabitWithConfirmation(
    context: android.content.Context,
    viewModel: HabitsViewModel,
    pendingData: Triple<String, String, HabitFrequency>?
) {
    pendingData?.let { (name, time, frequency) ->
        viewModel.addHabit(name, time, frequency)
        Toast.makeText(
            context,
            context.getString(R.string.habit_added),
            Toast.LENGTH_SHORT
        ).show()
    }
}