package com.jeandealmeida_dev.billortest.chat.data.repository

import com.jeandealmeida_dev.billortest.chat.domain.model.ChatMessage
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

/**
 * Repository interface for chat operations
 */
interface ChatRepository {
    
    /**
     * Get all messages from local cache
     */
    fun getMessages(): Flowable<List<ChatMessage>>
    
    /**
     * Get messages for a specific channel
     */
    fun getMessagesByChannel(channelId: String): Flowable<List<ChatMessage>>
    
    /**
     * Send a new message
     */
    fun sendMessage(
        message: String,
        userId: String,
        userName: String,
        channelId: String? = null
    ): Single<ChatMessage>
    
    /**
     * Sync messages from remote server
     */
    fun syncMessages(): Single<List<ChatMessage>>
    
    /**
     * Subscribe to realtime message updates
     */
    fun subscribeToRealtimeMessages(): Observable<ChatMessage>
    
    /**
     * Unsubscribe from realtime updates
     */
    fun unsubscribeFromRealtime()
    
    /**
     * Clear all local messages
     */
    fun clearMessages(): Single<Int>
}
