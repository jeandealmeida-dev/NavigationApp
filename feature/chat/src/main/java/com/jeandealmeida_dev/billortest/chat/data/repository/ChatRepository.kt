package com.jeandealmeida_dev.billortest.chat.data.repository

import com.jeandealmeida_dev.billortest.chat.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for chat operations
 */
interface ChatRepository {

    /**
     * Get messages for a specific channel
     */
    suspend fun getMessagesByChannel(channelId: String): Flow<List<ChatMessage>>

    /**
     * Send a new message
     */
    suspend fun sendMessage(
        message: String,
        userId: String,
        userName: String,
        channelId: String? = null
    ): ChatMessage

    /**
     * Send a new audio message
     */
    suspend fun sendAudioMessage(
        audioUrl: String,
        audioDuration: Int,
        userId: String,
        userName: String,
        channelId: String? = null
    ): ChatMessage

    /**
     * Send a new typing status
     */
    suspend fun sendTypingStatus(
        isTyping: Boolean,
        userId: String,
        userName: String,
        channelId: String
    )

    /**
     * Subscribe to realtime message updates
     */
    suspend fun subscribeToRealtimeMessages(channelId: String)

    /**
     * Subscribe to realtime typing status updates
     */
    suspend fun subscribeToRealtimeTypingStatus(channelId: String): Flow<List<String>>

    /**
     * Clear all local messages
     */
    suspend fun clearMessages(): Int
}
