package com.jeandealmeida_dev.billortest.chat.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jeandealmeida_dev.billortest.chat.data.local.entity.ChatMessageEntity
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single

/**
 * Data Access Object for chat messages
 */
@Dao
interface ChatMessageDao {
    
    @Query("SELECT * FROM chat_messages ORDER BY created_at ASC")
    fun getAllMessages(): Flowable<List<ChatMessageEntity>>
    
    @Query("SELECT * FROM chat_messages WHERE channel_id = :channelId ORDER BY created_at ASC")
    fun getMessagesByChannel(channelId: String): Flowable<List<ChatMessageEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessage(message: ChatMessageEntity): Single<Long>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessages(messages: List<ChatMessageEntity>): Single<List<Long>>
    
    @Query("DELETE FROM chat_messages")
    fun deleteAllMessages(): Single<Int>
    
    @Query("DELETE FROM chat_messages WHERE id = :messageId")
    fun deleteMessage(messageId: String): Single<Int>
}
