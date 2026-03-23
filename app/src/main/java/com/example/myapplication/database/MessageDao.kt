package com.example.myapplication.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO for the `messages` table.
 */
@Dao
interface MessageDao {
    /** Reactive stream of the full conversation ordered from oldest to newest. */
    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<MessageEntity>>

    /** One-shot variant of [getAllMessages] for callers that do not need a flow. */
    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    suspend fun getAllMessagesOnce(): List<MessageEntity>

    /** Persists a new chat message row. */
    @Insert
    suspend fun insertMessage(message: MessageEntity)

    /** Deletes a single persisted message. */
    @Delete
    suspend fun deleteMessage(message: MessageEntity)

    /** Removes every message from the local history. */
    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()

    /** Returns the current number of stored messages. */
    @Query("SELECT COUNT(*) FROM messages")
    suspend fun getMessageCount(): Int
}
