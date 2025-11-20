package com.wlodarczyk.whatsmyhabit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.wlodarczyk.whatsmyhabit.db.SettingsDataStore
import com.wlodarczyk.whatsmyhabit.ui.theme.screens.MainScreen
import com.wlodarczyk.whatsmyhabit.ui.theme.WhatsMyHabitTheme
import com.wlodarczyk.whatsmyhabit.utils.AlarmScheduler
import com.wlodarczyk.whatsmyhabit.utils.PermissionManager
import com.wlodarczyk.whatsmyhabit.viewmodel.HabitsViewModel

class MainActivity : ComponentActivity() {

    private lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionManager = PermissionManager(this)
        permissionManager.requestNotificationPermission()
        permissionManager.requestExactAlarmPermission()

        NotificationUtils.createNotificationChannel(this)

        setContent {
            val context = LocalContext.current
            val alarmScheduler = AlarmScheduler(context)
            val viewModel = HabitsViewModel(context, alarmScheduler)

            val themePreference by SettingsDataStore
                .getThemePreference(context)
                .collectAsState(initial = "SYSTEM")

            val useDarkTheme = when (themePreference) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            WhatsMyHabitTheme(darkTheme = useDarkTheme) {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}