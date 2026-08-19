package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class ScheduleItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val timeStr: String,
    val hour: Int,
    val minute: Int,
    val note: String,
    val recurrence: String = "DAILY", // ONCE, DAILY, WEEKDAYS, WEEKENDS, WEEKLY, CUSTOM
    val daysOfWeek: String = "1,2,3,4,5,6,7", // 1=Mon .. 7=Sun
    val isEnabled: Boolean = true,
    val notify: Boolean = true,
    val category: String = "CUSTOM", // WATER, MEDICATION, MEAL, WORKOUT, SLEEP, CUSTOM
    val sortOrder: Int = 0,
    val createdTimestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "schedule_completions",
    indices = [Index(value = ["scheduleId", "dateKey"], unique = true)]
)
data class ScheduleCompletionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scheduleId: String,
    val dateKey: String, // "yyyy-MM-dd"
    val isCompleted: Boolean,
    val completedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "water_logs")
data class WaterLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateKey: String, // "yyyy-MM-dd"
    val amountMl: Int = 250,
    val slotIndex: Int = -1, // 0..6 for preset glasses, -1 for custom
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String = "HEALTH",
    val iconName: String = "Check",
    val targetDaysPerWeek: Int = 7,
    val createdTimestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "habit_logs",
    indices = [Index(value = ["habitId", "dateKey"], unique = true)]
)
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: String,
    val dateKey: String,
    val isCompleted: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayNumber: Int, // 1, 2, 3, 4
    val dayName: String,
    val focusArea: String,
    val dateKey: String,
    val durationSeconds: Int,
    val completedExercisesCount: Int,
    val totalExercisesCount: Int,
    val isCompleted: Boolean,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "sleep_logs",
    indices = [Index(value = ["dateKey"], unique = true)]
)
data class SleepLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateKey: String,
    val bedtimeHour: Int = 22,
    val bedtimeMinute: Int = 0,
    val wakeHour: Int = 5,
    val wakeMinute: Int = 30,
    val durationMinutes: Int = 450, // 7.5 hours
    val qualityRating: Int = 5, // 1 to 5
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskName: String,
    val presetName: String,
    val durationMinutes: Int,
    val completedCycles: Int,
    val targetCycles: Int,
    val dateKey: String,
    val timestamp: Long = System.currentTimeMillis()
)
