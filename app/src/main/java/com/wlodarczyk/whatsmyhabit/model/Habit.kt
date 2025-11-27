package com.wlodarczyk.whatsmyhabit.model

import java.util.Calendar
import java.util.concurrent.TimeUnit

data class Habit(
    val id: Int,
    val name: String,
    val time: String,
    var done: Boolean = false,
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val createdDate: Long = System.currentTimeMillis(),
    var lastCompletedDate: Long? = null,
    var streak: Int = 0
) {
    fun shouldReset(): Boolean {
        if (!done || lastCompletedDate == null) return false

        val today = getTodayStartMillis()
        val completedDay = getDayStartMillis(lastCompletedDate!!)

        return today > completedDay
    }
    fun calculateStreak(): Int {
        if (lastCompletedDate == null) return 0

        val today = getTodayStartMillis()
        val lastCompleted = getDayStartMillis(lastCompletedDate!!)

        val daysDifference = TimeUnit.MILLISECONDS.toDays(today - lastCompleted)

        return when {
            daysDifference == 0L -> streak
            daysDifference == 1L -> streak
            else -> 0
        }
    }

    fun updateStreakOnCompletion(): Int {
        val today = getTodayStartMillis()
        val lastCompleted = lastCompletedDate?.let { getDayStartMillis(it) }

        return when {
            lastCompleted == null -> 1
            lastCompleted == today -> streak
            TimeUnit.MILLISECONDS.toDays(today - lastCompleted) == 1L -> streak + 1
            else -> 1
        }
    }

    private fun getTodayStartMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getDayStartMillis(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}