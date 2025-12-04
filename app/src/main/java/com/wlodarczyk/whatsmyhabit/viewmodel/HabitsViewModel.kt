package com.wlodarczyk.whatsmyhabit.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wlodarczyk.whatsmyhabit.model.Habit
import com.wlodarczyk.whatsmyhabit.model.HabitFrequency
import com.wlodarczyk.whatsmyhabit.db.HabitDataStore
import com.wlodarczyk.whatsmyhabit.utils.AlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HabitsViewModel(
    private val context: Context,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    companion object {
        private const val TAG = "HabitsViewModel"
    }

    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    init {
        loadHabits()
        checkAndResetHabits()
    }

    private fun loadHabits() {
        viewModelScope.launch {
            HabitDataStore.getHabitsFlow(context).collect { habitsList ->
                val habitsWithUpdatedStreak = habitsList.map { habit ->
                    habit.copy(streak = habit.calculateStreak())
                }
                _habits.value = habitsWithUpdatedStreak
            }
        }
    }

    fun checkAndResetHabits() {
        viewModelScope.launch {
            val updatedList = _habits.value.map { habit ->
                val calculatedStreak = habit.calculateStreak()

                if (habit.shouldReset()) {
                    Log.d(TAG, "Reset '${habit.name}' - streak: $calculatedStreak")
                    habit.copy(
                        done = false,
                        streak = calculatedStreak
                    )
                } else {
                    if (calculatedStreak != habit.streak) {
                        Log.d(TAG, "Aktualizacja streak '${habit.name}': ${habit.streak} -> $calculatedStreak")
                    }
                    habit.copy(streak = calculatedStreak)
                }
            }

            if (updatedList != _habits.value) {
                _habits.value = updatedList
                HabitDataStore.saveHabits(context, updatedList)
            }
            rescheduleAllAlarms()
        }
    }

    fun reloadFromDataStore() {
        viewModelScope.launch {
            Log.d(TAG, "Przeładowywanie z DataStore...")

            val freshData = HabitDataStore.getHabitsFlow(context).first()
            Log.d(TAG, "Załadowano ${freshData.size} nawyków")

            val habitsWithUpdatedStreak = freshData.map { habit ->
                habit.copy(streak = habit.calculateStreak())
            }
            _habits.value = habitsWithUpdatedStreak

            checkAndResetHabits()
        }
    }

    private fun rescheduleAllAlarms() {
        viewModelScope.launch {
            // anuluj wszystkie istniejące alarmy
            _habits.value.forEach { habit ->
                alarmScheduler.cancelAlarm(habit)
            }

            Log.d(TAG, "Planowanie alarmów dla ${_habits.value.size} nawyków")

            // zaplanuj nowe alarmy
            _habits.value.forEach { habit ->
                Log.d(TAG, "Planowanie alarmu dla: ${habit.name}")
                alarmScheduler.scheduleAlarm(habit)
            }
        }
    }

    fun addHabit(name: String, time: String, frequency: HabitFrequency = HabitFrequency.DAILY) {
        val newHabit = Habit(
            id = System.currentTimeMillis().toInt(),
            name = name,
            time = time,
            done = false,
            frequency = frequency,
            createdDate = System.currentTimeMillis(),
            lastCompletedDate = null,
            streak = 0
        )

        val updatedList = _habits.value + newHabit
        _habits.value = updatedList

        Log.d(TAG, "Dodano nowy nawyk: ${newHabit.name}, planowanie alarmu")
        alarmScheduler.scheduleAlarm(newHabit)

        viewModelScope.launch {
            HabitDataStore.saveHabits(context, updatedList)
        }
    }

    fun removeHabit(habit: Habit) {
        Log.d(TAG, "Usuwanie nawyku: ${habit.name}")
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
                if (isDone) {
                    val newStreak = habit.updateStreakOnCompletion()
                    Log.d(TAG, "Habit '${habit.name}' - nowy streak: $newStreak")

                    habit.copy(
                        done = true,
                        lastCompletedDate = System.currentTimeMillis(),
                        streak = newStreak
                    )
                } else {
                    habit.copy(done = false)
                }
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
    fun getTotalActiveTodayCount(): Int = _habits.value.size

    fun getActiveTodayHabits(): List<Habit> = _habits.value
}