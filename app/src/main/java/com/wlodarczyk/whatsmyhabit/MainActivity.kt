package com.wlodarczyk.whatsmyhabit

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.wlodarczyk.whatsmyhabit.ui.theme.WhatsMyHabitTheme
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.platform.LocalContext
import com.wlodarczyk.whatsmyhabit.Habit



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val sampleHabits = remember {
                mutableStateListOf(
                    Habit(1, "Pij wodę", "8:00"),
                    Habit(2, "Zrób śniadanie", "9:00"),
                    Habit(3, "Pij wodę", "9:10")
                )
            }
            var showDialog by remember { mutableStateOf(false) }
            //remember przechowuje stan tzn. zapamietuje wartosc pomiedzy kompozycjami
            var name by remember { mutableStateOf("") }
            var time by remember { mutableStateOf("") }
            val context = LocalContext.current
            val timeRegex = Regex("^([01]\\d|2[0-3]):([0-5]\\d)$")

            WhatsMyHabitTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = {
                        FloatingActionButton(onClick = { showDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Dodaj nawyk")
                        }
                    }
                ) { innerPadding ->
                    HabitList(habits = sampleHabits, modifier = Modifier.padding(innerPadding))
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
                                    sampleHabits.add(Habit(sampleHabits.size + 1, name, time))
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
    fun HabitList(habits: SnapshotStateList<Habit>, modifier: Modifier = Modifier) {
        val context = LocalContext.current
        LazyColumn(modifier = modifier) { //lazy column wyswietla liste przewijana pionowo
            items(habits) { habit ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${habit.name} o ${habit.time}")
                    TextButton(onClick = {
                        habits.remove(habit)
                        Toast.makeText(context, "Usunięto nawyk", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("Usuń")
                    }

                }
            }
        }
    }
}