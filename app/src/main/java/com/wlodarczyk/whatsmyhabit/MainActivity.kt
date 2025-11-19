package com.wlodarczyk.whatsmyhabit

import android.Manifest
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.wlodarczyk.whatsmyhabit.model.SettingsDataStore
import com.wlodarczyk.whatsmyhabit.ui.theme.WhatsMyHabitTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Calendar


class MainActivity : ComponentActivity() {

    private val alarmManager: AlarmManager by lazy {
        getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Pozwolenie na powiadomienia przyznane!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Odmówiono pozwolenia na powiadomienia.", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestExactAlarmPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "Uprawnienie do dokładnych alarmów przyznane.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Odmówiono uprawnienia do dokładnych alarmów.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun askForExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent().apply {
                    action = android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                }.also {
                    requestExactAlarmPermissionLauncher.launch(it)
                }
            }
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
        askForNotificationPermission()
        askForExactAlarmPermission()
        NotificationUtils.createNotificationChannel(this)
        setContent {
            val sampleHabits = remember { mutableStateListOf<Habit>() }
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val alarmManager = this.alarmManager
            val themePreference by SettingsDataStore.getThemePreference(context).collectAsState(initial = "SYSTEM")
            val useDarkTheme = when (themePreference) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            LaunchedEffect(Unit) {
                try {
                    HabitDataStore.getHabitsFlow(context).collect { list ->
                        sampleHabits.clear()
                        sampleHabits.addAll(list)
                    }
                } catch (e: Exception) {
                }
            }

            var showDialog by remember { mutableStateOf(false) }
            var name by remember { mutableStateOf("") }
            var time by remember { mutableStateOf("") }

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


            WhatsMyHabitTheme(darkTheme = useDarkTheme) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
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
                            }
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
                            scope = scope,
                            alarmManager = alarmManager
                        )
                    }
                    if (showDialog) {
                        AlertDialog(
                            onDismissRequest = { showDialog = false },
                            title = { Text("Dodaj nowy nawyk") },
                            text = {
                                Column {
                                    OutlinedTextField(
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

                                        scheduleNotification(context, newHabit)

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

private fun scheduleNotification(context: Context, newHabit: Habit) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val intent = Intent(context, HabitNotificationReceiver::class.java).apply {
        putExtra("habit_id", newHabit.id)
        putExtra("habit_name", newHabit.name)
        putExtra("habit_time", newHabit.time)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        newHabit.id,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val timeParts = newHabit.time.split(":").map {
        it.toInt()
    }
    val calendar = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
        set(Calendar.HOUR_OF_DAY, timeParts[0])
        set(Calendar.MINUTE, timeParts[1])
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)

        if (before(Calendar.getInstance())) {
            add(Calendar.DATE, 1)
        }
    }

    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
                Toast.makeText(context, "Powiadomienia mogą być opóźnione. Nadaj uprawnienia do alarmów w ustawieniach.", Toast.LENGTH_LONG).show()
            }
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
        else -> {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        }
    }
    Toast.makeText(context, "Alarm zaplanowany na ${calendar.time}", Toast.LENGTH_LONG).show()
}



@Composable
fun HabitList(habits: SnapshotStateList<Habit>, modifier: Modifier = Modifier, scope: CoroutineScope, alarmManager: AlarmManager) {
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
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

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