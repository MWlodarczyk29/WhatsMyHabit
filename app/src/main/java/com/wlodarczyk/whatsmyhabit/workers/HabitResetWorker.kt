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
            Log.d(TAG, "Rozpoczęto resetowanie nawyków o północy")

            // pobranie wszystkich nawyków
            val habits = HabitDataStore.getHabitsFlow(applicationContext).first()
            Log.d(TAG, "Pobrano ${habits.size} nawyków do przetworzenia")

            // resetowanie nawyków, które wymagają resetu
            val updatedHabits = habits.map { habit ->
                if (habit.shouldReset()) {
                    Log.d(TAG, "Resetowanie nawyku: ${habit.name}")
                    habit.copy(done = false)
                } else {
                    habit
                }
            }

            // zapisanie zaktualizowanych nawyków
            HabitDataStore.saveHabits(applicationContext, updatedHabits)

            // inicjalizacja schedulera alarmów
            val alarmScheduler = AlarmScheduler(applicationContext)

            // anulowanie wszystkich starych alarmów
            habits.forEach { habit ->
                alarmScheduler.cancelAlarm(habit)
            }

            Log.d(TAG, "Planowanie alarmów dla ${updatedHabits.size} nawyków na nowy dzień")

            // zaplanowanie nowych alarmów
            updatedHabits.forEach { habit ->
                Log.d(TAG, "Planowanie alarmu dla: ${habit.name}")
                alarmScheduler.scheduleAlarm(habit)
            }

            Log.d(TAG, "Zakończono resetowanie nawyków pomyślnie")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Błąd podczas resetowania nawyków", e)
            Result.failure()
        }
    }
}