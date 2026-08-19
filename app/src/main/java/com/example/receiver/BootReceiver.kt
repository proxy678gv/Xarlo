package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AppDatabase
import com.example.notification.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val scheduler = AlarmScheduler(context)
            val database = AppDatabase.getInstance(context)

            CoroutineScope(Dispatchers.IO).launch {
                val activeSchedules = database.scheduleDao().getActiveSchedules()
                for (schedule in activeSchedules) {
                    if (schedule.notify) {
                        scheduler.scheduleScheduleAlarm(schedule)
                    }
                }
            }
        }
    }
}
