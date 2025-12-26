package com.jeandealmeida_dev.billortest.chat.domain.usecase

import com.jeandealmeida_dev.billortest.chat.data.repository.ChatRepository
import com.jeandealmeida_dev.billortest.chat.domain.model.ChatMessage
import javax.inject.Inject

/**
 * Use case for sending a chat message
 */
class SendMessageUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    
    /**
     * Send a message
     * 
     * @param message The message text
     * @param userId The sender's user ID
     * @param userName The sender's user name
     * @param channelId Optional channel ID
     * @return Single with the sent message
     */
    suspend operator fun invoke(
        message: String,
        userId: String,
        userName: String,
        channelId: String
    ): ChatMessage {
        require(message.isNotBlank()) { "Message cannot be blank" }
        require(userId.isNotBlank()) { "User ID cannot be blank" }
        require(userName.isNotBlank()) { "User name cannot be blank" }
        require(channelId.isNotBlank()) { "Channel ID cannot be blank" }

        return repository.sendMessage(message.trim(), userId, userName, channelId)
    }
}
