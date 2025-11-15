package com.wlodarczyk.whatsmyhabit.model

data class Habit(
    val id: Int,
    val name: String,
    val time: String,
    var done: Boolean = false,
)
