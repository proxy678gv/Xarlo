package com.example.ai.zlva

import java.util.Locale
import java.util.regex.Pattern

object ZlvaEngine {

    fun processCommand(input: String): ZlvaMessage {
        val trimmed = input.trim()
        val lower = trimmed.lowercase(Locale.ROOT)

        // 1. Water Intake Query ("How much water have I had today?", "water status", etc.)
        if (lower.contains("how much water") || (lower.contains("water") && (lower.contains("today") || lower.contains("status") || lower.contains("consumed") || lower.contains("drunk") || lower.contains("drank")) && !lower.contains("add"))) {
            return ZlvaMessage(
                isUser = false,
                text = "Let me check your hydration intake for today.",
                pendingIntent = ZlvaIntent.QueryWater("today")
            )
        }

        // 2. Add Water ("Add 250 ml water", "drank 500ml water", "add glass of water", etc.)
        if ((lower.contains("water") || lower.contains("hydration")) && (lower.contains("add") || lower.contains("drank") || lower.contains("drink") || lower.contains("+") || lower.contains("log"))) {
            val amount = extractWaterAmount(lower) ?: 250
            return ZlvaMessage(
                isUser = false,
                text = "I've structured a water intake entry of $amount ml.",
                pendingIntent = ZlvaIntent.AddWater(amount),
                structuredPreviewTitle = "Log Water Intake",
                structuredPreviewDetails = mapOf(
                    "Amount" to "$amount ml",
                    "Target" to "Today's Goal"
                )
            )
        }

        // 3. Water Reminder ("Remind me to drink water every 2 hours", etc.)
        if (lower.contains("remind") && lower.contains("water")) {
            val interval = extractIntervalHours(lower) ?: 2
            return ZlvaMessage(
                isUser = false,
                text = "I'll schedule hydration alerts every $interval hours.",
                pendingIntent = ZlvaIntent.SetWaterReminder(interval),
                structuredPreviewTitle = "Hydration Interval Alert",
                structuredPreviewDetails = mapOf(
                    "Interval" to "Every $interval hours",
                    "Type" to "Local notification"
                )
            )
        }

        // 4. Start / Configure Focus Session ("Start a 25 minute focus session", "Start 50 min pomodoro", etc.)
        if (lower.contains("focus") || lower.contains("pomodoro") || lower.contains("deep work") || lower.contains("sprint")) {
            if (lower.contains("deep work")) {
                return ZlvaMessage(
                    isUser = false,
                    text = "Configuring Deep Work focus preset (50m focus / 10m break).",
                    pendingIntent = ZlvaIntent.SetFocusPreset("Deep Work"),
                    structuredPreviewTitle = "Apply Focus Preset",
                    structuredPreviewDetails = mapOf(
                        "Preset" to "Deep Work",
                        "Focus Time" to "50 minutes",
                        "Break Time" to "10 minutes",
                        "Cycles" to "3 cycles"
                    )
                )
            } else if (lower.contains("classic")) {
                return ZlvaMessage(
                    isUser = false,
                    text = "Configuring Classic Pomodoro preset (25m focus / 5m break).",
                    pendingIntent = ZlvaIntent.SetFocusPreset("Classic"),
                    structuredPreviewTitle = "Apply Focus Preset",
                    structuredPreviewDetails = mapOf(
                        "Preset" to "Classic",
                        "Focus Time" to "25 minutes",
                        "Break Time" to "5 minutes",
                        "Cycles" to "4 cycles"
                    )
                )
            } else if (lower.contains("90") || lower.contains("ultradian")) {
                return ZlvaMessage(
                    isUser = false,
                    text = "Configuring 90-Minute focus preset.",
                    pendingIntent = ZlvaIntent.SetFocusPreset("90-Minute"),
                    structuredPreviewTitle = "Apply Focus Preset",
                    structuredPreviewDetails = mapOf(
                        "Preset" to "90-Minute",
                        "Focus Time" to "90 minutes",
                        "Break Time" to "20 minutes"
                    )
                )
            } else if (lower.contains("sprint") || lower.contains("15 min")) {
                return ZlvaMessage(
                    isUser = false,
                    text = "Configuring Mini Sprint focus preset.",
                    pendingIntent = ZlvaIntent.SetFocusPreset("Mini Sprint"),
                    structuredPreviewTitle = "Apply Focus Preset",
                    structuredPreviewDetails = mapOf(
                        "Preset" to "Mini Sprint",
                        "Focus Time" to "15 minutes",
                        "Break Time" to "3 minutes"
                    )
                )
            }

            val duration = extractMinutes(lower) ?: 25
            val task = extractTask(trimmed) ?: "Deep Focus"
            return ZlvaMessage(
                isUser = false,
                text = "Ready to start a $duration minute focus session for '$task'.",
                pendingIntent = ZlvaIntent.StartFocus(duration, task = task),
                structuredPreviewTitle = "Start Focus Session",
                structuredPreviewDetails = mapOf(
                    "Duration" to "$duration minutes",
                    "Task" to task
                )
            )
        }

        // 5. Move / Reschedule Schedule Item ("Move my workout to 7 PM", "reschedule study to 8:30 PM", etc.)
        if (lower.contains("move") || lower.contains("reschedule") || lower.contains("shift") || lower.contains("change time")) {
            val (hour, minute, timeStr) = extractTime(lower) ?: Triple(19, 0, "7:00 PM")
            val target = extractMoveTarget(lower) ?: "workout"
            return ZlvaMessage(
                isUser = false,
                text = "I'll reschedule '$target' to $timeStr.",
                pendingIntent = ZlvaIntent.MoveSchedule(target, hour, minute, timeStr),
                structuredPreviewTitle = "Move Schedule",
                structuredPreviewDetails = mapOf(
                    "Activity" to target.replaceFirstChar { it.uppercase() },
                    "New Time" to timeStr
                )
            )
        }

        // 6. Mark Workout Complete ("Mark today's workout complete", "completed workout", etc.)
        if (lower.contains("workout") && (lower.contains("complete") || lower.contains("done") || lower.contains("finish") || lower.contains("mark"))) {
            val dayNum = extractDayNumber(lower)
            val dayLabel = if (dayNum != null) "Day $dayNum" else "Today's Workout"
            return ZlvaMessage(
                isUser = false,
                text = "Great job on training! Recording $dayLabel as complete.",
                pendingIntent = ZlvaIntent.MarkWorkoutComplete(dayNum),
                structuredPreviewTitle = "Complete Workout",
                structuredPreviewDetails = mapOf(
                    "Workout" to dayLabel,
                    "Status" to "Completed"
                )
            )
        }

        // 7. Change Sleep Time ("Change my sleep time to 10 PM", "set bedtime to 11 PM", etc.)
        if (lower.contains("sleep") || lower.contains("bedtime")) {
            if (lower.contains("to") || lower.contains("at") || lower.contains("set") || lower.contains("change")) {
                val (hour, minute, timeStr) = extractTime(lower) ?: Triple(22, 0, "10:00 PM")
                return ZlvaMessage(
                    isUser = false,
                    text = "Updating planned sleep bedtime to $timeStr.",
                    pendingIntent = ZlvaIntent.SetSleepSchedule(hour, minute),
                    structuredPreviewTitle = "Update Sleep Plan",
                    structuredPreviewDetails = mapOf(
                        "Bedtime" to timeStr,
                        "Wake Time" to "5:30 AM",
                        "Planned Sleep" to "7.5 hours"
                    )
                )
            }
        }

        // 8. Show Schedule / Status ("Show today's schedule", "what is on my plan", "schedule", etc.)
        if (lower.contains("schedule") || lower.contains("routine") || lower.contains("agenda") || lower.contains("plan")) {
            if (lower.contains("show") || lower.contains("what") || lower.contains("view") || lower.contains("list") || lower.contains("today")) {
                return ZlvaMessage(
                    isUser = false,
                    text = "Here is your plan and upcoming items for today.",
                    pendingIntent = ZlvaIntent.QuerySchedule("today")
                )
            }
        }

        // 9. Reset Today ("Reset today's tasks", "reset day", etc.)
        if (lower.contains("reset") && (lower.contains("today") || lower.contains("day") || lower.contains("task") || lower.contains("schedule"))) {
            return ZlvaMessage(
                isUser = false,
                text = "This will reset all completion checkmarks and water glasses for today. Confirm below:",
                pendingIntent = ZlvaIntent.ResetToday,
                structuredPreviewTitle = "Reset Today's Progress",
                structuredPreviewDetails = mapOf(
                    "Action" to "Clear completed checks",
                    "Water" to "Reset to 0 ml"
                )
            )
        }

        // 10. Add Schedule ("Add reading every day at 8 PM", "Add workout tomorrow at 6 PM", "Add study at 8 PM every day", etc.)
        if (lower.contains("add") || lower.contains("create") || lower.contains("schedule") || lower.contains("remind")) {
            val (hour, minute, timeStr) = extractTime(lower) ?: Triple(20, 0, "8:00 PM")
            val recurrence = extractRecurrence(lower)
            val title = extractTitle(trimmed) ?: "Scheduled Activity"

            return ZlvaMessage(
                isUser = false,
                text = "I've structured a new schedule entry for '$title'.",
                pendingIntent = ZlvaIntent.AddSchedule(
                    title = title,
                    hour = hour,
                    minute = minute,
                    timeStr = timeStr,
                    recurrence = recurrence,
                    note = "Added via ZLVA offline assistant"
                ),
                structuredPreviewTitle = "Add Schedule Item",
                structuredPreviewDetails = mapOf(
                    "Title" to title,
                    "Time" to timeStr,
                    "Recurrence" to recurrence.replaceFirstChar { it.uppercase() }
                )
            )
        }

        // 11. Greeting & Help
        if (lower.contains("hi") || lower.contains("hello") || lower.contains("hey") || lower.contains("who are you") || lower.contains("help")) {
            return ZlvaMessage(
                isUser = false,
                text = "Greetings! I am ZLVA, your offline intelligence assistant in XARLO.\n\nI can execute local commands for you:\n• \"Add reading every day at 8 PM\"\n• \"Add 250 ml water\"\n• \"Start a 50 min Deep Work session\"\n• \"Move workout to 7 PM\"\n• \"Mark today's workout complete\"\n• \"Change sleep time to 10 PM\"\n\nEverything runs completely offline on your device.",
                pendingIntent = null
            )
        }

        // 12. Fallback conversational
        return ZlvaMessage(
            isUser = false,
            text = "I understood your query: \"$trimmed\".\n\nTry commands like:\n• \"Add study every day at 8 PM\"\n• \"Add 500 ml water\"\n• \"Start 25 min focus\"\n• \"Move workout to 7 PM\"",
            pendingIntent = null
        )
    }

