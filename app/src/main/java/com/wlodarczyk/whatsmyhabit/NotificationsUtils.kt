package com.wlodarczyk.whatsmyhabit

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.Manifest
import android.util.Log
import android.app.PendingIntent
import android.content.Intent
import com.wlodarczyk.whatsmyhabit.db.SettingsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object NotificationUtils {

    const val CHANNEL_ID = "habit_notifications"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val isEnglish = runBlocking {
                SettingsDataStore.getLanguagePreference(context).first() == "EN"
            }

            val name = if (isEnglish) "Habit Reminders" else "Przypomnienia o nawykach"
            val descriptionText = if (isEnglish)
                "Notifications reminding you to perform habits"
            else
                "Powiadomienia przypominające o wykonaniu nawyku"

            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText

                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    null
                )

                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)

                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC

                enableLights(true)
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            Log.d("NotificationUtils", "Kanał powiadomień utworzony z IMPORTANCE_HIGH")
        }
    }

    fun showHabitNotification(context: Context, habitName: String, habitId: Int) {
        val isEnglish = runBlocking {
            SettingsDataStore.getLanguagePreference(context).first() == "EN"
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            habitId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val title = if (isEnglish) "Time for your habit!" else "Czas na Twój nawyk!"
        val bigText = if (isEnglish)
            "It's time to complete: $habitName"
        else
            "Nadszedł czas na wykonanie nawyku: $habitName"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(habitName)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(bigText)
            )
            .build()

        val notificationManager = NotificationManagerCompat.from(context)

        try {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e("NotificationUtils", "BŁĄD: Brak uprawnień do powiadomień!")
                return
            }

            Log.d("NotificationUtils", "Wyświetlanie powiadomienia ID: $habitId dla: $habitName")
            notificationManager.notify(habitId, notification)
            Log.d("NotificationUtils", "Powiadomienie wysłane pomyślnie")

        } catch (e: Exception) {
            Log.e("NotificationUtils", "WYJĄTEK podczas wyświetlania powiadomienia!", e)
        }
    }
}