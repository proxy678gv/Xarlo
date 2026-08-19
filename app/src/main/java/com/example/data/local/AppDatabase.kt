package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.*
import com.example.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ScheduleItemEntity::class,
        ScheduleCompletionEntity::class,
        WaterLogEntity::class,
        HabitEntity::class,
        HabitLogEntity::class,
        WorkoutSessionEntity::class,
        SleepLogEntity::class,
        FocusSessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scheduleDao(): ScheduleDao
    abstract fun waterDao(): WaterDao
    abstract fun habitDao(): HabitDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun sleepDao(): SleepDao
    abstract fun focusDao(): FocusDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "xarlo_database.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getInstance(context)
                            database.scheduleDao().insertAll(InitialData.defaultSchedules)
                            database.habitDao().insertAllHabits(InitialData.defaultHabits)
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
