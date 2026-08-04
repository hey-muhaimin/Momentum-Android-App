package com.momentum.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "momentum_user_prefs")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val HAS_ONBOARDED = booleanPreferencesKey("has_onboarded")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val REMINDER_TIMES_CSV = stringPreferencesKey("reminder_times_csv")
    }

    val hasOnboarded: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HAS_ONBOARDED] ?: false
    }

    val reminderTime: Flow<Pair<Int, Int>> = context.dataStore.data.map { preferences ->
        val hour = preferences[PreferencesKeys.REMINDER_HOUR] ?: 21
        val minute = preferences[PreferencesKeys.REMINDER_MINUTE] ?: 0
        Pair(hour, minute)
    }

    val reminderTimesList: Flow<List<Pair<Int, Int>>> = context.dataStore.data.map { preferences ->
        val csv = preferences[PreferencesKeys.REMINDER_TIMES_CSV]
        if (!csv.isNullOrBlank()) {
            csv.split(",").mapNotNull { timeStr ->
                val parts = timeStr.trim().split(":")
                if (parts.size == 2) {
                    val h = parts[0].toIntOrNull()
                    val m = parts[1].toIntOrNull()
                    if (h != null && m != null) Pair(h, m) else null
                } else null
            }
        } else {
            val hour = preferences[PreferencesKeys.REMINDER_HOUR] ?: 21
            val minute = preferences[PreferencesKeys.REMINDER_MINUTE] ?: 0
            listOf(Pair(hour, minute))
        }
    }

    val isReminderEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.REMINDER_ENABLED] ?: true
    }

    suspend fun setOnboarded(onboarded: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_ONBOARDED] = onboarded
        }
    }

    suspend fun setReminderTime(hour: Int, minute: Int, enabled: Boolean = true) {
        setReminderTimes(listOf(Pair(hour, minute)), enabled)
    }

    suspend fun setReminderTimes(times: List<Pair<Int, Int>>, enabled: Boolean = true) {
        context.dataStore.edit { preferences ->
            val csv = times.joinToString(",") { (h, m) -> String.format("%02d:%02d", h, m) }
            preferences[PreferencesKeys.REMINDER_TIMES_CSV] = csv
            if (times.isNotEmpty()) {
                preferences[PreferencesKeys.REMINDER_HOUR] = times.first().first
                preferences[PreferencesKeys.REMINDER_MINUTE] = times.first().second
            }
            preferences[PreferencesKeys.REMINDER_ENABLED] = enabled
        }
    }
}

