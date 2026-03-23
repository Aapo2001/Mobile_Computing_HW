package com.example.myapplication.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room database used by the application.
 *
 * The schema is intentionally small for this coursework project and contains:
 *
 * - [UserProfile] for the single stored profile row
 * - [MessageEntity] for persisted chat history
 *
 * The singleton accessor ensures every repository talks to the same database instance.
 */
@Database(entities = [UserProfile::class, MessageEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    /** DAO for reading and updating the single saved user profile. */
    abstract fun userProfileDao(): UserProfileDao

    /** DAO for reading and mutating the chat message history. */
    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns the process-wide [AppDatabase] singleton.
         *
         * `allowMainThreadQueries()` and destructive migration fallback are enabled here to keep
         * the coursework setup simple, even though both choices would be reconsidered in a
         * production application.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
