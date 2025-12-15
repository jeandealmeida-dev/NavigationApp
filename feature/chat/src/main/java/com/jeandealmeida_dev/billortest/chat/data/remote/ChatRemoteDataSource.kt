package com.jeandealmeida_dev.billortest.chat.data.remote

import com.jeandealmeida_dev.billortest.chat.data.remote.model.ChatMessageDto
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

/**
 * Interface for remote chat data operations via Supabase
 */
interface ChatRemoteDataSource {
    
    /**
     * Fetch all messages from the server
     */
    fun getMessages(): Single<List<ChatMessageDto>>
    
    /**
     * Send a new message to the server
     */
    fun sendMessage(
        message: String,
        userId: String,
        userName: String,
        channelId: String? = null
    ): Single<ChatMessageDto>
    
    /**
     * Subscribe to realtime message updates
     * Returns an Observable that emits new messages as they arrive
     */
    fun subscribeToMessages(): Observable<ChatMessageDto>
    
    /**
     * Unsubscribe from realtime updates
     */
    fun unsubscribe()
}
