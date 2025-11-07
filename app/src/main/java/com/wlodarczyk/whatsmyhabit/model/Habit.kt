package com.wlodarczyk.whatsmyhabit.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class Habit(
    val id: Int,
    val name: String,
    val time: String,
    var done: Boolean = false,
)
