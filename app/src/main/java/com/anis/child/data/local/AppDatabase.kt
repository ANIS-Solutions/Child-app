package com.anis.child.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        LocationTelemetryEntity::class,
        SessionEntity::class,
        AnalysisResultEntity::class,
        TaskEntity::class,
        RewardEntity::class,
        AppRestrictionEntity::class,
        ScreenTimeConfigEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun locationTelemetryDao(): LocationTelemetryDao
    abstract fun sessionDao(): SessionDao
    abstract fun analysisResultDao(): AnalysisResultDao
    abstract fun taskDao(): TaskDao
    abstract fun rewardDao(): RewardDao
    abstract fun appRestrictionDao(): AppRestrictionDao
    abstract fun screenTimeConfigDao(): ScreenTimeConfigDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = Migration(1, 2) { db ->
            db.execSQL("CREATE TABLE IF NOT EXISTS sessions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "startTime INTEGER NOT NULL, " +
                    "endTime INTEGER, " +
                    "status TEXT NOT NULL DEFAULT 'Idle', " +
                    "intervalMs INTEGER NOT NULL DEFAULT 1000, " +
                    "totalCaptures INTEGER NOT NULL DEFAULT 0, " +
                    "blockedCount INTEGER NOT NULL DEFAULT 0, " +
                    "safeCount INTEGER NOT NULL DEFAULT 0, " +
                    "batteryStart INTEGER NOT NULL DEFAULT 0, " +
                    "batteryEnd INTEGER, " +
                    "batteryCharging INTEGER NOT NULL DEFAULT 0, " +
                    "cpuTimeMs INTEGER NOT NULL DEFAULT 0, " +
                    "cpuUsagePercent REAL NOT NULL DEFAULT 0.0, " +
                    "ramPssMb REAL NOT NULL DEFAULT 0.0)")

            db.execSQL("CREATE TABLE IF NOT EXISTS analysis_results (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "sessionId INTEGER NOT NULL, " +
                    "timestamp INTEGER NOT NULL, " +
                    "analysisResult TEXT NOT NULL DEFAULT 'Safe', " +
                    "decision TEXT NOT NULL DEFAULT 'SAFE', " +
                    "ocrTimeMs INTEGER NOT NULL DEFAULT 0, " +
                    "onnxTimeMs INTEGER NOT NULL DEFAULT 0, " +
                    "preprocessTimeMs INTEGER NOT NULL DEFAULT 0, " +
                    "threatDetails TEXT, " +
                    "imagePath TEXT, " +
                    "FOREIGN KEY (sessionId) REFERENCES sessions(id) ON DELETE CASCADE)")

            db.execSQL("CREATE INDEX IF NOT EXISTS idx_analysis_results_sessionId ON analysis_results(sessionId)")

            db.execSQL("CREATE TABLE IF NOT EXISTS tasks (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "remoteId TEXT NOT NULL DEFAULT '', " +
                    "title TEXT NOT NULL, " +
                    "description TEXT NOT NULL DEFAULT '', " +
                    "dueDate INTEGER, " +
                    "rewardValue INTEGER NOT NULL DEFAULT 0, " +
                    "status TEXT NOT NULL DEFAULT 'pending', " +
                    "completedAt INTEGER, " +
                    "createdAt INTEGER NOT NULL)")

            db.execSQL("CREATE TABLE IF NOT EXISTS rewards (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "remoteId TEXT NOT NULL DEFAULT '', " +
                    "type TEXT NOT NULL DEFAULT 'screen_time', " +
                    "title TEXT NOT NULL DEFAULT '', " +
                    "description TEXT NOT NULL DEFAULT '', " +
                    "pointCost INTEGER NOT NULL DEFAULT 0, " +
                    "state TEXT NOT NULL DEFAULT 'earned', " +
                    "earnedAt INTEGER NOT NULL, " +
                    "expiresAt INTEGER, " +
                    "consumedAt INTEGER)")
        }

        val MIGRATION_2_3 = Migration(2, 3) { db ->
            db.execSQL("CREATE TABLE IF NOT EXISTS app_restrictions (" +
                    "packageName TEXT NOT NULL PRIMARY KEY, " +
                    "label TEXT NOT NULL DEFAULT '', " +
                    "category TEXT NOT NULL DEFAULT 'General', " +
                    "isBlocked INTEGER NOT NULL DEFAULT 0, " +
                    "dailyTimeLimitMinutes INTEGER NOT NULL DEFAULT 0, " +
                    "isAllowListed INTEGER NOT NULL DEFAULT 0)")

            db.execSQL("CREATE TABLE IF NOT EXISTS screen_time_config (" +
                    "id INTEGER NOT NULL PRIMARY KEY DEFAULT 1, " +
                    "dailyLimitMinutes INTEGER NOT NULL DEFAULT 0, " +
                    "bedtimeStartHour INTEGER NOT NULL DEFAULT 21, " +
                    "bedtimeStartMinute INTEGER NOT NULL DEFAULT 0, " +
                    "bedtimeEndHour INTEGER NOT NULL DEFAULT 7, " +
                    "bedtimeEndMinute INTEGER NOT NULL DEFAULT 0, " +
                    "studyStartHour INTEGER NOT NULL DEFAULT 8, " +
                    "studyStartMinute INTEGER NOT NULL DEFAULT 0, " +
                    "studyEndHour INTEGER NOT NULL DEFAULT 14, " +
                    "studyEndMinute INTEGER NOT NULL DEFAULT 0, " +
                    "temporaryRestrictionUntil INTEGER, " +
                    "extraTimeEarnedMinutes INTEGER NOT NULL DEFAULT 0)")

            db.execSQL("INSERT OR IGNORE INTO screen_time_config (id) VALUES (1)")
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "anis_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
