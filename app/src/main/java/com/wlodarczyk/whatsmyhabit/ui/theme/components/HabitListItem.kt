package com.wlodarczyk.whatsmyhabit.ui.theme.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wlodarczyk.whatsmyhabit.model.Habit

@Composable
fun HabitListItem(
    habit: Habit,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = habit.done,
                onCheckedChange = onCheckedChange
            )
        }

        Text("${habit.name} o ${habit.time}")

        TextButton(onClick = onDelete) {
            Text("Usuń")
        }
    }
}