    private fun extractTime(text: String): Triple<Int, Int, String>? {
        // Match patterns like "8:30 PM", "8 PM", "20:00", "5:30 am", "at 6", "at 7pm"
        val regex = Pattern.compile("(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?", Pattern.CASE_INSENSITIVE)
        val matcher = regex.matcher(text)

        var lastMatch: Triple<Int, Int, String>? = null
        while (matcher.find()) {
            val rawHour = matcher.group(1)?.toIntOrNull() ?: continue
            val rawMinute = matcher.group(2)?.toIntOrNull() ?: 0
            val ampm = matcher.group(3)?.lowercase(Locale.ROOT)

            var hour = rawHour
            if (ampm == "pm" && hour < 12) hour += 12
            if (ampm == "am" && hour == 12) hour = 0

            // Format standard 12-hour display string
            val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
            val displayAmPm = if (hour >= 12) "PM" else "AM"
            val timeStr = String.format(Locale.getDefault(), "%d:%02d %s", displayHour, rawMinute, displayAmPm)

            lastMatch = Triple(hour, rawMinute, timeStr)
        }
        return lastMatch
    }

    private fun extractWaterAmount(text: String): Int? {
        val regex = Pattern.compile("(\\d+)\\s*(ml|glass|serving|oz)?", Pattern.CASE_INSENSITIVE)
        val matcher = regex.matcher(text)
        while (matcher.find()) {
            val num = matcher.group(1)?.toIntOrNull()
            val unit = matcher.group(2)?.lowercase(Locale.ROOT)
            if (num != null) {
                return when (unit) {
                    "glass", "serving" -> num * 250
                    "oz" -> (num * 29.57).toInt()
                    else -> if (num < 20) num * 250 else num
                }
            }
        }
        if (text.contains("glass")) return 250
        return null
    }

