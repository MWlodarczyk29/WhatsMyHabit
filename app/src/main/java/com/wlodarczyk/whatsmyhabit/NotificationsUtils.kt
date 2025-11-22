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

object NotificationUtils {

    const val CHANNEL_ID = "habit_notifications"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Przypomnienia o nawykach"
            val descriptionText = "Powiadomienia przypominające o wykonaniu nawyku"

            // IMPORTANCE_HIGH żeby powiadomienia nie były ciche
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText

                // włącz dźwięk
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    null
                )

                // włącz wibracje
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)

                // włącz pokazywanie na ekranie blokady
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC

            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            Log.d("NotificationUtils", "Kanał powiadomień utworzony z IMPORTANCE_HIGH")
        }
    }

    fun showHabitNotification(context: Context, habitName: String, habitId: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            habitId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // użyj domyślnego dźwięku powiadomienia
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Czas na Twój nawyk!")
            .setContentText(habitName)

            //PRIORITY_HIGH dla starszych Androidów (przed API 26)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

            // kategoria jako alarm/reminder
            .setCategory(NotificationCompat.CATEGORY_ALARM)

            // Dźwięk i wibracje
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(0, 250, 250, 250))

            //wyświetlanie jako heads-up notification
            .setDefaults(NotificationCompat.DEFAULT_ALL)

            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

            // styl expanded dla większego tekstu
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Nadszedł czas na wykonanie nawyku: $habitName")
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