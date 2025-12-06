package com.wlodarczyk.whatsmyhabit

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.Manifest

object NotificationUtils {

    const val CHANNEL_ID = "habit_notifications"
    private const val TAG = "NotificationUtils"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // używamy stringResource z Context - automatycznie w odpowiednim języku!
            val name = context.getString(R.string.notification_channel_name)
            val descriptionText = context.getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText

                // ustawienie domyślnego dźwięku powiadomienia
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    null
                )

                // konfiguracja wibracji
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)

                // widoczność na ekranie blokady
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC

                // włączenie diody LED
                enableLights(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // WAŻNE: jeśli kanał już istnieje, Android go zaktualizuje
            notificationManager.createNotificationChannel(channel)

            Log.d(TAG, "Kanał powiadomień utworzony/zaktualizowany: $name")
        }
    }
    fun showHabitNotification(context: Context, habitName: String, habitId: Int) {
        // WAŻNE: przed wyświetleniem powiadomienia, zawsze odśwież kanał
        createNotificationChannel(context)

        // intent otwierający główny ekran aplikacji po kliknięciu powiadomienia
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

        // teksty powiadomienia - automatycznie w aktualnym języku aplikacji
        val title = context.getString(R.string.notification_title)
        val text = context.getString(R.string.notification_text, habitName)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notis_icon)
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
                    .bigText(text)
            )
            .build()

        val notificationManager = NotificationManagerCompat.from(context)

        try {
            // sprawdzenie uprawnień do powiadomień
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e(TAG, "Brak uprawnień do powiadomień!")
                return
            }

            Log.d(TAG, "Wyświetlanie powiadomienia ID: $habitId dla: $habitName w języku: ${context.resources.configuration.locales[0]}")
            notificationManager.notify(habitId, notification)
            Log.d(TAG, "Powiadomienie wysłane pomyślnie")

        } catch (e: Exception) {
            Log.e(TAG, "Wyjątek podczas wyświetlania powiadomienia!", e)
        }
    }
}