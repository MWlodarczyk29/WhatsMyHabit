package com.wlodarczyk.whatsmyhabit.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.wlodarczyk.whatsmyhabit.db.HabitDataStore
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

            Log.d("HabitResetWorker", "Zakończono resetowanie nawyków")
            Result.success()
        } catch (e: Exception) {
            Log.e("HabitResetWorker", "Błąd podczas resetowania nawyków", e)
            Result.failure()
        }
    }
}