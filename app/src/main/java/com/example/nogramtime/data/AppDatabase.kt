package com.example.nogramtime.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.DayOfWeek

/**
 * Room database containing the blocking rules. A singleton instance is
 * initialised lazily to prevent multiple database instances being created.
 */
@Database(entities = [BlockRule::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun blockRuleDao(): BlockRuleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns a singleton database instance. If the database does not yet
         * exist it will be created. A simple pre‑population step inserts two
         * default rules if no rules are found.
         */
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }

        private fun buildDatabase(appContext: Context): AppDatabase {
            val db = Room.databaseBuilder(appContext, AppDatabase::class.java, "nogram_time.db")
                .fallbackToDestructiveMigration()
                .build()
            // Prepopulate with default rules once the database is created
            CoroutineScope(Dispatchers.IO).launch {
                val dao = db.blockRuleDao()
                if (dao.count() == 0) {
                    // Monday to Friday 08:30–12:15
                    dao.insert(
                        BlockRule(
                            startHour = 8,
                            startMinute = 30,
                            endHour = 12,
                            endMinute = 15,
                            days = listOf(
                                DayOfWeek.MONDAY.value,
                                DayOfWeek.TUESDAY.value,
                                DayOfWeek.WEDNESDAY.value,
                                DayOfWeek.THURSDAY.value,
                                DayOfWeek.FRIDAY.value
                            )
                        )
                    )
                    // Saturday and Sunday 14:00–16:30
                    dao.insert(
                        BlockRule(
                            startHour = 14,
                            startMinute = 0,
                            endHour = 16,
                            endMinute = 30,
                            days = listOf(
                                DayOfWeek.SATURDAY.value,
                                DayOfWeek.SUNDAY.value
                            )
                        )
                    )
                }
            }
            return db
        }
    }
}