package com.wlodarczyk.whatsmyhabit

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.Manifest
import android.util.Log

object NotificationUtils {

    const val CHANNEL_ID = "habit_notifications"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Przypomnienia o nawykach"
            val descriptionText = "Powiadomienia przypominające o wykonaniu nawyku"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    fun showHabitNotification(context: Context, habitName: String, habitId: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Czas na Twój nawyk!")
            .setContentText(habitName)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager = NotificationManagerCompat.from(context)

        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.e("NotificationUtils", "BŁĄD KRYTYCZNY: Próba wysłania powiadomienia bez uprawnień!")
                return
            }

            Log.d("NotificationUtils", "Próba wyświetlenia powiadomienia ID: $habitId dla nawyku: $habitName")
            notificationManager.notify(habitId, notification)
            Log.d("NotificationUtils", "Wywołano notify() bez wyjątku.")

        } catch (e: Exception) {
            Log.e("NotificationUtils", "WYSTĄPIŁ WYJĄTEK PODCZAS WYŚWIETLANIA POWIADOMIENIA!", e)
        }
    }

}
