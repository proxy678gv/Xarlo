package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ScheduleItemEntity
import com.example.notification.AlarmScheduler
import com.example.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationHelper = NotificationHelper(context)

        when (intent.action) {
            ACTION_SCHEDULE_REMINDER -> {
                val scheduleId = intent.getStringExtra(EXTRA_SCHEDULE_ID) ?: "schedule"
                val title = intent.getStringExtra(EXTRA_TITLE) ?: "Scheduled Activity"
                val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "Time for your scheduled routine!"
                val hour = intent.getIntExtra(EXTRA_HOUR, -1)
                val minute = intent.getIntExtra(EXTRA_MINUTE, -1)
                val recurrence = intent.getStringExtra(EXTRA_RECURRENCE) ?: "DAILY"

                notificationHelper.showScheduleNotification(
                    id = scheduleId.hashCode(),
                    title = title,
                    message = message
                )

                // Reschedule if repeating
                if (recurrence != "ONCE" && hour != -1 && minute != -1) {
                    val scheduler = AlarmScheduler(context)
                    val dummyEntity = ScheduleItemEntity(
                        id = scheduleId,
                        title = title,
                        timeStr = "",
                        hour = hour,
                        minute = minute,
                        note = "",
                        recurrence = recurrence,
                        isEnabled = true,
                        notify = true
                    )
                    scheduler.scheduleScheduleAlarm(dummyEntity)
                }
            }

            ACTION_FOCUS_FINISHED -> {
                val title = intent.getStringExtra(EXTRA_TITLE) ?: "Focus Finished"
                val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "Time to take a break!"
                notificationHelper.showFocusNotification(
                    id = AlarmScheduler.FOCUS_ALARM_ID,
                    title = title,
                    message = message
                )
            }

            ACTION_WATER_REMINDER -> {
                val title = intent.getStringExtra(EXTRA_TITLE) ?: "Hydration Check"
                val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "Drink 1 glass of water (250 ml)."
                notificationHelper.showWaterNotification(
                    id = 777123,
                    title = title,
                    message = message
                )
            }
        }
    }

    companion object {
        const val ACTION_SCHEDULE_REMINDER = "com.aistudio.xarlo.ACTION_SCHEDULE_REMINDER"
        const val ACTION_FOCUS_FINISHED = "com.aistudio.xarlo.ACTION_FOCUS_FINISHED"
        const val ACTION_WATER_REMINDER = "com.aistudio.xarlo.ACTION_WATER_REMINDER"

        const val EXTRA_SCHEDULE_ID = "extra_schedule_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_MESSAGE = "extra_message"
        const val EXTRA_HOUR = "extra_hour"
        const val EXTRA_MINUTE = "extra_minute"
        const val EXTRA_RECURRENCE = "extra_recurrence"
        const val EXTRA_MODE = "extra_mode"
    }
}
