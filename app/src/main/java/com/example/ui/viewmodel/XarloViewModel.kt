package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ai.zlva.ZlvaEngine
import com.example.ai.zlva.ZlvaIntent
import com.example.ai.zlva.ZlvaMessage
import com.example.backup.BackupManager
import com.example.data.datastore.ThemeMode
import com.example.data.datastore.UserPreferences
import com.example.data.local.Exercise
import com.example.data.local.InitialData
import com.example.data.local.WorkoutDayPlan
import com.example.data.local.entity.*
import com.example.data.repository.XarloRepository
import com.example.notification.AlarmScheduler
import com.example.notification.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ScheduleWithStatus(
    val item: ScheduleItemEntity,
    val isCompletedToday: Boolean
)

data class HabitWithStatus(
    val habit: HabitEntity,
    val isCompletedToday: Boolean,
    val currentStreak: Int,
    val completedThisWeek: Int
)

data class FocusPreset(
    val name: String,
    val focusMinutes: Int,
    val shortBreakMinutes: Int,
    val longBreakMinutes: Int,
    val targetCycles: Int
)

data class ActiveWorkoutState(
    val plan: WorkoutDayPlan,
    val completedExerciseIds: Set<String> = emptySet(),
    val isWorkoutActive: Boolean = false,
    val elapsedSeconds: Int = 0,
    val isResting: Boolean = false,
    val restRemainingSeconds: Int = 0,
    val currentRestingExerciseName: String = ""
)

