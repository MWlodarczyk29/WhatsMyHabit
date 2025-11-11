package com.wlodarczyk.whatsmyhabit

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.wlodarczyk.whatsmyhabit.model.Habit
import com.wlodarczyk.whatsmyhabit.model.HabitDataStore
import com.wlodarczyk.whatsmyhabit.ui.theme.WhatsMyHabitTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Calendar
import android.Manifest



class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Pozwolenie na powiadomienia przyznane!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Odmówiono pozwolenia na powiadomienia.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun askForNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        askForNotificationPermission()
        NotificationUtils.createNotificationChannel(this)
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

            var showTimePicker by remember { mutableStateOf(false) }
            var calendar = Calendar.getInstance()

            val timePickerDialog = TimePickerDialog(
                context,
                { _, hour: Int, minute: Int ->
                    time = String.format("%02d:%02d", hour, minute)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            )

            if (showTimePicker) {
                timePickerDialog.show()
                showTimePicker = false
            }


                WhatsMyHabitTheme {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = { Text("Moje nawyki") }
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
                            val doneCount = sampleHabits.count { it.done }
                            Text(
                                "Liczba ukończonych nawyków: $doneCount / ${sampleHabits.size}",
                                modifier = Modifier.padding(8.dp)
                            )
                            HabitList(
                                habits = sampleHabits,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(bottom = 70.dp),
                                scope = scope
                            )
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
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = if (time.isNotBlank()) time else "Wybierz godzinę")
                                            TextButton(onClick = { showTimePicker = true}){
                                                Text("Zmień")
                                            }
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = {
                                        if (name.isNotBlank() && time.isNotBlank()){
                                            val newHabit = Habit(
                                                System.currentTimeMillis().toInt(),
                                                name,
                                                time,
                                                done = false
                                            )
                                            sampleHabits.add(newHabit)

                                            val parts = time.split(":")
                                            val hour = parts[0].toInt()
                                            val minute = parts[1].toInt()

                                            val calendar = Calendar.getInstance().apply {
                                                set(Calendar.HOUR_OF_DAY, hour)
                                                set(Calendar.MINUTE, minute)
                                                set(Calendar.SECOND, 0)
                                                if (before(Calendar.getInstance())) add(
                                                    Calendar.DAY_OF_MONTH,
                                                    1
                                                )
                                            }

                                            val intent = Intent(
                                                context,
                                                HabitNotificationReceiver::class.java
                                            ).apply {
                                                putExtra("habit_id", newHabit.id)
                                                putExtra("habit_name", newHabit.name)
                                                putExtra("habit_time", newHabit.time)
                                            }

                                            val pendingIntent = PendingIntent.getBroadcast(
                                                context,
                                                newHabit.id,
                                                intent,
                                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                                            )

                                            val alarmManager =
                                                context.getSystemService(ALARM_SERVICE) as AlarmManager
                                            alarmManager.setRepeating(
                                                AlarmManager.RTC_WAKEUP,
                                                calendar.timeInMillis,
                                                AlarmManager.INTERVAL_DAY,
                                                pendingIntent
                                            )

                                            scope.launch {
                                                HabitDataStore.saveHabits(
                                                    context,
                                                    sampleHabits.toList()
                                                )
                                            }

                                            Toast.makeText(
                                                context,
                                                "Dodano nawyk",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            name = ""
                                            time = ""
                                            showDialog = false
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Niepoprawna godzina",
                                                Toast.LENGTH_SHORT
                                            ).show()
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
    }
}

    @Composable
    fun HabitList(habits: SnapshotStateList<Habit>, modifier: Modifier = Modifier, scope: CoroutineScope) {
        val context = LocalContext.current

        fun updateAndSaveHabits(updatedList: List<Habit>) {
            scope.launch {
                HabitDataStore.saveHabits(context, updatedList)
            }
        }

        LazyColumn(modifier = modifier) {
            items(habits, key = {it.id}) { habit ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically){
                        Checkbox(
                            checked = habit.done,
                            onCheckedChange = { checked ->
                                val index = habits.indexOf(habit)
                                if (index != -1) {
                                    habits[index] = habit.copy(done = checked)
                                    updateAndSaveHabits(habits.toList())
                                }
                            }
                        )
                    }
                    Text("${habit.name} o ${habit.time}")
                    TextButton(onClick = {
                        val intent = Intent(context, HabitNotificationReceiver::class.java).apply {
                            putExtra("habit_id", habit.id)
                            putExtra("habit_name", habit.name)
                            putExtra("habit_time", habit.time)
                        }

                        val pendingIntent = PendingIntent.getBroadcast(
                            context,
                            habit.id,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                        )

                        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        alarmManager.cancel(pendingIntent)

                        habits.remove(habit)
                        updateAndSaveHabits(habits.toList())
                        Toast.makeText(context, "Usunięto nawyk", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("Usuń")
                    }

                }
            }
        }
    }

//dodac ekran ustawien gdzie bedzie mozliwa zmiana jezyka na angielski i dodatkowo zmiana motywu aplikacji
// na ciemny lub jasny w zaleznosci od ustawien systemowych telefonu. Mozna rowniez dodac statystyki np. przy dodawaniu nawyku mozna dodac +5 pkt i te punkty po wykoaniu nawyku nam sie sumuja na dole aplikacji