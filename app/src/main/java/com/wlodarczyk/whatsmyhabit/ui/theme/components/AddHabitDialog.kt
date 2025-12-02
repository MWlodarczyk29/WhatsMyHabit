package com.wlodarczyk.whatsmyhabit.ui.theme.components

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.wlodarczyk.whatsmyhabit.model.HabitFrequency
import java.util.Calendar

@Composable
fun AddHabitDialog(
    isEnglish: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (name: String, time: String, frequency: HabitFrequency) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var selectedFrequency by remember { mutableStateOf(HabitFrequency.DAILY) }
    val context = LocalContext.current

    val calendar = Calendar.getInstance()
    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hour: Int, minute: Int ->
                time = String.format("%02d:%02d", hour, minute)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEnglish) "Add new habit" else "Dodaj nowy nawyk") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (isEnglish) "Habit name" else "Nazwa nawyku") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (time.isNotBlank())
                            "${if (isEnglish) "Time" else "Godzina"}: $time"
                        else
                            if (isEnglish) "Select time" else "Wybierz godzinę"
                    )
                    TextButton(onClick = { timePickerDialog.show() }) {
                        Text(if (isEnglish) "Change" else "Zmień")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && time.isNotBlank()) {
                        onConfirm(name, time, selectedFrequency)
                    }
                }
            ) {
                Text(if (isEnglish) "Add" else "Dodaj")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isEnglish) "Cancel" else "Anuluj")
            }
        }
    )
}