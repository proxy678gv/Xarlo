package com.example.ai.zlva

sealed class ZlvaIntent {
    data class AddSchedule(
        val title: String,
        val hour: Int,
        val minute: Int,
        val timeStr: String,
        val recurrence: String = "DAILY",
        val note: String = ""
    ) : ZlvaIntent()

    data class MoveSchedule(
        val targetQuery: String,
        val newHour: Int,
        val newMinute: Int,
        val newTimeStr: String
    ) : ZlvaIntent()

    data class AddWater(val amountMl: Int) : ZlvaIntent()

    data class QueryWater(val dateStr: String = "today") : ZlvaIntent()

    data class StartFocus(
        val durationMinutes: Int,
        val presetName: String? = null,
        val task: String = "Deep Work"
    ) : ZlvaIntent()

    data class SetFocusPreset(val presetName: String) : ZlvaIntent()

    data class MarkWorkoutComplete(val dayNumber: Int? = null) : ZlvaIntent()

    data class SetSleepSchedule(
        val bedtimeHour: Int,
        val bedtimeMinute: Int,
        val wakeHour: Int = 5,
        val wakeMinute: Int = 30
    ) : ZlvaIntent()

    data class QuerySchedule(val dateStr: String = "today") : ZlvaIntent()

    data class ToggleHabit(val habitTitle: String) : ZlvaIntent()

    data class SetWaterReminder(val intervalHours: Int) : ZlvaIntent()

    object QueryStatus : ZlvaIntent()

    object ResetToday : ZlvaIntent()

    data class ConversationalResponse(
        val reply: String,
        val suggestedPrompt: String? = null
    ) : ZlvaIntent()
}

data class ZlvaMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val pendingIntent: ZlvaIntent? = null,
    val structuredPreviewTitle: String? = null,
    val structuredPreviewDetails: Map<String, String>? = null,
    val timestamp: Long = System.currentTimeMillis()
)
