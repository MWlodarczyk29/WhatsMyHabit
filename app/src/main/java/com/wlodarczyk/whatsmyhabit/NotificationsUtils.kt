package com.wlodarczyk.whatsmyhabit

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationUtils {
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "habit_channel",
                "Przypomnienia o nawykach",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Kanał powiadomień dla nawyków"
            }

            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