    private fun extractIntervalHours(text: String): Int? {
        val regex = Pattern.compile("every\\s+(\\d+)\\s*(hour|hr)", Pattern.CASE_INSENSITIVE)
        val matcher = regex.matcher(text)
        if (matcher.find()) {
            return matcher.group(1)?.toIntOrNull()
        }
        return null
    }

    private fun extractMinutes(text: String): Int? {
        val regex = Pattern.compile("(\\d+)\\s*(min|minute)", Pattern.CASE_INSENSITIVE)
        val matcher = regex.matcher(text)
        if (matcher.find()) {
            return matcher.group(1)?.toIntOrNull()
        }
        return null
    }

    private fun extractDayNumber(text: String): Int? {
        val regex = Pattern.compile("day\\s*([1-4])", Pattern.CASE_INSENSITIVE)
        val matcher = regex.matcher(text)
        if (matcher.find()) {
            return matcher.group(1)?.toIntOrNull()
        }
        return null
    }

    private fun extractRecurrence(text: String): String {
        return when {
            text.contains("every day") || text.contains("daily") -> "DAILY"
            text.contains("weekday") || text.contains("mon to fri") -> "WEEKDAYS"
            text.contains("weekend") -> "WEEKENDS"
            text.contains("weekly") || text.contains("every week") -> "WEEKLY"
            text.contains("tomorrow") || text.contains("once") || text.contains("today") -> "ONCE"
            else -> "DAILY"
        }
    }

