package com.wlodarczyk.whatsmyhabit

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.wlodarczyk.whatsmyhabit.db.SettingsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Locale

class HabitNotificationReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "NotifReceiver"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val habitId = intent?.getIntExtra("habit_id", 0) ?: 0
        val habitName = intent?.getStringExtra("habit_name")
            ?: context.getString(R.string.notification_content)
        val time = intent?.getStringExtra("habit_time") ?: ""

        Log.d(TAG, "=== POWIADOMIENIE START ===")
        Log.d(TAG, "Habit: $habitName (ID: $habitId)")

        val localizedContext = createLocalizedContext(context)

        Log.d(TAG, "Locale: ${localizedContext.resources.configuration.locales[0]}")

        if (androidx.core.content.ContextCompat.checkSelfPermission(
                localizedContext,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationUtils.showHabitNotification(localizedContext, habitName, habitId)
        } else {
            Log.e(TAG, "Brak uprawnień do powiadomień")
        }

        if (time.isNotEmpty()) {
            rescheduleAlarm(localizedContext, habitId, habitName, time)
        }
    }

    private fun createLocalizedContext(context: Context): Context {
        return try {
            val languagePreference = runBlocking {
                SettingsDataStore.getLanguagePreference(context).first()
            }

            Log.d(TAG, "Preferencja języka z DataStore: $languagePreference")

            val locale = when (languagePreference) {
                "EN" -> Locale.ENGLISH
                else -> Locale("pl", "PL")
            }

            Locale.setDefault(locale)

            val config = context.resources.configuration
            config.setLocale(locale)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.createConfigurationContext(config)
            } else {
                @Suppress("DEPRECATION")
                context.resources.updateConfiguration(config, context.resources.displayMetrics)
                context
            }
        } catch (e: Exception) {
            Log.e(TAG, "Błąd podczas tworzenia lokalizowanego Context", e)
            context
        }
    }

    private fun rescheduleAlarm(context: Context, habitId: Int, habitName: String, time: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
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

        val calendar = calculateNextAlarmTime(time)

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                }
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

        Log.d(TAG, "Alarm przełożony na następny dzień: ${calendar.time}")
    }

    private fun calculateNextAlarmTime(time: String): java.util.Calendar {
        val timeParts = time.split(":").map { it.toInt() }
        return java.util.Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(java.util.Calendar.HOUR_OF_DAY, timeParts[0])
            set(java.util.Calendar.MINUTE, timeParts[1])
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            if (before(java.util.Calendar.getInstance())) {
                add(java.util.Calendar.DATE, 1)
            }
        }
    }
}