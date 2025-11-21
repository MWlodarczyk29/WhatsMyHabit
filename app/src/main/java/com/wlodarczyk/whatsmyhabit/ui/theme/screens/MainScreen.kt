package com.wlodarczyk.whatsmyhabit.ui.theme.screens

import android.content.Intent
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
import com.wlodarczyk.whatsmyhabit.ui.theme.components.AddHabitDialog
import com.wlodarczyk.whatsmyhabit.ui.theme.components.HabitCard
import com.wlodarczyk.whatsmyhabit.ui.theme.components.HabitStatsHeader
import com.wlodarczyk.whatsmyhabit.viewmodel.HabitsViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(viewModel: HabitsViewModel) {
    val context = LocalContext.current
    val habits by viewModel.habits.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    val activeTodayHabits = remember(habits) {
        habits.filter { it.isActiveToday() }
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    // sprawdź i zresetuj nawyki przy każdym otwarciu ekranu
    LaunchedEffect(Unit) {
        viewModel.checkAndResetHabits()
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
                    viewModel.addHabit(name, time, frequency)
                    Toast.makeText(
                        context,
                        "Dodano nawyk",
                        Toast.LENGTH_SHORT
                    ).show()
                    showDialog = false
                }
            )
        }
    }
}