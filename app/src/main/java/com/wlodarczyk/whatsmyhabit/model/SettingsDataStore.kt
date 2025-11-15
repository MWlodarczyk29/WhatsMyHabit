package com.wlodarczyk.whatsmyhabit.model

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object SettingsDataStore {

    private val THEME_KEY = stringPreferencesKey("theme_preferences")

    suspend fun saveThemePreference(context: Context, themeValue: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = themeValue
        }
    }

    fun getThemePreference(context: Context): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[THEME_KEY] ?: "SYSTEM"
        }
    }

}
