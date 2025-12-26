package com.jeandealmeida_dev.billortest.chat.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jeandealmeida_dev.billortest.chat.data.local.entity.ChatMessageEntity
import com.jeandealmeida_dev.billortest.chat.domain.model.ChatMessage
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for chat messages
 */
@Dao
interface ChatMessageDao {
    
    @Query("SELECT * FROM chat_messages WHERE channel_id = :channelId ORDER BY created_at ASC")
    fun getMessagesByChannel(channelId: String): Flow<List<ChatMessageEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessageEntity>): List<Long>
    
    @Query("DELETE FROM chat_messages")
    suspend fun deleteAllMessages(): Int
    
    @Query("DELETE FROM chat_messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String): Int
}
