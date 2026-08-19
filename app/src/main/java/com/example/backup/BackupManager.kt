package com.example.backup

import com.example.data.local.entity.*
import com.example.data.repository.AllBackupData
import org.json.JSONArray
import org.json.JSONObject

object BackupManager {

    fun exportToJson(data: AllBackupData): String {
        val root = JSONObject()
        root.put("app", data.app)
        root.put("version", data.version)
        root.put("exportedAt", data.exportedAt)

        // Schedules
        val schedulesArray = JSONArray()
        data.schedules.forEach { s ->
            val obj = JSONObject().apply {
                put("id", s.id)
                put("title", s.title)
                put("timeStr", s.timeStr)
                put("hour", s.hour)
                put("minute", s.minute)
                put("note", s.note)
                put("recurrence", s.recurrence)
                put("daysOfWeek", s.daysOfWeek)
                put("isEnabled", s.isEnabled)
                put("notify", s.notify)
                put("category", s.category)
                put("sortOrder", s.sortOrder)
                put("createdTimestamp", s.createdTimestamp)
            }
            schedulesArray.put(obj)
        }
        root.put("schedules", schedulesArray)

        // Completions
        val compArray = JSONArray()
        data.completions.forEach { c ->
            val obj = JSONObject().apply {
                put("scheduleId", c.scheduleId)
                put("dateKey", c.dateKey)
                put("isCompleted", c.isCompleted)
                put("completedTimestamp", c.completedTimestamp)
            }
            compArray.put(obj)
        }
        root.put("completions", compArray)

        // Water
        val waterArray = JSONArray()
        data.waterLogs.forEach { w ->
            val obj = JSONObject().apply {
                put("dateKey", w.dateKey)
                put("amountMl", w.amountMl)
                put("slotIndex", w.slotIndex)
                put("timestamp", w.timestamp)
            }
            waterArray.put(obj)
        }
        root.put("waterLogs", waterArray)

        // Habits
        val habitsArray = JSONArray()
        data.habits.forEach { h ->
            val obj = JSONObject().apply {
                put("id", h.id)
                put("title", h.title)
                put("category", h.category)
                put("iconName", h.iconName)
                put("targetDaysPerWeek", h.targetDaysPerWeek)
                put("createdTimestamp", h.createdTimestamp)
            }
            habitsArray.put(obj)
        }
        root.put("habits", habitsArray)

        // Habit Logs
        val habitLogsArray = JSONArray()
        data.habitLogs.forEach { hl ->
            val obj = JSONObject().apply {
                put("habitId", hl.habitId)
                put("dateKey", hl.dateKey)
                put("isCompleted", hl.isCompleted)
                put("timestamp", hl.timestamp)
            }
            habitLogsArray.put(obj)
        }
        root.put("habitLogs", habitLogsArray)

        // Workout Sessions
        val workoutArray = JSONArray()
        data.workoutSessions.forEach { ws ->
            val obj = JSONObject().apply {
                put("dayNumber", ws.dayNumber)
                put("dayName", ws.dayName)
                put("focusArea", ws.focusArea)
                put("dateKey", ws.dateKey)
                put("durationSeconds", ws.durationSeconds)
                put("completedExercisesCount", ws.completedExercisesCount)
                put("totalExercisesCount", ws.totalExercisesCount)
                put("isCompleted", ws.isCompleted)
                put("notes", ws.notes)
                put("timestamp", ws.timestamp)
            }
            workoutArray.put(obj)
        }
        root.put("workoutSessions", workoutArray)

        // Sleep Logs
        val sleepArray = JSONArray()
        data.sleepLogs.forEach { sl ->
            val obj = JSONObject().apply {
                put("dateKey", sl.dateKey)
                put("bedtimeHour", sl.bedtimeHour)
                put("bedtimeMinute", sl.bedtimeMinute)
                put("wakeHour", sl.wakeHour)
                put("wakeMinute", sl.wakeMinute)
                put("durationMinutes", sl.durationMinutes)
                put("qualityRating", sl.qualityRating)
                put("notes", sl.notes)
                put("timestamp", sl.timestamp)
            }
            sleepArray.put(obj)
        }
        root.put("sleepLogs", sleepArray)

        // Focus Sessions
        val focusArray = JSONArray()
        data.focusSessions.forEach { fs ->
            val obj = JSONObject().apply {
                put("taskName", fs.taskName)
                put("presetName", fs.presetName)
                put("durationMinutes", fs.durationMinutes)
                put("completedCycles", fs.completedCycles)
                put("targetCycles", fs.targetCycles)
                put("dateKey", fs.dateKey)
                put("timestamp", fs.timestamp)
            }
            focusArray.put(obj)
        }
        root.put("focusSessions", focusArray)

        return root.toString(2)
    }

