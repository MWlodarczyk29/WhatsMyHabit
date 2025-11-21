package com.wlodarczyk.whatsmyhabit.db

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.wlodarczyk.whatsmyhabit.model.Habit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("habits_store")

object HabitDataStore {
    private val HABITS_KEY = stringPreferencesKey("habits_json")
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val listType = Types.newParameterizedType(List::class.java, Habit::class.java)
    private val adapter = moshi.adapter<List<Habit>>(listType)

    fun getHabitsFlow(context: Context): Flow<List<Habit>> {
        return context.dataStore.data.map { prefs ->
            try {
                prefs[HABITS_KEY]?.let { adapter.fromJson(it) ?: emptyList() } ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun saveHabits(context: Context, habits: List<Habit>) {
        context.dataStore.edit { prefs ->
            prefs[HABITS_KEY] = adapter.toJson(habits)
        }
    }
}
