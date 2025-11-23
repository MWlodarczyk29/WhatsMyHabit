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
    var showDialog by remember { mutableStateOf(false) }

    var showNotificationExplanation by remember { mutableStateOf(false) }
    var showAlarmExplanation by remember { mutableStateOf(false) }
    var showNotificationDeniedDialog by remember { mutableStateOf(false) }
    var showAlarmDeniedDialog by remember { mutableStateOf(false) }

    var pendingHabitData by remember { mutableStateOf<Triple<String, String, HabitFrequency>?>(null) }

    val activeTodayHabits = remember(habits) {
        habits.filter { it.isActiveToday() }
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
                Log.d("MainScreen", "Wykryto nowy dzień! Przeładowywanie danych...")
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
                title = { Text("Moje nawyki") },
                actions = {
                    IconButton(onClick = {
                        context.startActivity(Intent(context, SettingsActivity::class.java))
                    }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Ustawienia"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj nawyk")
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
                totalCount = viewModel.getTotalActiveTodayCount()
            )

            if (activeTodayHabits.isEmpty()) {
                Text(
                    text = "Brak nawyków na dziś. Dodaj nowy nawyk!",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(activeTodayHabits, key = { it.id }) { habit ->
                    HabitCard(
                        habit = habit,
                        onCheckedChange = { checked ->
                            viewModel.toggleHabitDone(habit.id, checked)
                        },
                        onDelete = {
                            viewModel.removeHabit(habit)
                            Toast.makeText(
                                context,
                                "Usunięto nawyk",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }
        }

        if (showDialog) {
            AddHabitDialog(
                onDismiss = { showDialog = false },
                onConfirm = { name, time, frequency ->
                    showDialog = false
                    pendingHabitData = Triple(name, time, frequency)

                    val addHabit = {
                        viewModel.addHabit(name, time, frequency)
                        Toast.makeText(context, "Dodano nawyk", Toast.LENGTH_SHORT).show()
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
                            "Nawyk dodany bez przypomnień",
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
                    Text("Przypomnienia o nawykach")
                },
                text = {
                    Column {
                        Text(
                            text = "Aby aplikacja mogła wysyłać Ci przypomnienia o nawykach o wybranej godzinie, potrzebne jest uprawnienie do powiadomień.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "\n✓ Otrzymasz powiadomienie o czasie nawyku\n✓ Nie przegapisz żadnego nawyku\n✓ Zwiększysz szansę na sukces",
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
                                            Toast.makeText(context, "Dodano nawyk", Toast.LENGTH_SHORT).show()
                                            pendingHabitData = null
                                        }
                                    }
                                } else {
                                    showNotificationDeniedDialog = true
                                }
                            }
                        }
                    ) {
                        Text("Przyznaj uprawnienie")
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
                                    "Nawyk dodany bez przypomnień",
                                    Toast.LENGTH_LONG
                                ).show()
                                pendingHabitData = null
                            }
                        }
                    ) {
                        Text("Pomiń")
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
                    Text("Brak uprawnień do powiadomień")
                },
                text = {
                    Text(
                        "Bez uprawnienia do powiadomień aplikacja nie będzie mogła wysyłać przypomnień o nawykach.\n\n" +
                                "⚠️ Konsekwencje:\n" +
                                "• Nie otrzymasz powiadomień o czasie nawyku\n" +
                                "• Musisz sam pamiętać o swoich nawykach\n" +
                                "• Może to obniżyć skuteczność budowania nawyków\n\n" +
                                "Możesz włączyć powiadomienia później w ustawieniach systemowych aplikacji."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            permissionManager.openAppSettings()
                            showNotificationDeniedDialog = false
                        }
                    ) {
                        Text("Otwórz ustawienia")
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
                                    "Nawyk dodany bez przypomnień",
                                    Toast.LENGTH_LONG
                                ).show()
                                pendingHabitData = null
                            }
                        }
                    ) {
                        Text("Dodaj mimo to")
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
                            "Nawyk dodany (przypomnienia mogą być opóźnione)",
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
                    Text("Dokładne przypomnienia")
                },
                text = {
                    Column {
                        Text(
                            text = "Aby przypomnienia pojawiały się dokładnie o wybranej godzinie, potrzebne jest dodatkowe uprawnienie.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "\n✓ Przypomnienia o dokładnej godzinie\n✓ Brak opóźnień w powiadomieniach\n✓ Lepsza konsekwencja w nawykach",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "\nBez tego uprawnienia przypomnienia mogą pojawić się z opóźnieniem (do kilku minut).",
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
                                        Toast.makeText(context, "Dodano nawyk", Toast.LENGTH_SHORT).show()
                                        pendingHabitData = null
                                    }
                                } else {
                                    showAlarmDeniedDialog = true
                                }
                            }
                        }
                    ) {
                        Text("Przyznaj uprawnienie")
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
                                    "Nawyk dodany (przypomnienia mogą być opóźnione)",
                                    Toast.LENGTH_LONG
                                ).show()
                                pendingHabitData = null
                            }
                        }
                    ) {
                        Text("Pomiń")
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
                    Text("Brak uprawnień do dokładnych alarmów")
                },
                text = {
                    Text(
                        "Bez uprawnienia do dokładnych alarmów przypomnienia mogą pojawić się z opóźnieniem.\n\n" +
                                "⚠️ Konsekwencje:\n" +
                                "• Powiadomienia mogą być opóźnione o kilka minut\n" +
                                "• Trudniej zachować regularność\n" +
                                "• Aplikacja będzie działać, ale mniej precyzyjnie\n\n" +
                                "Możesz włączyć to uprawnienie później w ustawieniach systemowych."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            permissionManager.openAppSettings()
                            showAlarmDeniedDialog = false
                        }
                    ) {
                        Text("Otwórz ustawienia")
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
                                    "Nawyk dodany (przypomnienia mogą być opóźnione)",
                                    Toast.LENGTH_LONG
                                ).show()
                                pendingHabitData = null
                            }
                        }
                    ) {
                        Text("Rozumiem")
                    }
                }
            )
        }
    }
}