package com.wlodarczyk.whatsmyhabit

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.wlodarczyk.whatsmyhabit.NotificationUtils

class HabitNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val habitId = intent?.getIntExtra("habit_id", 0) ?: 0
        val habitName = intent?.getStringExtra("habit_name") ?: "Twój nawyk"
        val time = intent?.getStringExtra("habit_time") ?: ""

        val notification = NotificationCompat.Builder(context, "habit_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Czas na nawyk!")
            .setContentText("$habitName o $time")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationUtils.createNotificationChannel(context)

        val notificationManager = NotificationManagerCompat.from(context)

        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(habitId, notification)
        }

    }
}