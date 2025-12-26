package com.jeandealmeida_dev.billortest.chat.domain.usecase

import com.jeandealmeida_dev.billortest.chat.data.repository.ChatRepository
import javax.inject.Inject

/**
 * Use case for subscribing to realtime chat messages
 */
class SubscribeToMessagesUseCase @Inject constructor(
    private val repository: ChatRepository
) {

    /**
     * Subscribe to realtime message updates
     * @return Observable that emits new messages as they arrive
     */
    suspend operator fun invoke(channelId: String) {
        repository.subscribeToRealtimeMessages(channelId)
    }
}
