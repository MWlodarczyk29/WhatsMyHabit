package com.wlodarczyk.whatsmyhabit.utils

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class PermissionManager(private val activity: ComponentActivity) {

    private val alarmManager by lazy {
        activity.getSystemService(android.app.AlarmManager::class.java)
    }

    private val notificationPermissionLauncher: ActivityResultLauncher<String> =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            val message = if (isGranted) {
                "Pozwolenie na powiadomienia przyznane!"
            } else {
                "Odmówiono pozwolenia na powiadomienia."
            }
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        }

    private val exactAlarmPermissionLauncher: ActivityResultLauncher<Intent> =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val message = if (alarmManager.canScheduleExactAlarms()) {
                    "Uprawnienie do dokładnych alarmów przyznane."
                } else {
                    "Odmówiono uprawnienia do dokładnych alarmów."
                }
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
            }
        }

    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                exactAlarmPermissionLauncher.launch(intent)
            }
        }
    }
}