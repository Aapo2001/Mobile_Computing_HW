package com.example.myapplication.repository

import android.content.Context
import com.example.myapplication.database.AppDatabase
import com.example.myapplication.database.MessageEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository wrapper around [com.example.myapplication.database.MessageDao].
 *
 * The repository keeps message construction in one place so UI code does not need to know about
 * database entities or timestamps.
 */
class MessageRepository(context: Context) {
    private val messageDao = AppDatabase.getDatabase(context).messageDao()

    /** Reactive stream of all saved messages in display order. */
    fun getAllMessages(): Flow<List<MessageEntity>> = messageDao.getAllMessages()

    /** One-shot read of the full conversation history. */
    suspend fun getAllMessagesOnce(): List<MessageEntity> = messageDao.getAllMessagesOnce()

    /** Creates and inserts a message authored by either the user or Gemma. */
    suspend fun insertMessage(author: String, body: String) {
        messageDao.insertMessage(
            MessageEntity(
                author = author,
                body = body
            )
        )
    }

    /** Deletes a single message row. */
    suspend fun deleteMessage(message: MessageEntity) {
        messageDao.deleteMessage(message)
    }

    /** Clears the entire stored conversation. */
    suspend fun deleteAllMessages() {
        messageDao.deleteAllMessages()
    }

    /** Returns the number of persisted messages. */
    suspend fun getMessageCount(): Int = messageDao.getMessageCount()
}
