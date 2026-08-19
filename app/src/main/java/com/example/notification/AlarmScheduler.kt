package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.local.entity.ScheduleItemEntity
import com.example.receiver.AlarmReceiver
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleScheduleAlarm(schedule: ScheduleItemEntity) {
        if (!schedule.isEnabled || !schedule.notify) {
            cancelScheduleAlarm(schedule.id)
            return
        }

        val triggerTime = computeNextTriggerTime(schedule.hour, schedule.minute, schedule.recurrence)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_SCHEDULE_REMINDER
            putExtra(AlarmReceiver.EXTRA_SCHEDULE_ID, schedule.id)
            putExtra(AlarmReceiver.EXTRA_TITLE, schedule.title)
            putExtra(AlarmReceiver.EXTRA_MESSAGE, "${schedule.timeStr} • ${schedule.note}")
            putExtra(AlarmReceiver.EXTRA_HOUR, schedule.hour)
            putExtra(AlarmReceiver.EXTRA_MINUTE, schedule.minute)
            putExtra(AlarmReceiver.EXTRA_RECURRENCE, schedule.recurrence)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // In case of restricted exact alarm permission on Android 12+, fallback gracefully
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    fun cancelScheduleAlarm(scheduleId: String) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_SCHEDULE_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun scheduleFocusFinishedAlarm(durationMillis: Long, taskName: String, mode: String) {
        val triggerTime = System.currentTimeMillis() + durationMillis
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_FOCUS_FINISHED
            putExtra(AlarmReceiver.EXTRA_TITLE, "XARLO • Focus Session Complete")
            putExtra(AlarmReceiver.EXTRA_MESSAGE, "$taskName • Time for a recovery break!")
            putExtra(AlarmReceiver.EXTRA_MODE, mode)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            FOCUS_ALARM_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    fun cancelFocusAlarm() {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_FOCUS_FINISHED
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            FOCUS_ALARM_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    companion object {
        const val FOCUS_ALARM_ID = 888899

        fun computeNextTriggerTime(hour: Int, minute: Int, recurrence: String): Long {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (target.before(now)) {
                target.add(Calendar.DAY_OF_YEAR, 1)
            }

            when (recurrence) {
                "WEEKDAYS" -> {
                    while (target.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                        target.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
                    ) {
                        target.add(Calendar.DAY_OF_YEAR, 1)
                    }
                }
                "WEEKENDS" -> {
                    while (target.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY &&
                        target.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY
                    ) {
                        target.add(Calendar.DAY_OF_YEAR, 1)
                    }
                }
            }

            return target.timeInMillis
        }
    }
}
