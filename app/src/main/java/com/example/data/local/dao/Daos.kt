package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedules ORDER BY hour ASC, minute ASC, sortOrder ASC")
    fun getAllSchedules(): Flow<List<ScheduleItemEntity>>

    @Query("SELECT * FROM schedules WHERE isEnabled = 1 ORDER BY hour ASC, minute ASC")
    suspend fun getActiveSchedules(): List<ScheduleItemEntity>

    @Query("SELECT * FROM schedules WHERE id = :id LIMIT 1")
    suspend fun getScheduleById(id: String): ScheduleItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(item: ScheduleItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ScheduleItemEntity>)

    @Update
    suspend fun updateSchedule(item: ScheduleItemEntity)

    @Query("DELETE FROM schedules WHERE id = :id")
    suspend fun deleteScheduleById(id: String)

    @Query("DELETE FROM schedules")
    suspend fun deleteAll()

    // Completions
    @Query("SELECT * FROM schedule_completions WHERE dateKey = :dateKey")
    fun getCompletionsForDate(dateKey: String): Flow<List<ScheduleCompletionEntity>>

    @Query("SELECT * FROM schedule_completions WHERE dateKey = :dateKey")
    suspend fun getCompletionsListForDate(dateKey: String): List<ScheduleCompletionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setCompletion(completion: ScheduleCompletionEntity)

    @Query("DELETE FROM schedule_completions WHERE dateKey = :dateKey")
    suspend fun clearCompletionsForDate(dateKey: String)

    @Query("SELECT * FROM schedule_completions")
    suspend fun getAllCompletions(): List<ScheduleCompletionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCompletions(completions: List<ScheduleCompletionEntity>)
}

@Dao
interface WaterDao {
    @Query("SELECT * FROM water_logs WHERE dateKey = :dateKey ORDER BY timestamp ASC")
    fun getWaterLogsForDate(dateKey: String): Flow<List<WaterLogEntity>>

    @Query("SELECT * FROM water_logs WHERE dateKey = :dateKey ORDER BY timestamp ASC")
    suspend fun getWaterLogsListForDate(dateKey: String): List<WaterLogEntity>

    @Query("SELECT * FROM water_logs ORDER BY timestamp DESC")
    fun getAllWaterLogs(): Flow<List<WaterLogEntity>>

    @Query("SELECT * FROM water_logs")
    suspend fun getAllWaterLogsList(): List<WaterLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterLog(log: WaterLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllWaterLogs(logs: List<WaterLogEntity>)

    @Query("DELETE FROM water_logs WHERE dateKey = :dateKey AND slotIndex = :slotIndex")
    suspend fun deleteWaterLogBySlot(dateKey: String, slotIndex: Int)

    @Query("DELETE FROM water_logs WHERE id = :id")
    suspend fun deleteWaterLogById(id: Long)

    @Query("DELETE FROM water_logs WHERE dateKey = :dateKey")
    suspend fun clearWaterForDate(dateKey: String)
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY createdTimestamp ASC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits")
    suspend fun getAllHabitsList(): List<HabitEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllHabits(habits: List<HabitEntity>)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabitById(id: String)

    // Habit Logs
    @Query("SELECT * FROM habit_logs WHERE dateKey = :dateKey")
    fun getHabitLogsForDate(dateKey: String): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY dateKey DESC")
    fun getLogsForHabit(habitId: String): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs ORDER BY dateKey DESC")
    fun getAllHabitLogs(): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs")
    suspend fun getAllHabitLogsList(): List<HabitLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setHabitLog(log: HabitLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllHabitLogs(logs: List<HabitLogEntity>)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND dateKey = :dateKey")
    suspend fun deleteHabitLog(habitId: String, dateKey: String)
}

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE dateKey = :dateKey ORDER BY timestamp DESC")
    fun getSessionsForDate(dateKey: String): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions")
    suspend fun getAllSessionsList(): List<WorkoutSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSessions(sessions: List<WorkoutSessionEntity>)

    @Query("DELETE FROM workout_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)
}

@Dao
interface SleepDao {
    @Query("SELECT * FROM sleep_logs WHERE dateKey = :dateKey LIMIT 1")
    fun getSleepForDate(dateKey: String): Flow<SleepLogEntity?>

    @Query("SELECT * FROM sleep_logs ORDER BY dateKey DESC")
    fun getAllSleepLogs(): Flow<List<SleepLogEntity>>

    @Query("SELECT * FROM sleep_logs")
    suspend fun getAllSleepLogsList(): List<SleepLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepLog(log: SleepLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSleepLogs(logs: List<SleepLogEntity>)

    @Query("DELETE FROM sleep_logs WHERE dateKey = :dateKey")
    suspend fun deleteSleepForDate(dateKey: String)
}

@Dao
interface FocusDao {
    @Query("SELECT * FROM focus_sessions WHERE dateKey = :dateKey ORDER BY timestamp DESC")
    fun getFocusSessionsForDate(dateKey: String): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC")
    fun getAllFocusSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions")
    suspend fun getAllFocusSessionsList(): List<FocusSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusSession(session: FocusSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFocusSessions(sessions: List<FocusSessionEntity>)
}
