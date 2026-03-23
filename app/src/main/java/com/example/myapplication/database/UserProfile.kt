package com.example.myapplication.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing the app's single saved user profile.
 *
 * The fixed default `id = 1` makes this table behave like a small singleton record rather than a
 * collection of independent profiles.
 */
@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val username: String,
    val imagePath: String
)
