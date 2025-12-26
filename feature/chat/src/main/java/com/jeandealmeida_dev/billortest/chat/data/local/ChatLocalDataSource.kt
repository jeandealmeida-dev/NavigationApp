package com.jeandealmeida_dev.billortest.chat.data.local

import com.jeandealmeida_dev.billortest.chat.data.local.dao.ChatMessageDao
import com.jeandealmeida_dev.billortest.chat.data.local.entity.ChatMessageEntity
import com.jeandealmeida_dev.billortest.chat.domain.model.ChatMessage
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Local data source implementation for chat messages using Room
 */
class ChatLocalDataSource @Inject constructor(
    private val chatMessageDao: ChatMessageDao
) {

    fun getMessagesByChannel(channelId: String): Flow<List<ChatMessageEntity>> {
        return chatMessageDao.getMessagesByChannel(channelId)
    }

    suspend fun insertMessage(message: ChatMessageEntity): Long {
        return chatMessageDao.insertMessage(message)
    }

    suspend fun insertMessages(messages: List<ChatMessageEntity>): List<Long> {
        return chatMessageDao.insertMessages(messages)
    }

    suspend fun deleteAllMessages(): Int {
        return chatMessageDao.deleteAllMessages()
    }

    suspend fun deleteMessage(messageId: String): Int {
        return chatMessageDao.deleteMessage(messageId)
    }
}
