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

    override suspend fun doWork(): Result {
        return try {
            Log.d("HabitResetWorker", "Rozpoczęto resetowanie nawyków o północy")

            val habits = HabitDataStore.getHabitsFlow(applicationContext).first()

            val updatedHabits = habits.map { habit ->
                if (habit.shouldReset()) {
                    Log.d("HabitResetWorker", "Resetowanie nawyku: ${habit.name}")
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

            Log.d("HabitResetWorker", "Planowanie alarmów dla ${updatedHabits.size} nawyków na nowy dzień")

            updatedHabits.forEach { habit ->
                Log.d("HabitResetWorker", "Planowanie alarmu dla: ${habit.name}")
                alarmScheduler.scheduleAlarm(habit)
            }

            Log.d("HabitResetWorker", "Zakończono resetowanie nawyków")
            Result.success()
        } catch (e: Exception) {
            Log.e("HabitResetWorker", "Błąd podczas resetowania nawyków", e)
            Result.failure()
        }
    }
}