    private fun extractTitle(raw: String): String? {
        // Strip common prefixes
        var s = raw
        val prefixes = listOf(
            "add ", "schedule ", "create ", "set up ", "remind me to ", "remind me "
        )
        for (p in prefixes) {
            if (s.startsWith(p, ignoreCase = true)) {
                s = s.substring(p.length)
                break
            }
        }

        // Cut off time or recurrence suffixes
        val suffixes = listOf(
            " at ", " every ", " tomorrow", " today", " daily", " on "
        )
        var bestCut = s.length
        for (suf in suffixes) {
            val idx = s.indexOf(suf, ignoreCase = true)
            if (idx != -1 && idx < bestCut && idx > 0) {
                bestCut = idx
            }
        }

        val candidate = s.substring(0, bestCut).trim()
        return if (candidate.isNotEmpty()) candidate.replaceFirstChar { it.uppercase() } else null
    }

    private fun extractTask(text: String): String? {
        val idx = text.indexOf("for ", ignoreCase = true)
        if (idx != -1) {
            val candidate = text.substring(idx + 4).trim()
            if (candidate.isNotEmpty()) return candidate.replaceFirstChar { it.uppercase() }
        }
        return null
    }

    private fun extractMoveTarget(text: String): String? {
        var s = text
        val moveIdx = s.indexOf("move ", ignoreCase = true)
        if (moveIdx != -1) {
            s = s.substring(moveIdx + 5)
        }
        val toIdx = s.indexOf(" to ", ignoreCase = true)
        if (toIdx != -1) {
            s = s.substring(0, toIdx)
        }
        s = s.replace("my ", "", ignoreCase = true).trim()
        return if (s.isNotEmpty()) s else null
    }
}
