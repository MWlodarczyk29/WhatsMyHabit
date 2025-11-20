package com.wlodarczyk.whatsmyhabit.ui.theme.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HabitStatsHeader(doneCount: Int, totalCount: Int) {
    Text(
        text = "Liczba ukończonych nawyków: $doneCount / $totalCount",
        modifier = Modifier.padding(8.dp)
    )
}