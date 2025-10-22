package com.wlodarczyk.whatsmyhabit.model

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.wlodarczyk.whatsmyhabit.R

class HabitNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val habitName = intent?.getStringExtra("habit_name") ?: "Twój nawyk"
        val time = intent?.getStringExtra("habit_time") ?: ""

        val notification = NotificationCompat.Builder(context, "habit_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Czas na nawyk!")
            .setContentText("$habitName o $time")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(habitName.hashCode(), notification)
    }
}