    fun parseFromJson(jsonString: String): AllBackupData {
        val root = JSONObject(jsonString)
        val app = root.optString("app", "XARLO")
        val version = root.optInt("version", 1)
        val exportedAt = root.optLong("exportedAt", System.currentTimeMillis())

        val schedules = mutableListOf<ScheduleItemEntity>()
        val schedulesArray = root.optJSONArray("schedules")
        if (schedulesArray != null) {
            for (i in 0 until schedulesArray.length()) {
                val obj = schedulesArray.getJSONObject(i)
                schedules.add(
                    ScheduleItemEntity(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        timeStr = obj.optString("timeStr", "8:00 AM"),
                        hour = obj.getInt("hour"),
                        minute = obj.getInt("minute"),
                        note = obj.optString("note", ""),
                        recurrence = obj.optString("recurrence", "DAILY"),
                        daysOfWeek = obj.optString("daysOfWeek", "1,2,3,4,5,6,7"),
                        isEnabled = obj.optBoolean("isEnabled", true),
                        notify = obj.optBoolean("notify", true),
                        category = obj.optString("category", "CUSTOM"),
                        sortOrder = obj.optInt("sortOrder", 0),
                        createdTimestamp = obj.optLong("createdTimestamp", System.currentTimeMillis())
                    )
                )
            }
        }

        val completions = mutableListOf<ScheduleCompletionEntity>()
        val compArray = root.optJSONArray("completions")
        if (compArray != null) {
            for (i in 0 until compArray.length()) {
                val obj = compArray.getJSONObject(i)
                completions.add(
                    ScheduleCompletionEntity(
                        scheduleId = obj.getString("scheduleId"),
                        dateKey = obj.getString("dateKey"),
                        isCompleted = obj.getBoolean("isCompleted"),
                        completedTimestamp = obj.optLong("completedTimestamp", System.currentTimeMillis())
                    )
                )
            }
        }

        val waterLogs = mutableListOf<WaterLogEntity>()
        val waterArray = root.optJSONArray("waterLogs")
        if (waterArray != null) {
            for (i in 0 until waterArray.length()) {
                val obj = waterArray.getJSONObject(i)
                waterLogs.add(
                    WaterLogEntity(
                        dateKey = obj.getString("dateKey"),
                        amountMl = obj.optInt("amountMl", 250),
                        slotIndex = obj.optInt("slotIndex", -1),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        }

        val habits = mutableListOf<HabitEntity>()
        val habitsArray = root.optJSONArray("habits")
        if (habitsArray != null) {
            for (i in 0 until habitsArray.length()) {
                val obj = habitsArray.getJSONObject(i)
                habits.add(
                    HabitEntity(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        category = obj.optString("category", "HEALTH"),
                        iconName = obj.optString("iconName", "Check"),
                        targetDaysPerWeek = obj.optInt("targetDaysPerWeek", 7),
                        createdTimestamp = obj.optLong("createdTimestamp", System.currentTimeMillis())
                    )
                )
            }
        }

        val habitLogs = mutableListOf<HabitLogEntity>()
        val habitLogsArray = root.optJSONArray("habitLogs")
        if (habitLogsArray != null) {
            for (i in 0 until habitLogsArray.length()) {
                val obj = habitLogsArray.getJSONObject(i)
                habitLogs.add(
                    HabitLogEntity(
                        habitId = obj.getString("habitId"),
                        dateKey = obj.getString("dateKey"),
                        isCompleted = obj.getBoolean("isCompleted"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        }

        val workoutSessions = mutableListOf<WorkoutSessionEntity>()
        val workoutArray = root.optJSONArray("workoutSessions")
        if (workoutArray != null) {
            for (i in 0 until workoutArray.length()) {
                val obj = workoutArray.getJSONObject(i)
                workoutSessions.add(
                    WorkoutSessionEntity(
                        dayNumber = obj.optInt("dayNumber", 1),
                        dayName = obj.optString("dayName", "Day 1"),
                        focusArea = obj.optString("focusArea", "Upper Body"),
                        dateKey = obj.getString("dateKey"),
                        durationSeconds = obj.optInt("durationSeconds", 0),
                        completedExercisesCount = obj.optInt("completedExercisesCount", 0),
                        totalExercisesCount = obj.optInt("totalExercisesCount", 6),
                        isCompleted = obj.optBoolean("isCompleted", false),
                        notes = obj.optString("notes", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        }

        val sleepLogs = mutableListOf<SleepLogEntity>()
        val sleepArray = root.optJSONArray("sleepLogs")
        if (sleepArray != null) {
            for (i in 0 until sleepArray.length()) {
                val obj = sleepArray.getJSONObject(i)
                sleepLogs.add(
                    SleepLogEntity(
                        dateKey = obj.getString("dateKey"),
                        bedtimeHour = obj.optInt("bedtimeHour", 22),
                        bedtimeMinute = obj.optInt("bedtimeMinute", 0),
                        wakeHour = obj.optInt("wakeHour", 5),
                        wakeMinute = obj.optInt("wakeMinute", 30),
                        durationMinutes = obj.optInt("durationMinutes", 450),
                        qualityRating = obj.optInt("qualityRating", 5),
                        notes = obj.optString("notes", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        }

        val focusSessions = mutableListOf<FocusSessionEntity>()
        val focusArray = root.optJSONArray("focusSessions")
        if (focusArray != null) {
            for (i in 0 until focusArray.length()) {
                val obj = focusArray.getJSONObject(i)
                focusSessions.add(
                    FocusSessionEntity(
                        taskName = obj.optString("taskName", "Deep Work"),
                        presetName = obj.optString("presetName", "Classic"),
                        durationMinutes = obj.optInt("durationMinutes", 25),
                        completedCycles = obj.optInt("completedCycles", 1),
                        targetCycles = obj.optInt("targetCycles", 4),
                        dateKey = obj.getString("dateKey"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        }

        return AllBackupData(
            app = app,
            version = version,
            exportedAt = exportedAt,
            schedules = schedules,
            completions = completions,
            waterLogs = waterLogs,
            habits = habits,
            habitLogs = habitLogs,
            workoutSessions = workoutSessions,
            sleepLogs = sleepLogs,
            focusSessions = focusSessions
        )
    }
}
