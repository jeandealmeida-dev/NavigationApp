package com.jeandealmeida_dev.billortest.chat.data.remote

import com.jeandealmeida_dev.billortest.chat.data.remote.model.ChatMessageDto
import kotlinx.coroutines.flow.Flow

interface ChatRemoteDataSource {

    suspend fun sendMessage(
        message: String,
        userId: String,
        userName: String,
        channelId: String? = null
    ): ChatMessageDto

    suspend fun sendAudioMessage(
        audioUrl: String,
        audioDuration: Int,
        userId: String,
        userName: String,
        channelId: String? = null
    ): ChatMessageDto

    suspend fun sendTypingStatus(
        isTyping: Boolean,
        userId: String,
        userName: String,
        channelId: String
    )

    suspend fun subscribeToMessages(
        channelId: String
    ): Flow<ChatMessageDto>

    suspend fun getMessages(): List<ChatMessageDto>

    suspend fun subscribeToTypingStatus(channelId: String): Flow<List<String>>
}
