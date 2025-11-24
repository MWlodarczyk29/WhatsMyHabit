package com.wlodarczyk.whatsmyhabit.ui.theme.screens

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.wlodarczyk.whatsmyhabit.SettingsActivity
import com.wlodarczyk.whatsmyhabit.db.SettingsDataStore
import com.wlodarczyk.whatsmyhabit.model.HabitFrequency
import com.wlodarczyk.whatsmyhabit.ui.theme.components.AddHabitDialog
import com.wlodarczyk.whatsmyhabit.ui.theme.components.HabitCard
import com.wlodarczyk.whatsmyhabit.ui.theme.components.HabitStatsHeader
import com.wlodarczyk.whatsmyhabit.utils.PermissionManager
import com.wlodarczyk.whatsmyhabit.viewmodel.HabitsViewModel
import kotlinx.coroutines.delay
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    viewModel: HabitsViewModel,
    permissionManager: PermissionManager
) {
    val context = LocalContext.current

    val habits by viewModel.habits.collectAsState()
    val languagePreference by SettingsDataStore.getLanguagePreference(context).collectAsState(initial = "PL")
    val isEnglish = languagePreference == "EN"

    var recomposeKey by remember { mutableStateOf(0) }

    var showDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var habitToDelete by remember { mutableStateOf<com.wlodarczyk.whatsmyhabit.model.Habit?>(null) }

    var showNotificationExplanation by remember { mutableStateOf(false) }
    var showAlarmExplanation by remember { mutableStateOf(false) }
    var showNotificationDeniedDialog by remember { mutableStateOf(false) }
    var showAlarmDeniedDialog by remember { mutableStateOf(false) }

    var pendingHabitData by remember { mutableStateOf<Triple<String, String, HabitFrequency>?>(null) }

    val allHabits = remember(habits, recomposeKey) {
        habits
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    LaunchedEffect(Unit) {
        viewModel.reloadFromDataStore()
    }

    LaunchedEffect(Unit) {
        delay(1000)
        var lastCheckedDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        var lastCheckedYear = Calendar.getInstance().get(Calendar.YEAR)

        while (true) {
            delay(60000)
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
            CenterAlignedTopAppBar(
                title = { Text(if (isEnglish) "My Habits" else "Moje nawyki") },
                actions = {
                    IconButton(onClick = {
                        context.startActivity(Intent(context, SettingsActivity::class.java))
                    }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = if (isEnglish) "Settings" else "Ustawienia"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = if (isEnglish) "Add habit" else "Dodaj nawyk")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HabitStatsHeader(
                doneCount = viewModel.getDoneCount(),
                totalCount = viewModel.getTotalActiveTodayCount(),
                isEnglish = isEnglish
            )

            if (allHabits.isEmpty()) {
                Text(
                    text = if (isEnglish) "No habits for today. Add a new habit!" else "Brak nawyków na dziś. Dodaj nowy nawyk!",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
            ) {
                items(allHabits, key = { habit -> "${habit.id}-$recomposeKey" }) { habit -> // ⬅️ Unikalny key!
                    HabitCard(
                        habit = habit,
                        isEnglish = isEnglish,
                        onCheckedChange = { checked ->
                            viewModel.toggleHabitDone(habit.id, checked)
                        },
                        onDelete = {
                            habitToDelete = habit
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }

        // DIALOG POTWIERDZENIA USUNIĘCIA
        if (showDeleteDialog && habitToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    habitToDelete = null
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                title = {
                    Text(if (isEnglish) "Delete habit?" else "Usunąć nawyk?")
                },
                text = {
                    Text(
                        if (isEnglish)
                            "Are you sure you want to delete \"${habitToDelete?.name}\"? This action cannot be undone."
                        else
                            "Czy na pewno chcesz usunąć \"${habitToDelete?.name}\"? Tej operacji nie można cofnąć."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            habitToDelete?.let { habit ->
                                viewModel.removeHabit(habit)
                                Toast.makeText(
                                    context,
                                    if (isEnglish) "Habit deleted" else "Usunięto nawyk",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            showDeleteDialog = false
                            habitToDelete = null
                        }
                    ) {
                        Text(if (isEnglish) "Delete" else "Usuń", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        habitToDelete = null
                    }) {
                        Text(if (isEnglish) "Cancel" else "Anuluj")
                    }
                }
            )
        }

        if (showDialog) {
            AddHabitDialog(
                isEnglish = isEnglish,
                onDismiss = { showDialog = false },
                onConfirm = { name, time, frequency ->
                    showDialog = false
                    pendingHabitData = Triple(name, time, frequency)

                    val addHabit = {
                        viewModel.addHabit(name, time, frequency)
                        Toast.makeText(
                            context,
                            if (isEnglish) "Habit added" else "Dodano nawyk",
                            Toast.LENGTH_SHORT
                        ).show()
                        pendingHabitData = null
                    }

                    if (!permissionManager.hasNotificationPermission()) {
                        showNotificationExplanation = true
                    } else if (!permissionManager.hasExactAlarmPermission()) {
                        showAlarmExplanation = true
                    } else {
                        addHabit()
                    }
                }
            )
        }

        if (showNotificationExplanation) {
            AlertDialog(
                onDismissRequest = {
                    showNotificationExplanation = false
                    pendingHabitData?.let { (name, time, frequency) ->
                        viewModel.addHabit(name, time, frequency)
                        Toast.makeText(
                            context,
                            if (isEnglish) "Habit added without reminders" else "Nawyk dodany bez przypomnień",
                            Toast.LENGTH_LONG
                        ).show()
                        pendingHabitData = null
                    }
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                title = {
                    Text(if (isEnglish) "Habit Reminders" else "Przypomnienia o nawykach")
                },
                text = {
                    Column {
                        Text(
                            text = if (isEnglish)
                                "To send you reminders about habits at the selected time, notification permission is required."
                            else
                                "Aby aplikacja mogła wysyłać Ci przypomnienia o nawykach o wybranej godzinie, potrzebne jest uprawnienie do powiadomień.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = if (isEnglish)
                                "\n✓ You'll receive exact notifications at habit time\n✓ You won't miss any habit\n✓ Increase your chances of success"
                            else
                                "\n✓ Otrzymasz powiadomienie o czasie nawyku\n✓ Nie przegapisz żadnego z nawyków\n✓ Zwiększysz szansę na swój sukces",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showNotificationExplanation = false
                            permissionManager.requestNotificationPermission { granted ->
                                if (granted) {
                                    if (!permissionManager.hasExactAlarmPermission()) {
                                        showAlarmExplanation = true
                                    } else {
                                        pendingHabitData?.let { (name, time, frequency) ->
                                            viewModel.addHabit(name, time, frequency)
                                            Toast.makeText(
                                                context,
                                                if (isEnglish) "Habit added" else "Dodano nawyk",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            pendingHabitData = null
                                        }
                                    }
                                } else {
                                    showNotificationDeniedDialog = true
                                }
                            }
                        }
                    ) {
                        Text(if (isEnglish) "Grant permission" else "Przyznaj uprawnienie")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showNotificationExplanation = false
                            pendingHabitData?.let { (name, time, frequency) ->
                                viewModel.addHabit(name, time, frequency)
                                Toast.makeText(
                                    context,
                                    if (isEnglish) "Habit added without reminders" else "Nawyk dodany bez przypomnień",
                                    Toast.LENGTH_LONG
                                ).show()
                                pendingHabitData = null
                            }
                        }
                    ) {
                        Text(if (isEnglish) "Skip" else "Pomiń")
                    }
                }
            )
        }

        if (showNotificationDeniedDialog) {
            AlertDialog(
                onDismissRequest = {
                    showNotificationDeniedDialog = false
                    pendingHabitData?.let { (name, time, frequency) ->
                        viewModel.addHabit(name, time, frequency)
                        pendingHabitData = null
                    }
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                title = {
                    Text(if (isEnglish) "No notification permission" else "Brak uprawnień do powiadomień")
                },
                text = {
                    Text(
                        if (isEnglish)
                            "Without notification permission, the app won't be able to send habit reminders.\n\n⚠️ This action results:\n• You won't receive notifications at habit time\n• You must remember your habits yourself\n• This may reduce habit-building effectiveness\n\nYou can enable notifications later in app system settings."
                        else
                            "Bez uprawnienia do powiadomień aplikacja nie będzie mogła wysyłać przypomnień o nawykach.\n\n⚠️ Konsekwencje:\n• Nie otrzymasz powiadomień o czasie nawyku\n• Musisz sam pamiętać o swoich nawykach\n• Może to obniżyć skuteczność budowania nawyków\n\nMożesz włączyć powiadomienia później w ustawieniach systemowych aplikacji."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            permissionManager.openAppSettings()
                            showNotificationDeniedDialog = false
                        }
                    ) {
                        Text(if (isEnglish) "Open settings" else "Otwórz ustawienia")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showNotificationDeniedDialog = false
                            pendingHabitData?.let { (name, time, frequency) ->
                                viewModel.addHabit(name, time, frequency)
                                Toast.makeText(
                                    context,
                                    if (isEnglish) "Habit added without reminders" else "Nawyk dodany bez przypomnień",
                                    Toast.LENGTH_LONG
                                ).show()
                                pendingHabitData = null
                            }
                        }
                    ) {
                        Text(if (isEnglish) "Add anyway" else "Dodaj mimo to")
                    }
                }
            )
        }

        if (showAlarmExplanation) {
            AlertDialog(
                onDismissRequest = {
                    showAlarmExplanation = false
                    pendingHabitData?.let { (name, time, frequency) ->
                        viewModel.addHabit(name, time, frequency)
                        Toast.makeText(
                            context,
                            if (isEnglish) "Habit added (reminders may be delayed)" else "Nawyk dodany (przypomnienia mogą być opóźnione)",
                            Toast.LENGTH_LONG
                        ).show()
                        pendingHabitData = null
                    }
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                title = {
                    Text(if (isEnglish) "Exact Reminders" else "Dokładne przypomnienia")
                },
                text = {
                    Column {
                        Text(
                            text = if (isEnglish)
                                "For reminders to appear exactly at the selected time, additional permission is required."
                            else
                                "Aby przypomnienia pojawiały się dokładnie o wybranej godzinie, potrzebne jest dodatkowe uprawnienie.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = if (isEnglish)
                                "\n✓ Reminders at exact time\n✓ No notification delays\n✓ Better consistency in habits"
                            else
                                "\n✓ Przypomnienia o dokładnej godzinie\n✓ Brak opóźnień w powiadomieniach\n✓ Lepsza konsekwencja w nawykach",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isEnglish)
                                "\nWithout this permission, reminders may appear with a delay (up to a few minutes)."
                            else
                                "\nBez tego uprawnienia przypomnienia mogą pojawić się z opóźnieniem (do kilku minut).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showAlarmExplanation = false
                            permissionManager.requestExactAlarmPermission { granted ->
                                if (granted) {
                                    pendingHabitData?.let { (name, time, frequency) ->
                                        viewModel.addHabit(name, time, frequency)
                                        Toast.makeText(
                                            context,
                                            if (isEnglish) "Habit added" else "Dodano nawyk",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        pendingHabitData = null
                                    }
                                } else {
                                    showAlarmDeniedDialog = true
                                }
                            }
                        }
                    ) {
                        Text(if (isEnglish) "Grant permission" else "Przyznaj uprawnienie")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showAlarmExplanation = false
                            pendingHabitData?.let { (name, time, frequency) ->
                                viewModel.addHabit(name, time, frequency)
                                Toast.makeText(
                                    context,
                                    if (isEnglish) "Habit added (reminders may be delayed)" else "Nawyk dodany (przypomnienia mogą być opóźnione)",
                                    Toast.LENGTH_LONG
                                ).show()
                                pendingHabitData = null
                            }
                        }
                    ) {
                        Text(if (isEnglish) "Skip" else "Pomiń")
                    }
                }
            )
        }

        if (showAlarmDeniedDialog) {
            AlertDialog(
                onDismissRequest = {
                    showAlarmDeniedDialog = false
                    pendingHabitData?.let { (name, time, frequency) ->
                        viewModel.addHabit(name, time, frequency)
                        pendingHabitData = null
                    }
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                title = {
                    Text(if (isEnglish) "No exact alarm permission" else "Brak uprawnień do dokładnych alarmów")
                },
                text = {
                    Text(
                        if (isEnglish)
                            "Without exact alarm permission, reminders may appear with a delay.\n\n⚠️ Consequences:\n• Notifications may be delayed by several minutes\n• Harder to maintain regularity\n• App will work, but less precisely\n\nYou can enable this permission later in system settings."
                        else
                            "Bez uprawnienia do dokładnych alarmów przypomnienia mogą pojawić się z opóźnieniem.\n\n⚠️ Konsekwencje:\n• Powiadomienia mogą być opóźnione o kilka minut\n• Trudniej zachować regularność\n• Aplikacja będzie działać, ale mniej precyzyjnie\n\nMożesz włączyć to uprawnienie później w ustawieniach systemowych."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            permissionManager.openAppSettings()
                            showAlarmDeniedDialog = false
                        }
                    ) {
                        Text(if (isEnglish) "Open settings" else "Otwórz ustawienia")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showAlarmDeniedDialog = false
                            pendingHabitData?.let { (name, time, frequency) ->
                                viewModel.addHabit(name, time, frequency)
                                Toast.makeText(
                                    context,
                                    if (isEnglish) "Habit added (reminders may be delayed)" else "Nawyk dodany (przypomnienia mogą być opóźnione)",
                                    Toast.LENGTH_LONG
                                ).show()
                                pendingHabitData = null
                            }
                        }
                    ) {
                        Text(if (isEnglish) "I understand" else "Rozumiem")
                    }
                }
            )
        }
    }
}