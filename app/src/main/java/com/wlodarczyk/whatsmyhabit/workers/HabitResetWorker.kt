package com.wlodarczyk.whatsmyhabit.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.wlodarczyk.whatsmyhabit.db.HabitDataStore
import com.wlodarczyk.whatsmyhabit.utils.AlarmScheduler
import kotlinx.coroutines.flow.first

class HabitResetWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "HabitResetWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Started resetting habits at midnight...")

            val habits = HabitDataStore.getHabitsFlow(applicationContext).first()
            Log.d(TAG, "Loaded ${habits.size} habits to process")

            val updatedHabits = habits.map { habit ->
                if (habit.shouldReset()) {
                    Log.d(TAG, "Habit reset: ${habit.name}")
                    habit.copy(done = false)
                } else {
                    habit
                }
            }

            HabitDataStore.saveHabits(applicationContext, updatedHabits)

            val alarmScheduler = AlarmScheduler(applicationContext)

            habits.forEach { habit ->
                alarmScheduler.cancelAlarm(habit)
            }

            Log.d(TAG, "Scheduling alarms for ${updatedHabits.size} habits for the next day")

            updatedHabits.forEach { habit ->
                Log.d(TAG, "Scheduling alarm for: ${habit.name}")
                alarmScheduler.scheduleAlarm(habit)
            }

            Log.d(TAG, "Habits reset ended successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Habits reset error", e)
            Result.failure()
        }
    }
}