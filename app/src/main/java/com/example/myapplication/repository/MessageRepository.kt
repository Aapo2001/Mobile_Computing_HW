package com.example.myapplication.repository

import android.content.Context
import com.example.myapplication.database.AppDatabase
import com.example.myapplication.database.MessageEntity
import kotlinx.coroutines.flow.Flow

class MessageRepository(context: Context) {
    private val messageDao = AppDatabase.getDatabase(context).messageDao()

    fun getAllMessages(): Flow<List<MessageEntity>> = messageDao.getAllMessages()

    suspend fun insertMessage(author: String, body: String) {
        messageDao.insertMessage(
            MessageEntity(
                author = author,
                body = body
            )
        )
    }
}
