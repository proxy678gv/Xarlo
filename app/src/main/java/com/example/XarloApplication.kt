package com.example

import android.app.Application
import com.example.data.datastore.UserPreferencesDataStore
import com.example.data.local.AppDatabase
import com.example.data.repository.XarloRepository
import com.example.notification.AlarmScheduler
import com.example.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class XarloApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var preferencesDataStore: UserPreferencesDataStore
        private set

    lateinit var repository: XarloRepository
        private set

    lateinit var notificationHelper: NotificationHelper
        private set

    lateinit var alarmScheduler: AlarmScheduler
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        preferencesDataStore = UserPreferencesDataStore(this)
        repository = XarloRepository(database, preferencesDataStore)
        notificationHelper = NotificationHelper(this)
        alarmScheduler = AlarmScheduler(this)

        CoroutineScope(Dispatchers.IO).launch {
            repository.ensureInitialDataLoaded()
        }
    }
}
