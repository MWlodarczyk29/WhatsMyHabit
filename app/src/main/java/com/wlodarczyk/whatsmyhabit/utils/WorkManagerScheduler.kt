package com.wlodarczyk.whatsmyhabit.utils

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.wlodarczyk.whatsmyhabit.workers.HabitResetWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

object WorkManagerScheduler {

    private const val HABIT_RESET_WORK_NAME = "habit_reset_work"

    /**
     * planuje codzienne resetowanie nawyków o północy
     */
    fun scheduleDailyReset(context: Context) {
        val currentTime = Calendar.getInstance()
        val midnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (before(currentTime)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val initialDelay = midnight.timeInMillis - currentTime.timeInMillis

        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .build()

        val resetWorkRequest = PeriodicWorkRequestBuilder<HabitResetWorker>(
            24, TimeUnit.HOURS,
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            HABIT_RESET_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            resetWorkRequest
        )
    }

    /**
     * anuluje zaplanowane resetowanie
     */
    fun cancelDailyReset(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(HABIT_RESET_WORK_NAME)
    }
}