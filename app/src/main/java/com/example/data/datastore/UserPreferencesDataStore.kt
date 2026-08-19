package com.example.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "xarlo_preferences")

enum class ThemeMode {
    DARK, LIGHT, SYSTEM
}

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val waterTargetMl: Int = 3000,
    val sleepTargetMinutes: Int = 450,
    val notificationsEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val focusTask: String = "Deep Work / Study",
    val focusTargetSessions: Int = 4,
    val focusPresetName: String = "Classic"
)

class UserPreferencesDataStore(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val WATER_TARGET_ML = intPreferencesKey("water_target_ml")
        val SLEEP_TARGET_MINUTES = intPreferencesKey("sleep_target_minutes")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val FOCUS_TASK = stringPreferencesKey("focus_task")
        val FOCUS_TARGET_SESSIONS = intPreferencesKey("focus_target_sessions")
        val FOCUS_PRESET_NAME = stringPreferencesKey("focus_preset_name")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        val themeModeStr = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.DARK.name
        val themeMode = try {
            ThemeMode.valueOf(themeModeStr)
        } catch (e: Exception) {
            ThemeMode.DARK
        }

        UserPreferences(
            themeMode = themeMode,
            waterTargetMl = preferences[PreferencesKeys.WATER_TARGET_ML] ?: 3000,
            sleepTargetMinutes = preferences[PreferencesKeys.SLEEP_TARGET_MINUTES] ?: 450,
            notificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true,
            vibrationEnabled = preferences[PreferencesKeys.VIBRATION_ENABLED] ?: true,
            soundEnabled = preferences[PreferencesKeys.SOUND_ENABLED] ?: true,
            focusTask = preferences[PreferencesKeys.FOCUS_TASK] ?: "Deep Work / Study",
            focusTargetSessions = preferences[PreferencesKeys.FOCUS_TARGET_SESSIONS] ?: 4,
            focusPresetName = preferences[PreferencesKeys.FOCUS_PRESET_NAME] ?: "Classic"
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun setWaterTargetMl(target: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WATER_TARGET_ML] = target
        }
    }

    suspend fun setSleepTargetMinutes(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SLEEP_TARGET_MINUTES] = minutes
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIBRATION_ENABLED] = enabled
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SOUND_ENABLED] = enabled
        }
    }

    suspend fun setFocusTask(task: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FOCUS_TASK] = task
        }
    }

    suspend fun setFocusTargetSessions(target: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FOCUS_TARGET_SESSIONS] = target
        }
    }

    suspend fun setFocusPresetName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FOCUS_PRESET_NAME] = name
        }
    }
}
