package com.wlodarczyk.whatsmyhabit.ui.theme.components

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wlodarczyk.whatsmyhabit.R
import com.wlodarczyk.whatsmyhabit.model.HabitFrequency
import java.util.Calendar

@Composable
fun AddHabitDialog(
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
        title = {
            Text(stringResource(R.string.add_new_habit))
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.habit_name)) },
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
                            stringResource(R.string.time_format, time)
                        else
                            stringResource(R.string.select_time)
                    )
                    TextButton(onClick = { timePickerDialog.show() }) {
                        Text(stringResource(R.string.change))
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
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}