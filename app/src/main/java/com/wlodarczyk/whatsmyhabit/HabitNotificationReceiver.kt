package com.wlodarczyk.whatsmyhabit

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast

class HabitNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Toast.makeText(context, "Receiver uruchomiony!", Toast.LENGTH_LONG).show()

        val habitId = intent?.getIntExtra("habit_id", 0) ?: 0
        val habitName = intent?.getStringExtra("habit_name") ?: "Twój nawyk"
        val time = intent?.getStringExtra("habit_time") ?: ""
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("HabitReceiver", "Wywoływanie NotificationUtils.showHabitNotification dla: $habitName")
            NotificationUtils.showHabitNotification(context, habitName, habitId)
        } else {
            Log.e("HabitReceiver", "Brak uprawnień do wysłania powiadomienia!")
        }

        if (time.isNotEmpty()) {
            rescheduleAlarm(context, habitId, habitName, time)
        }
    }

    private fun rescheduleAlarm(context: Context, habitId: Int, habitName: String, time: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager

        val intent = Intent(context, HabitNotificationReceiver::class.java).apply {
            putExtra("habit_id", habitId)
            putExtra("habit_name", habitName)
            putExtra("habit_time", time)
        }

        val pendingIntent = android.app.PendingIntent.getBroadcast(
            context,
            habitId,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val timeParts = time.split(":").map { it.toInt() }
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(java.util.Calendar.HOUR_OF_DAY, timeParts[0])
            set(java.util.Calendar.MINUTE, timeParts[1])
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            if (before(java.util.Calendar.getInstance())) {
                add(java.util.Calendar.DATE, 1)
            }
        }

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                    Log.d("HabitReceiver", "Zaplanowano dokładny alarm na następny dzień.")
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                    Log.w("HabitReceiver", "Brak uprawnień do dokładnych alarmów. Użyto niedokładnego alarmu.")
                }
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
                Log.d("HabitReceiver", "Zaplanowano dokładny alarm (dla M-R) na następny dzień.")
            }
            else -> {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
                Log.d("HabitReceiver", "Zaplanowano powtarzalny alarm (dla <M) na następny dzień.")
            }
        }
    }
}
