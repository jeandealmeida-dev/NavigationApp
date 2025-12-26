package com.jeandealmeida_dev.billortest.chat.domain.usecase

import com.jeandealmeida_dev.billortest.chat.data.repository.ChatRepository
import javax.inject.Inject

/**
 * Use case for sending a chat message
 */
class SendTypingStatusUseCase @Inject constructor(
    private val repository: ChatRepository
) {

    /**
     * Send a message
     *
     * @param isTyping Status of typing
     * @param userId The sender's user ID
     * @param userName The sender's user name
     * @param channelId Optional channel ID
     * @return Single with the sent message
     */
    suspend operator fun invoke(
        isTyping: Boolean,
        userId: String,
        userName: String,
        channelId: String
    ) {
        require(userId.isNotBlank()) { "User ID cannot be blank" }
        require(userName.isNotBlank()) { "User name cannot be blank" }
        require(channelId.isNotBlank()) { "Channel ID cannot be blank" }

        repository.sendTypingStatus(isTyping, userId, userName, channelId)
    }
}
