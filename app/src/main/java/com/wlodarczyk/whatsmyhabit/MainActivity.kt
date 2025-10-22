package com.wlodarczyk.whatsmyhabit

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.wlodarczyk.whatsmyhabit.model.Habit
import com.wlodarczyk.whatsmyhabit.model.HabitDataStore
import com.wlodarczyk.whatsmyhabit.ui.theme.WhatsMyHabitTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val sampleHabits = remember { mutableStateListOf<Habit>() }
            val context = LocalContext.current
            val scope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                try {
                    HabitDataStore.getHabitsFlow(context).collect { list ->
                        sampleHabits.clear()
                        sampleHabits.addAll(list)
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Błąd wczytywania danych", Toast.LENGTH_SHORT).show()
                }
            }

            var showDialog by remember { mutableStateOf(false) }
            var name by remember { mutableStateOf("") }
            var time by remember { mutableStateOf("") }
            val timeRegex = Regex("^([01]\\d|2[0-3]):([0-5]\\d)$")

            WhatsMyHabitTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text("Moje nawyki")}
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(onClick = { showDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Dodaj nawyk")
                        }
                    }
                ) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        val doneCount = sampleHabits.count { it.done }
                        Text("Liczba ukończonych nawyków: $doneCount / ${sampleHabits.size}",
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    HabitList(habits = sampleHabits, modifier = Modifier.fillMaxHeight(), scope = scope)
                    }
                }

                if (showDialog) {
                    AlertDialog( //tworzy standardowe okienko dialogowe w Compose
                        onDismissRequest = { showDialog = false },
                        title = { Text("Dodaj nowy nawyk") },
                        text = {
                            Column {
                                OutlinedTextField( //pole do wpisania nazwy i godziny nawyku
                                    value = name,
                                    onValueChange = { name = it },
                                    label = { Text("Nazwa nawyku") }
                                )
                                OutlinedTextField(
                                    value = time,
                                    onValueChange = { time = it },
                                    label = { Text("Godzina (HH:MM)") }
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                if (name.isNotBlank() && timeRegex.matches(time)) {
                                    val newHabit = Habit(sampleHabits.size + 1, name, time, done = false)
                                    sampleHabits.add(newHabit)
                                    scope.launch {
                                        HabitDataStore.saveHabits(context, sampleHabits.toList())
                                    }
                                    Toast.makeText(context, "Dodano nawyk", Toast.LENGTH_SHORT)
                                        .show()
                                    name = ""
                                    time = ""
                                    showDialog = false
                                } else {
                                    Toast.makeText(context, "Niepoprawna godzina", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Text("Dodaj")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDialog = false }) {
                                Text("Anuluj")
                            }
                        }
                    )
                }
            }
        }
    }

    @Composable
    fun HabitList(habits: SnapshotStateList<Habit>, modifier: Modifier = Modifier, scope: CoroutineScope) {
        val context = LocalContext.current
        LazyColumn(modifier = modifier) { //lazy column wyswietla liste przewijana pionowo
            items(habits, key = {it.id}) { habit ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically){
                        Checkbox(
                            checked = habit.done,
                            onCheckedChange = { checked ->
                                habit.done = checked
                                scope.launch { HabitDataStore.saveHabits(context, habits.toList())}
                            }
                        )
                    }
                    Text("${habit.name} o ${habit.time}")
                    TextButton(onClick = {
                        habits.remove(habit)
                       scope.launch { HabitDataStore.saveHabits(context, habits.toList())}
                        Toast.makeText(context, "Usunięto nawyk", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("Usuń")
                    }

                }
            }
        }
    }
