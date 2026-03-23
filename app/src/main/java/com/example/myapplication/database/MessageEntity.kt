package com.example.myapplication.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity used to persist conversation messages shown in the chat UI.
 *
 * Messages are ordered by [timestamp] when they are loaded back into the UI.
 */
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val author: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis()
)
