package com.jeandealmeida_dev.billortest.chat.domain.usecase

import com.jeandealmeida_dev.billortest.chat.data.repository.ChatRepository
import com.jeandealmeida_dev.billortest.chat.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for subscribing to realtime chat messages
 */
class SubscribeToTypingStatusUseCase @Inject constructor(
    private val repository: ChatRepository
) {

    /**
     * Subscribe to realtime typing status updates
     * @return Observable that emits new messages as they arrive
     */
    suspend operator fun invoke(channelId: String): Flow<List<String>> {
        return repository.subscribeToRealtimeTypingStatus(channelId)
    }
}
