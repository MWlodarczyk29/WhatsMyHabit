package com.wlodarczyk.whatsmyhabit.ui.theme.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wlodarczyk.whatsmyhabit.R

@Composable
fun HabitStatsHeader(
    doneCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(R.string.completed_habits, doneCount, totalCount),
        modifier = modifier.padding(8.dp)
    )
}