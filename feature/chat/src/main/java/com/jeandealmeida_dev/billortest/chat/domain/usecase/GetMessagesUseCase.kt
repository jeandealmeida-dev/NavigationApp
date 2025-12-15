package com.jeandealmeida_dev.billortest.chat.domain.usecase

import com.jeandealmeida_dev.billortest.chat.data.repository.ChatRepository
import com.jeandealmeida_dev.billortest.chat.domain.model.ChatMessage
import io.reactivex.rxjava3.core.Flowable
import javax.inject.Inject

/**
 * Use case for retrieving chat messages
 */
class GetMessagesUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    
    /**
     * Get all messages
     */
    operator fun invoke(): Flowable<List<ChatMessage>> {
        return repository.getMessages()
    }
    
    /**
     * Get messages for a specific channel
     */
    operator fun invoke(channelId: String): Flowable<List<ChatMessage>> {
        return repository.getMessagesByChannel(channelId)
    }
}
