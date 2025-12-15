package com.jeandealmeida_dev.billortest.chat.domain.usecase

import com.jeandealmeida_dev.billortest.chat.data.repository.ChatRepository
import com.jeandealmeida_dev.billortest.chat.domain.model.ChatMessage
import io.reactivex.rxjava3.core.Observable
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
    operator fun invoke(): Observable<ChatMessage> {
        return repository.subscribeToRealtimeMessages()
    }
    
    /**
     * Unsubscribe from realtime updates
     */
    fun unsubscribe() {
        repository.unsubscribeFromRealtime()
    }
}
