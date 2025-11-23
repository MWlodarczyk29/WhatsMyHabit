package com.wlodarczyk.whatsmyhabit

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.wlodarczyk.whatsmyhabit.db.SettingsDataStore
import com.wlodarczyk.whatsmyhabit.ui.theme.WhatsMyHabitTheme
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val themePreference by SettingsDataStore.getThemePreference(context).collectAsState(initial = "SYSTEM")
            val languagePreference by SettingsDataStore.getLanguagePreference(context).collectAsState(initial = "PL")

            val useDarkTheme = when (themePreference) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            WhatsMyHabitTheme(darkTheme = useDarkTheme) {
                SettingsScreen(
                    onBackClicked = { finish() },
                    isEnglish = languagePreference == "EN"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBackClicked: () -> Unit, isEnglish: Boolean) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val themePreference by SettingsDataStore.getThemePreference(context).collectAsState(initial = "SYSTEM")
    val languagePreference by SettingsDataStore.getLanguagePreference(context).collectAsState(initial = "PL")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEnglish) "Settings" else "Ustawienia") },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = if (isEnglish) "Back" else "Wróć")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = if (isEnglish) "App info" else "Informacje o aplikacji",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // SEKCJA MOTYWU
            Text(
                text = if (isEnglish) "App Theme" else "Motyw aplikacji",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            val themes = if (isEnglish) {
                mapOf("LIGHT" to "Light", "DARK" to "Dark", "SYSTEM" to "System")
            } else {
                mapOf("LIGHT" to "Jasny", "DARK" to "Ciemny", "SYSTEM" to "Systemowy")
            }

            Column(modifier = Modifier.selectableGroup()) {
                themes.forEach { (key, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = (themePreference == key),
                                onClick = {
                                    scope.launch {
                                        SettingsDataStore.saveThemePreference(context, key)
                                    }
                                },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (themePreference == key),
                            onClick = null
                        )
                        Text(
                            text = value,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(24.dp))

            // SEKCJA JĘZYKA
            Text(
                text = if (isEnglish) "Language" else "Język",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            val languages = mapOf(
                "PL" to "Polski",
                "EN" to "English"
            )

            Column(modifier = Modifier.selectableGroup()) {
                languages.forEach { (key, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = (languagePreference == key),
                                onClick = {
                                    scope.launch {
                                        SettingsDataStore.saveLanguagePreference(context, key)
                                    }
                                },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (languagePreference == key),
                            onClick = null
                        )
                        Text(
                            text = value,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        }
    }
}