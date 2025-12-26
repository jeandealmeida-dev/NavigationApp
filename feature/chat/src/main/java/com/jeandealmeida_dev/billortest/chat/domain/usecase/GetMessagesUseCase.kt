package com.jeandealmeida_dev.billortest.chat.domain.usecase

import com.jeandealmeida_dev.billortest.chat.data.repository.ChatRepository
import com.jeandealmeida_dev.billortest.chat.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving chat messages
 */
class GetMessagesUseCase @Inject constructor(
    private val repository: ChatRepository
) {

    /**
     * Get messages for a specific channel
     */
    suspend operator fun invoke(channelId: String): Flow<List<ChatMessage>> {
        return repository.getMessagesByChannel(channelId)
    }
}
