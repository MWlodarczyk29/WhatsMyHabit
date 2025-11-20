package com.wlodarczyk.whatsmyhabit.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wlodarczyk.whatsmyhabit.model.Habit
import com.wlodarczyk.whatsmyhabit.db.HabitDataStore
import com.wlodarczyk.whatsmyhabit.utils.AlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HabitsViewModel(
    private val context: Context,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    init {
        loadHabits()
    }

    private fun loadHabits() {
        viewModelScope.launch {
            HabitDataStore.getHabitsFlow(context).collect { habitsList ->
                _habits.value = habitsList
            }
        }
    }

    fun addHabit(name: String, time: String) {
        val newHabit = Habit(
            id = System.currentTimeMillis().toInt(),
            name = name,
            time = time,
            done = false
        )

        val updatedList = _habits.value + newHabit
        _habits.value = updatedList

        alarmScheduler.scheduleAlarm(newHabit)

        viewModelScope.launch {
            HabitDataStore.saveHabits(context, updatedList)
        }
    }

    fun removeHabit(habit: Habit) {
        alarmScheduler.cancelAlarm(habit)

        val updatedList = _habits.value.filter { it.id != habit.id }
        _habits.value = updatedList

        viewModelScope.launch {
            HabitDataStore.saveHabits(context, updatedList)
        }
    }

    fun toggleHabitDone(habitId: Int, isDone: Boolean) {
        val updatedList = _habits.value.map { habit ->
            if (habit.id == habitId) {
                habit.copy(done = isDone)
            } else {
                habit
            }
        }

        _habits.value = updatedList

        viewModelScope.launch {
            HabitDataStore.saveHabits(context, updatedList)
        }
    }

    fun getDoneCount(): Int = _habits.value.count { it.done }
}