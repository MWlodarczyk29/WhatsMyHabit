package com.wlodarczyk.whatsmyhabit.ui.theme.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
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

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

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
                totalCount = habits.size
            )

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(habits, key = { it.id }) { habit ->
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
                onConfirm = { name, time ->
                    viewModel.addHabit(name, time)
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
