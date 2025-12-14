package com.wlodarczyk.whatsmyhabit

import android.Manifest
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

object NotificationUtils {

    const val CHANNEL_ID = "habit_notifications"
    private const val TAG = "NotificationUtils"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_name)
            val descriptionText = context.getString(R.string.notification_channel_description)
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
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)

            Log.d(TAG, "Notification chhannel updated/created: $name")
        }
    }
    fun showHabitNotification(context: Context, habitName: String, habitId: Int) {
        createNotificationChannel(context)

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
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e(TAG, "No permission granted for notifications")
                return
            }

            Log.d(TAG, "Showing ID notification: $habitId for: $habitName in language: ${context.resources.configuration.locales[0]}")
            notificationManager.notify(habitId, notification)
            Log.d(TAG, "Notification sent successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error while showing notification!", e)
        }
    }
}