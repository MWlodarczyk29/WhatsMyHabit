package com.wlodarczyk.whatsmyhabit.db

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
    private val LANGUAGE_KEY = stringPreferencesKey("language_preference")

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

    suspend fun saveLanguagePreference(context: Context, language: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
    }

    fun getLanguagePreference(context: Context): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[LANGUAGE_KEY] ?: "PL"
        }
    }
}