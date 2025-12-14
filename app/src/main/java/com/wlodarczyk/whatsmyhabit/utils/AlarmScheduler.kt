package com.wlodarczyk.whatsmyhabit.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.wlodarczyk.whatsmyhabit.HabitNotificationReceiver
import com.wlodarczyk.whatsmyhabit.model.Habit
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarm(habit: Habit) {
        val pendingIntent = createPendingIntent(habit)
        val calendar = calculateAlarmTime(habit.time)

        Log.d("AlarmScheduler", "Scheduling alarm for: ${habit.name} at ${calendar.time}")

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                scheduleForAndroid12Plus(calendar.timeInMillis, pendingIntent)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
            else -> {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            }
        }
    }

    fun cancelAlarm(habit: Habit) {
        val pendingIntent = createPendingIntent(habit)
        alarmManager.cancel(pendingIntent)
        Log.d("AlarmScheduler", "Alarm cancelled for: ${habit.name}")
    }

    private fun createPendingIntent(habit: Habit): PendingIntent {
        val intent = Intent(context, HabitNotificationReceiver::class.java).apply {
            putExtra("habit_id", habit.id)
            putExtra("habit_name", habit.name)
            putExtra("habit_time", habit.time)
            putExtra("habit_frequency", habit.frequency.name)
        }

        return PendingIntent.getBroadcast(
            context,
            habit.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun calculateAlarmTime(time: String): Calendar {
        val timeParts = time.split(":").map { it.toInt() }

        return Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, timeParts[0])
            set(Calendar.MINUTE, timeParts[1])
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1)
            }
        }
    }

    private fun scheduleForAndroid12Plus(timeInMillis: Long, pendingIntent: PendingIntent) {
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent
            )
            Log.d("AlarmScheduler", "Scheduled exact alarm")
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent
            )
            Log.w("AlarmScheduler", "No permission granted for exact alarm")
        }
    }
}