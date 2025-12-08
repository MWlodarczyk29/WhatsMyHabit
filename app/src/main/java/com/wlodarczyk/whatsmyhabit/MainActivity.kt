package com.wlodarczyk.whatsmyhabit

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.wlodarczyk.whatsmyhabit.db.SettingsDataStore
import com.wlodarczyk.whatsmyhabit.ui.theme.WhatsMyHabitTheme
import com.wlodarczyk.whatsmyhabit.ui.theme.screens.MainScreen
import com.wlodarczyk.whatsmyhabit.utils.AlarmScheduler
import com.wlodarczyk.whatsmyhabit.utils.PermissionManager
import com.wlodarczyk.whatsmyhabit.utils.WorkManagerScheduler
import com.wlodarczyk.whatsmyhabit.viewmodel.HabitsViewModel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionManager = PermissionManager(this)
        NotificationUtils.createNotificationChannel(this)
        WorkManagerScheduler.scheduleDailyReset(this)

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
                MainScreen(
                    viewModel = viewModel,
                    permissionManager = permissionManager
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val currentLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            resources.configuration.locales[0]
        } else {
            resources.configuration.locale
        }

        val savedLanguage = runBlocking {
            SettingsDataStore.getLanguagePreference(this@MainActivity).first()
        }

        val expectedLocale = when (savedLanguage) {
            "EN" -> Locale.ENGLISH
            else -> Locale("pl", "PL")
        }

        if (currentLocale.language != expectedLocale.language) {
            recreate()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(updateLocale(newBase))
    }

    private fun updateLocale(context: Context): Context {
        val languagePreference = runBlocking {
            SettingsDataStore.getLanguagePreference(context).first()
        }

        val locale = when (languagePreference) {
            "EN" -> Locale.ENGLISH
            else -> Locale("pl", "PL")
        }

        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }
}