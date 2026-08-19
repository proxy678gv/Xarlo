package com.example.data.repository

import com.example.data.datastore.ThemeMode
import com.example.data.datastore.UserPreferences
import com.example.data.datastore.UserPreferencesDataStore
import com.example.data.local.AppDatabase
import com.example.data.local.InitialData
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class XarloRepository(
    private val database: AppDatabase,
    private val preferencesDataStore: UserPreferencesDataStore
) {
    private val scheduleDao = database.scheduleDao()
    private val waterDao = database.waterDao()
    private val habitDao = database.habitDao()
    private val workoutDao = database.workoutDao()
    private val sleepDao = database.sleepDao()
    private val focusDao = database.focusDao()

    val userPreferences: Flow<UserPreferences> = preferencesDataStore.userPreferencesFlow

    suspend fun ensureInitialDataLoaded() {
        val existingSchedules = scheduleDao.getActiveSchedules()
        if (existingSchedules.isEmpty()) {
            scheduleDao.insertAll(InitialData.defaultSchedules)
        }
        val existingHabits = habitDao.getAllHabitsList()
        if (existingHabits.isEmpty()) {
            habitDao.insertAllHabits(InitialData.defaultHabits)
        }
    }

    // Date formatting helper
    fun getTodayDateKey(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun formatDateKey(date: Date): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(date)
    }

    // Schedule
    val allSchedules: Flow<List<ScheduleItemEntity>> = scheduleDao.getAllSchedules()

    fun getCompletionsForDate(dateKey: String = getTodayDateKey()): Flow<List<ScheduleCompletionEntity>> {
        return scheduleDao.getCompletionsForDate(dateKey)
    }

    suspend fun toggleScheduleCompletion(scheduleId: String, dateKey: String = getTodayDateKey()): Boolean {
        val current = scheduleDao.getCompletionsListForDate(dateKey)
        val existing = current.find { it.scheduleId == scheduleId }
        val newStatus = if (existing == null) true else !existing.isCompleted
        scheduleDao.setCompletion(
            ScheduleCompletionEntity(
                id = existing?.id ?: 0,
                scheduleId = scheduleId,
                dateKey = dateKey,
                isCompleted = newStatus
            )
        )
        return newStatus
    }

    suspend fun insertSchedule(item: ScheduleItemEntity) = scheduleDao.insertSchedule(item)
    suspend fun updateSchedule(item: ScheduleItemEntity) = scheduleDao.updateSchedule(item)
    suspend fun deleteSchedule(id: String) = scheduleDao.deleteScheduleById(id)
    suspend fun resetDayCompletions(dateKey: String = getTodayDateKey()) {
        scheduleDao.clearCompletionsForDate(dateKey)
        waterDao.clearWaterForDate(dateKey)
    }

    // Water
    fun getWaterLogsForDate(dateKey: String = getTodayDateKey()): Flow<List<WaterLogEntity>> {
        return waterDao.getWaterLogsForDate(dateKey)
    }

    val allWaterLogs: Flow<List<WaterLogEntity>> = waterDao.getAllWaterLogs()

    suspend fun toggleWaterSlot(slotIndex: Int, dateKey: String = getTodayDateKey()) {
        val logs = waterDao.getWaterLogsListForDate(dateKey)
        val existing = logs.find { it.slotIndex == slotIndex }
        if (existing != null) {
            waterDao.deleteWaterLogBySlot(dateKey, slotIndex)
        } else {
            waterDao.insertWaterLog(
                WaterLogEntity(
                    dateKey = dateKey,
                    amountMl = 250,
                    slotIndex = slotIndex
                )
            )
        }
    }

    suspend fun addCustomWater(amountMl: Int, dateKey: String = getTodayDateKey()) {
        waterDao.insertWaterLog(
            WaterLogEntity(
                dateKey = dateKey,
                amountMl = amountMl,
                slotIndex = -1
            )
        )
    }

    // Habits
    val allHabits: Flow<List<HabitEntity>> = habitDao.getAllHabits()

    fun getHabitLogsForDate(dateKey: String = getTodayDateKey()): Flow<List<HabitLogEntity>> {
        return habitDao.getHabitLogsForDate(dateKey)
    }

    val allHabitLogs: Flow<List<HabitLogEntity>> = habitDao.getAllHabitLogs()

    suspend fun toggleHabit(habitId: String, dateKey: String = getTodayDateKey()) {
        val logs = habitDao.getAllHabitLogsList().filter { it.dateKey == dateKey }
        val existing = logs.find { it.habitId == habitId }
        val newStatus = if (existing == null) true else !existing.isCompleted
        habitDao.setHabitLog(
            HabitLogEntity(
                id = existing?.id ?: 0,
                habitId = habitId,
                dateKey = dateKey,
                isCompleted = newStatus
            )
        )
    }

    suspend fun insertHabit(habit: HabitEntity) = habitDao.insertHabit(habit)
    suspend fun deleteHabit(habit: HabitEntity) = habitDao.deleteHabit(habit)

    // Workouts
    val allWorkoutSessions: Flow<List<WorkoutSessionEntity>> = workoutDao.getAllSessions()

    fun getWorkoutSessionsForDate(dateKey: String = getTodayDateKey()): Flow<List<WorkoutSessionEntity>> {
        return workoutDao.getSessionsForDate(dateKey)
    }

    suspend fun recordWorkoutSession(session: WorkoutSessionEntity) = workoutDao.insertSession(session)

    // Sleep
    fun getSleepForDate(dateKey: String = getTodayDateKey()): Flow<SleepLogEntity?> {
        return sleepDao.getSleepForDate(dateKey)
    }

    val allSleepLogs: Flow<List<SleepLogEntity>> = sleepDao.getAllSleepLogs()

    suspend fun saveSleepLog(log: SleepLogEntity) = sleepDao.insertSleepLog(log)

    // Focus Sessions
    fun getFocusSessionsForDate(dateKey: String = getTodayDateKey()): Flow<List<FocusSessionEntity>> {
        return focusDao.getFocusSessionsForDate(dateKey)
    }

    val allFocusSessions: Flow<List<FocusSessionEntity>> = focusDao.getAllFocusSessions()

    suspend fun recordFocusSession(session: FocusSessionEntity) = focusDao.insertFocusSession(session)

    // Preferences
    suspend fun setThemeMode(mode: ThemeMode) = preferencesDataStore.setThemeMode(mode)
    suspend fun setWaterTargetMl(target: Int) = preferencesDataStore.setWaterTargetMl(target)
    suspend fun setSleepTargetMinutes(minutes: Int) = preferencesDataStore.setSleepTargetMinutes(minutes)
    suspend fun setNotificationsEnabled(enabled: Boolean) = preferencesDataStore.setNotificationsEnabled(enabled)
    suspend fun setVibrationEnabled(enabled: Boolean) = preferencesDataStore.setVibrationEnabled(enabled)
    suspend fun setSoundEnabled(enabled: Boolean) = preferencesDataStore.setSoundEnabled(enabled)
    suspend fun setFocusTask(task: String) = preferencesDataStore.setFocusTask(task)
    suspend fun setFocusTargetSessions(target: Int) = preferencesDataStore.setFocusTargetSessions(target)
    suspend fun setFocusPresetName(name: String) = preferencesDataStore.setFocusPresetName(name)

    // Full export & restore data models
    suspend fun getAllDataForExport(): AllBackupData {
        return AllBackupData(
            schedules = scheduleDao.getActiveSchedules(),
            completions = scheduleDao.getAllCompletions(),
            waterLogs = waterDao.getAllWaterLogsList(),
            habits = habitDao.getAllHabitsList(),
            habitLogs = habitDao.getAllHabitLogsList(),
            workoutSessions = workoutDao.getAllSessionsList(),
            sleepLogs = sleepDao.getAllSleepLogsList(),
            focusSessions = focusDao.getAllFocusSessionsList()
        )
    }

    suspend fun restoreAllData(data: AllBackupData) {
        if (data.schedules.isNotEmpty()) scheduleDao.insertAll(data.schedules)
        if (data.completions.isNotEmpty()) scheduleDao.insertAllCompletions(data.completions)
        if (data.waterLogs.isNotEmpty()) waterDao.insertAllWaterLogs(data.waterLogs)
        if (data.habits.isNotEmpty()) habitDao.insertAllHabits(data.habits)
        if (data.habitLogs.isNotEmpty()) habitDao.insertAllHabitLogs(data.habitLogs)
        if (data.workoutSessions.isNotEmpty()) workoutDao.insertAllSessions(data.workoutSessions)
        if (data.sleepLogs.isNotEmpty()) sleepDao.insertAllSleepLogs(data.sleepLogs)
        if (data.focusSessions.isNotEmpty()) focusDao.insertAllFocusSessions(data.focusSessions)
    }
}

data class AllBackupData(
    val app: String = "XARLO",
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val schedules: List<ScheduleItemEntity> = emptyList(),
    val completions: List<ScheduleCompletionEntity> = emptyList(),
    val waterLogs: List<WaterLogEntity> = emptyList(),
    val habits: List<HabitEntity> = emptyList(),
    val habitLogs: List<HabitLogEntity> = emptyList(),
    val workoutSessions: List<WorkoutSessionEntity> = emptyList(),
    val sleepLogs: List<SleepLogEntity> = emptyList(),
    val focusSessions: List<FocusSessionEntity> = emptyList()
)
