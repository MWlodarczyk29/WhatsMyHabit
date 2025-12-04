package com.wlodarczyk.whatsmyhabit.ui.theme.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wlodarczyk.whatsmyhabit.model.Habit

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HabitsList(
    habits: List<Habit>,
    recomposeKey: Int,
    onHabitCheckedChange: (Habit, Boolean) -> Unit,
    onHabitDelete: (Habit) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        items(
            items = habits,
            key = { habit -> "${habit.id}-$recomposeKey" }
        ) { habit ->
            HabitCard(
                habit = habit,
                onCheckedChange = { checked ->
                    onHabitCheckedChange(habit, checked)
                },
                onDelete = {
                    onHabitDelete(habit)
                }
            )
        }
    }
}