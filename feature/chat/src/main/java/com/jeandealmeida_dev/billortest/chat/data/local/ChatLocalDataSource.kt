package com.jeandealmeida_dev.billortest.chat.data.local

import com.jeandealmeida_dev.billortest.chat.data.local.dao.ChatMessageDao
import com.jeandealmeida_dev.billortest.chat.data.local.entity.ChatMessageEntity
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

/**
 * Local data source implementation for chat messages using Room
 */
class ChatLocalDataSource @Inject constructor(
    private val chatMessageDao: ChatMessageDao
) {
    
    fun getAllMessages(): Flowable<List<ChatMessageEntity>> {
        return chatMessageDao.getAllMessages()
    }
    
    fun getMessagesByChannel(channelId: String): Flowable<List<ChatMessageEntity>> {
        return chatMessageDao.getMessagesByChannel(channelId)
    }
    
    fun insertMessage(message: ChatMessageEntity): Single<Long> {
        return chatMessageDao.insertMessage(message)
    }
    
    fun insertMessages(messages: List<ChatMessageEntity>): Single<List<Long>> {
        return chatMessageDao.insertMessages(messages)
    }
    
    fun deleteAllMessages(): Single<Int> {
        return chatMessageDao.deleteAllMessages()
    }
    
    fun deleteMessage(messageId: String): Single<Int> {
        return chatMessageDao.deleteMessage(messageId)
    }
}