class XarloViewModel(
    private val repository: XarloRepository,
    private val alarmScheduler: AlarmScheduler,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    val userPreferences = repository.userPreferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserPreferences()
    )

    private val _currentNavDestination = MutableStateFlow("home")
    val currentNavDestination: StateFlow<String> = _currentNavDestination.asStateFlow()

    fun navigateTo(dest: String) {
        _currentNavDestination.value = dest
    }

    private val _toastEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    fun showToast(message: String) {
        _toastEvent.tryEmit(message)
    }

    // Schedule
    private val todayKey = repository.getTodayDateKey()

    val schedulesWithStatus: StateFlow<List<ScheduleWithStatus>> = combine(
        repository.allSchedules,
        repository.getCompletionsForDate(todayKey)
    ) { schedules, completions ->
        val completionMap = completions.associate { it.scheduleId to it.isCompleted }
        schedules.map { item ->
            ScheduleWithStatus(
                item = item,
                isCompletedToday = completionMap[item.id] == true
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val nextScheduledItem: StateFlow<ScheduleWithStatus?> = schedulesWithStatus.map { list ->
        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val currentMinute = now.get(Calendar.MINUTE)
        val currentTimeVal = currentHour * 60 + currentMinute

        // Find upcoming incomplete item or earliest incomplete
        val activeItems = list.filter { it.item.isEnabled }
        val upcoming = activeItems.filter { !it.isCompletedToday && (it.item.hour * 60 + it.item.minute) >= currentTimeVal }
            .minByOrNull { it.item.hour * 60 + it.item.minute }

        upcoming ?: activeItems.firstOrNull { !it.isCompletedToday } ?: activeItems.firstOrNull()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun toggleSchedule(scheduleId: String) {
        viewModelScope.launch {
            val completed = repository.toggleScheduleCompletion(scheduleId, todayKey)
            showToast(if (completed) "Activity completed!" else "Marked incomplete")
        }
    }

    fun addSchedule(title: String, timeStr: String, hour: Int, minute: Int, recurrence: String, note: String) {
        viewModelScope.launch {
            val id = "custom_" + UUID.randomUUID().toString().take(8)
            val item = ScheduleItemEntity(
                id = id,
                title = title,
                timeStr = timeStr,
                hour = hour,
                minute = minute,
                recurrence = recurrence,
                note = note,
                isEnabled = true,
                notify = true,
                sortOrder = hour * 60 + minute
            )
            repository.insertSchedule(item)
            alarmScheduler.scheduleScheduleAlarm(item)
            showToast("Schedule added: $title at $timeStr")
        }
    }

    fun updateSchedule(item: ScheduleItemEntity) {
        viewModelScope.launch {
            repository.updateSchedule(item)
            alarmScheduler.scheduleScheduleAlarm(item)
            showToast("Schedule updated")
        }
    }

    fun deleteSchedule(id: String) {
        viewModelScope.launch {
            alarmScheduler.cancelScheduleAlarm(id)
            repository.deleteSchedule(id)
            showToast("Schedule deleted")
        }
    }

    fun resetTodayProgress() {
        viewModelScope.launch {
            repository.resetDayCompletions(todayKey)
            showToast("Today's progress reset")
        }
    }

    // Water
    val todayWaterLogs = repository.getWaterLogsForDate(todayKey).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val todayWaterTotalMl: StateFlow<Int> = todayWaterLogs.map { logs ->
        logs.sumOf { it.amountMl }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val waterSlotsState: StateFlow<List<Boolean>> = todayWaterLogs.map { logs ->
        val slots = MutableList(7) { false }
        logs.forEach { log ->
            if (log.slotIndex in 0..6) {
                slots[log.slotIndex] = true
            }
        }
        slots
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = List(7) { false }
    )

    fun toggleWaterSlot(slotIndex: Int) {
        viewModelScope.launch {
            repository.toggleWaterSlot(slotIndex, todayKey)
            val currentSlotState = waterSlotsState.value.getOrNull(slotIndex) ?: false
            if (!currentSlotState) {
                showToast("Hydration logged: +250 ml")
            } else {
                showToast("Serving unlogged")
            }
        }
    }

    fun addCustomWater(amountMl: Int) {
        viewModelScope.launch {
            repository.addCustomWater(amountMl, todayKey)
            showToast("Logged +$amountMl ml water")
        }
    }

    val allWaterLogs = repository.allWaterLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Habits
    val habitsWithStatus: StateFlow<List<HabitWithStatus>> = combine(
        repository.allHabits,
        repository.allHabitLogs
    ) { habits, logs ->
        habits.map { habit ->
            val habitLogs = logs.filter { it.habitId == habit.id }
            val completedToday = habitLogs.any { it.dateKey == todayKey && it.isCompleted }

            // Calculate current streak
            var streak = 0
            val cal = Calendar.getInstance()
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            // If not completed today, check if completed yesterday to maintain streak
            val todayStr = sdf.format(cal.time)
            val doneToday = habitLogs.any { it.dateKey == todayStr && it.isCompleted }
            if (!doneToday) {
                cal.add(Calendar.DAY_OF_YEAR, -1)
            }

            while (true) {
                val dStr = sdf.format(cal.time)
                val isDone = habitLogs.any { it.dateKey == dStr && it.isCompleted }
                if (isDone) {
                    streak++
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                } else {
                    break
                }
            }

            // Completed this week
            val weekCal = Calendar.getInstance().apply { firstDayOfWeek = Calendar.MONDAY }
            weekCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            var thisWeekCount = 0
            for (i in 0..6) {
                val dKey = sdf.format(weekCal.time)
                if (habitLogs.any { it.dateKey == dKey && it.isCompleted }) {
                    thisWeekCount++
                }
                weekCal.add(Calendar.DAY_OF_YEAR, 1)
            }

            HabitWithStatus(
                habit = habit,
                isCompletedToday = completedToday,
                currentStreak = streak,
                completedThisWeek = thisWeekCount
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun toggleHabit(habitId: String) {
        viewModelScope.launch {
            repository.toggleHabit(habitId, todayKey)
            showToast("Habit updated")
        }
    }

    fun addHabit(title: String, category: String, targetDays: Int) {
        viewModelScope.launch {
            val id = "habit_" + UUID.randomUUID().toString().take(8)
            val habit = HabitEntity(
                id = id,
                title = title,
                category = category,
                targetDaysPerWeek = targetDays
            )
            repository.insertHabit(habit)
            showToast("Habit created: $title")
        }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
            showToast("Habit removed")
        }
    }

    // Workouts
    private val _selectedWorkoutDay = MutableStateFlow(0) // 0..3 for Days 1..4
    val selectedWorkoutDay: StateFlow<Int> = _selectedWorkoutDay.asStateFlow()

    fun selectWorkoutDay(dayIndex: Int) {
        _selectedWorkoutDay.value = dayIndex.coerceIn(0, 3)
    }

    private val _activeWorkoutState = MutableStateFlow(
        ActiveWorkoutState(plan = InitialData.workoutPlans[0])
    )
    val activeWorkoutState: StateFlow<ActiveWorkoutState> = _activeWorkoutState.asStateFlow()

    private var workoutTimerJob: Job? = null
    private var restTimerJob: Job? = null

    fun startWorkoutSession(dayIndex: Int) {
        val plan = InitialData.workoutPlans[dayIndex.coerceIn(0, 3)]
        _activeWorkoutState.value = ActiveWorkoutState(
            plan = plan,
            completedExerciseIds = emptySet(),
            isWorkoutActive = true,
            elapsedSeconds = 0,
            isResting = false
        )
        workoutTimerJob?.cancel()
        workoutTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _activeWorkoutState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }
        }
        showToast("Started ${plan.title} workout session!")
    }

    fun toggleExerciseDone(exercise: Exercise) {
        val current = _activeWorkoutState.value
        val updated = current.completedExerciseIds.toMutableSet()
        val willBeDone = !updated.contains(exercise.id)

        if (willBeDone) {
            updated.add(exercise.id)
            _activeWorkoutState.value = current.copy(completedExerciseIds = updated)
            startRestTimer(exercise.defaultRestSeconds, exercise.name)
        } else {
            updated.remove(exercise.id)
            _activeWorkoutState.value = current.copy(completedExerciseIds = updated)
        }
    }

    private fun startRestTimer(seconds: Int, exerciseName: String) {
        restTimerJob?.cancel()
        _activeWorkoutState.update {
            it.copy(
                isResting = true,
                restRemainingSeconds = seconds,
                currentRestingExerciseName = exerciseName
            )
        }
        restTimerJob = viewModelScope.launch {
            var rem = seconds
            while (rem > 0) {
                delay(1000)
                rem--
                _activeWorkoutState.update { it.copy(restRemainingSeconds = rem) }
            }
            _activeWorkoutState.update { it.copy(isResting = false) }
            showToast("Rest completed! Next set ready.")
        }
    }

    fun skipRestTimer() {
        restTimerJob?.cancel()
        _activeWorkoutState.update { it.copy(isResting = false, restRemainingSeconds = 0) }
    }

    fun finishWorkoutSession() {
        val current = _activeWorkoutState.value
        workoutTimerJob?.cancel()
        restTimerJob?.cancel()

        val isComplete = current.completedExerciseIds.size >= current.plan.exercises.size
        viewModelScope.launch {
            repository.recordWorkoutSession(
                WorkoutSessionEntity(
                    dayNumber = current.plan.dayNumber,
                    dayName = current.plan.title,
                    focusArea = current.plan.subtitle,
                    dateKey = todayKey,
                    durationSeconds = current.elapsedSeconds,
                    completedExercisesCount = current.completedExerciseIds.size,
                    totalExercisesCount = current.plan.exercises.size,
                    isCompleted = isComplete,
                    notes = "Completed on device"
                )
            )
            _activeWorkoutState.value = ActiveWorkoutState(
                plan = current.plan,
                isWorkoutActive = false,
                completedExerciseIds = emptySet()
            )
            showToast("${current.plan.title} workout saved to history!")
        }
    }

    val allWorkoutSessions = repository.allWorkoutSessions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Sleep
    val todaySleepLog = repository.getSleepForDate(todayKey).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val allSleepLogs = repository.allSleepLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun logSleep(bedHour: Int, bedMin: Int, wakeH: Int, wakeM: Int, quality: Int, notes: String) {
        viewModelScope.launch {
            var durationMinutes = (wakeH * 60 + wakeM) - (bedHour * 60 + bedMin)
            if (durationMinutes <= 0) {
                durationMinutes += 24 * 60
            }
            repository.saveSleepLog(
                SleepLogEntity(
                    dateKey = todayKey,
                    bedtimeHour = bedHour,
                    bedtimeMinute = bedMin,
                    wakeHour = wakeH,
                    wakeMinute = wakeM,
                    durationMinutes = durationMinutes,
                    qualityRating = quality,
                    notes = notes
                )
            )
            showToast("Sleep logged: ${(durationMinutes / 60.0).format(1)} hours")
        }
    }

    // Pomodoro / Focus System
    val focusPresets = listOf(
        FocusPreset("Classic", 25, 5, 15, 4),
        FocusPreset("Deep Work", 50, 10, 20, 3),
        FocusPreset("90-Minute", 90, 20, 30, 2),
        FocusPreset("Mini Sprint", 15, 3, 10, 4)
    )

    private val _selectedPreset = MutableStateFlow(focusPresets[0])
    val selectedPreset: StateFlow<FocusPreset> = _selectedPreset.asStateFlow()

    private val _focusMode = MutableStateFlow("Focus") // "Focus", "Short Break", "Long Break"
    val focusMode: StateFlow<String> = _focusMode.asStateFlow()

    private val _focusRunning = MutableStateFlow(false)
    val focusRunning: StateFlow<Boolean> = _focusRunning.asStateFlow()

    private val _focusRemainingSeconds = MutableStateFlow(25 * 60)
    val focusRemainingSeconds: StateFlow<Int> = _focusRemainingSeconds.asStateFlow()

    private val _focusTotalSeconds = MutableStateFlow(25 * 60)
    val focusTotalSeconds: StateFlow<Int> = _focusTotalSeconds.asStateFlow()

    private val _focusCurrentCycle = MutableStateFlow(1)
    val focusCurrentCycle: StateFlow<Int> = _focusCurrentCycle.asStateFlow()

    private val _focusTaskName = MutableStateFlow("Deep Work / Study")
    val focusTaskName: StateFlow<String> = _focusTaskName.asStateFlow()

    private var focusTimerJob: Job? = null

    fun setFocusPreset(preset: FocusPreset) {
        _selectedPreset.value = preset
        _focusMode.value = "Focus"
        _focusCurrentCycle.value = 1
        _focusTotalSeconds.value = preset.focusMinutes * 60
        _focusRemainingSeconds.value = preset.focusMinutes * 60
        _focusRunning.value = false
        focusTimerJob?.cancel()
        alarmScheduler.cancelFocusAlarm()
        showToast("${preset.name} technique selected (${preset.focusMinutes}m focus / ${preset.shortBreakMinutes}m break)")
    }

    fun setFocusTask(task: String) {
        _focusTaskName.value = task
        viewModelScope.launch {
            repository.setFocusTask(task)
        }
    }

    fun toggleFocusTimer() {
        val willRun = !_focusRunning.value
        _focusRunning.value = willRun

        if (willRun) {
            val remSecs = _focusRemainingSeconds.value
            alarmScheduler.scheduleFocusFinishedAlarm(remSecs * 1000L, _focusTaskName.value, _focusMode.value)
            focusTimerJob?.cancel()
            focusTimerJob = viewModelScope.launch {
                while (_focusRemainingSeconds.value > 0 && _focusRunning.value) {
                    delay(1000)
                    _focusRemainingSeconds.update { it - 1 }
                }
                if (_focusRemainingSeconds.value <= 0) {
                    finishFocusInterval()
                }
            }
        } else {
            alarmScheduler.cancelFocusAlarm()
            focusTimerJob?.cancel()
        }
    }

    private fun finishFocusInterval() {
        _focusRunning.value = false
        focusTimerJob?.cancel()
        val currentP = _selectedPreset.value

        if (_focusMode.value == "Focus") {
            // Completed a focus block
            val cycle = _focusCurrentCycle.value
            viewModelScope.launch {
                repository.recordFocusSession(
                    FocusSessionEntity(
                        taskName = _focusTaskName.value,
                        presetName = currentP.name,
                        durationMinutes = currentP.focusMinutes,
                        completedCycles = cycle,
                        targetCycles = currentP.targetCycles,
                        dateKey = todayKey
                    )
                )
            }

            val isLongBreak = (cycle % currentP.targetCycles == 0)
            val nextMode = if (isLongBreak) "Long Break" else "Short Break"
            val breakMins = if (isLongBreak) currentP.longBreakMinutes else currentP.shortBreakMinutes
            _focusMode.value = nextMode
            _focusTotalSeconds.value = breakMins * 60
            _focusRemainingSeconds.value = breakMins * 60

            if (cycle < currentP.targetCycles) {
                _focusCurrentCycle.update { it + 1 }
            } else {
                _focusCurrentCycle.value = 1
            }
            showToast("Focus interval complete! Time for $nextMode.")
        } else {
            // Break finished
            _focusMode.value = "Focus"
            _focusTotalSeconds.value = currentP.focusMinutes * 60
            _focusRemainingSeconds.value = currentP.focusMinutes * 60
            showToast("Break finished! Focus time.")
        }
    }

    fun resetFocusTimer() {
        _focusRunning.value = false
        focusTimerJob?.cancel()
        alarmScheduler.cancelFocusAlarm()
        val currentP = _selectedPreset.value
        _focusMode.value = "Focus"
        _focusTotalSeconds.value = currentP.focusMinutes * 60
        _focusRemainingSeconds.value = currentP.focusMinutes * 60
        showToast("Timer reset")
    }

    fun skipFocusInterval() {
        finishFocusInterval()
    }

    val todayFocusSessions = repository.getFocusSessionsForDate(todayKey).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allFocusSessions = repository.allFocusSessions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // ZLVA Offline AI Assistant State
    private val _isZlvaOpen = MutableStateFlow(false)
    val isZlvaOpen: StateFlow<Boolean> = _isZlvaOpen.asStateFlow()

    fun openZlva() {
        _isZlvaOpen.value = true
    }

    fun closeZlva() {
        _isZlvaOpen.value = false
    }

    private val _zlvaMessages = MutableStateFlow(
        listOf(
            ZlvaMessage(
                isUser = false,
                text = "Welcome back to XARLO. I am ZLVA, your offline AI assistant.\n\nTell me what you'd like to do, such as:\n• \"Add reading every day at 8 PM\"\n• \"Add 250 ml water\"\n• \"Start 25 min focus on Study\"\n• \"Move workout to 7 PM\""
            )
        )
    )
    val zlvaMessages: StateFlow<List<ZlvaMessage>> = _zlvaMessages.asStateFlow()

    fun sendZlvaMessage(prompt: String) {
        if (prompt.isBlank()) return

        val userMsg = ZlvaMessage(isUser = true, text = prompt)
        val aiMsg = ZlvaEngine.processCommand(prompt)

        _zlvaMessages.update { it + userMsg + aiMsg }
    }

    fun executeZlvaIntent(intent: ZlvaIntent) {
        viewModelScope.launch {
            when (intent) {
                is ZlvaIntent.AddSchedule -> {
                    addSchedule(
                        title = intent.title,
                        timeStr = intent.timeStr,
                        hour = intent.hour,
                        minute = intent.minute,
                        recurrence = intent.recurrence,
                        note = intent.note
                    )
                }
                is ZlvaIntent.MoveSchedule -> {
                    val currentSchedules = schedulesWithStatus.value
                    val target = currentSchedules.find {
                        it.item.title.contains(intent.targetQuery, ignoreCase = true) ||
                        it.item.category.contains(intent.targetQuery, ignoreCase = true)
                    }
                    if (target != null) {
                        val updated = target.item.copy(
                            hour = intent.newHour,
                            minute = intent.newMinute,
                            timeStr = intent.newTimeStr
                        )
                        updateSchedule(updated)
                        showToast("Rescheduled '${target.item.title}' to ${intent.newTimeStr}")
                    } else {
                        showToast("Could not find matching schedule item for '${intent.targetQuery}'")
                    }
                }
                is ZlvaIntent.AddWater -> {
                    addCustomWater(intent.amountMl)
                }
                is ZlvaIntent.QueryWater -> {
                    val total = todayWaterTotalMl.value
                    showToast("Hydration today: $total ml")
                }
                is ZlvaIntent.StartFocus -> {
                    _focusTaskName.value = intent.task
                    _focusMode.value = "Focus"
                    _focusTotalSeconds.value = intent.durationMinutes * 60
                    _focusRemainingSeconds.value = intent.durationMinutes * 60
                    toggleFocusTimer()
                    navigateTo("focus")
                }
                is ZlvaIntent.SetFocusPreset -> {
                    val preset = focusPresets.find { it.name.equals(intent.presetName, ignoreCase = true) }
                    if (preset != null) {
                        setFocusPreset(preset)
                    }
                }
                is ZlvaIntent.MarkWorkoutComplete -> {
                    val day = (intent.dayNumber ?: 1) - 1
                    val plan = InitialData.workoutPlans[day.coerceIn(0, 3)]
                    repository.recordWorkoutSession(
                        WorkoutSessionEntity(
                            dayNumber = plan.dayNumber,
                            dayName = plan.title,
                            focusArea = plan.subtitle,
                            dateKey = todayKey,
                            durationSeconds = 45 * 60,
                            completedExercisesCount = plan.exercises.size,
                            totalExercisesCount = plan.exercises.size,
                            isCompleted = true,
                            notes = "Logged via ZLVA"
                        )
                    )
                    showToast("Recorded ${plan.title} as completed!")
                }
                is ZlvaIntent.SetSleepSchedule -> {
                    logSleep(intent.bedtimeHour, intent.bedtimeMinute, intent.wakeHour, intent.wakeMinute, 5, "Set via ZLVA")
                }
                is ZlvaIntent.QuerySchedule -> {
                    navigateTo("schedule")
                }
                is ZlvaIntent.ResetToday -> {
                    resetTodayProgress()
                }
                is ZlvaIntent.SetWaterReminder -> {
                    showToast("Hydration alert set for every ${intent.intervalHours} hours")
                }
                else -> {}
            }
            closeZlva()
        }
    }

    // Settings & Export/Import
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
            showToast("Theme changed to ${mode.name.lowercase().replaceFirstChar { it.uppercase() }}")
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setNotificationsEnabled(enabled)
            showToast(if (enabled) "Notifications enabled" else "Notifications muted")
        }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setVibrationEnabled(enabled)
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setSoundEnabled(enabled)
        }
    }

    suspend fun getBackupJson(): String {
        val data = repository.getAllDataForExport()
        return BackupManager.exportToJson(data)
    }

    suspend fun restoreBackupFromJson(jsonString: String): Boolean {
        return try {
            val data = BackupManager.parseFromJson(jsonString)
            repository.restoreAllData(data)
            showToast("Backup restored successfully!")
            true
        } catch (e: Exception) {
            showToast("Invalid backup file: ${e.message}")
            false
        }
    }
}

private fun Double.format(digits: Int) = "%.${digits}f".format(Locale.getDefault(), this)

class XarloViewModelFactory(
    private val repository: XarloRepository,
    private val alarmScheduler: AlarmScheduler,
    private val notificationHelper: NotificationHelper
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(XarloViewModel::class.java)) {
            return XarloViewModel(repository, alarmScheduler, notificationHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
