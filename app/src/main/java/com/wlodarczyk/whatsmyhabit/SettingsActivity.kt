package com.wlodarczyk.whatsmyhabit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.wlodarczyk.whatsmyhabit.db.SettingsDataStore
import com.wlodarczyk.whatsmyhabit.ui.theme.WhatsMyHabitTheme
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val themePreference by SettingsDataStore.getThemePreference(context).collectAsState(initial = "SYSTEM")
            val useDarkTheme = when (themePreference) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            WhatsMyHabitTheme(darkTheme = useDarkTheme) {
                SettingsScreen(
                    currentTheme = themePreference,
                    onThemeChange = { newTheme ->
                        (this as ComponentActivity).lifecycleScope.launch {
                            SettingsDataStore.saveThemePreference(context, newTheme)
                        }
                    },
                    onBackClicked = {
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentTheme: String,
    onThemeChange: (String) -> Unit,
    onBackClicked: () -> Unit
) {
    var showInfoDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ustawienia") },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showInfoDialog = true }) {
                Icon(Icons.Default.Info, contentDescription = "Informacje o aplikacji")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .selectableGroup()
        ) {
            Text("Motyw aplikacji", style = MaterialTheme.typography.titleLarge)

            val themes = mapOf("LIGHT" to "Jasny", "DARK" to "Ciemny", "SYSTEM" to "Systemowy")

            themes.forEach { (key, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .selectable(
                            selected = (currentTheme == key),
                            onClick = { onThemeChange(key) },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (currentTheme == key),
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

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("O aplikacji ,,What's my habit?''") },
            text = {
                Text(
                    ",,What's my habit?'' jest aplikacją, która pomaga w budowaniu i śledzeniu codziennych nawyków. " +
                            "Możesz dodawać nowe nawyki, ustawiać dla nich przypomnienia i monitorować swoje postępy. " +
                            "Celem aplikacji jest wspieranie Cię w regularności i osiąganiu zamierzonych celów. " +
                    "Powodzenia!"
                )
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Rozumiem")
                }
            }
        )
    }
}
