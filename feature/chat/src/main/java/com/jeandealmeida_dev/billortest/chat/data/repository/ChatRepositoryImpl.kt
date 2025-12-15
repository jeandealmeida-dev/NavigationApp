package com.jeandealmeida_dev.billortest.chat.data.repository

import com.jeandealmeida_dev.billortest.chat.data.local.ChatLocalDataSource
import com.jeandealmeida_dev.billortest.chat.data.mapper.ChatMessageMapper.toDomain
import com.jeandealmeida_dev.billortest.chat.data.mapper.ChatMessageMapper.toEntity
import com.jeandealmeida_dev.billortest.chat.data.remote.ChatRemoteDataSource
import com.jeandealmeida_dev.billortest.chat.domain.model.ChatMessage
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

/**
 * Implementation of ChatRepository
 * Coordinates between local and remote data sources
 */
class ChatRepositoryImpl @Inject constructor(
    private val localDataSource: ChatLocalDataSource,
    private val remoteDataSource: ChatRemoteDataSource
) : ChatRepository {
    
    override fun getMessages(): Flowable<List<ChatMessage>> {
        return localDataSource.getAllMessages()
            .map { entities -> entities.map { it.toDomain() } }
            .subscribeOn(Schedulers.io())
    }
    
    override fun getMessagesByChannel(channelId: String): Flowable<List<ChatMessage>> {
        return localDataSource.getMessagesByChannel(channelId)
            .map { entities -> entities.map { it.toDomain() } }
            .subscribeOn(Schedulers.io())
    }
    
    override fun sendMessage(
        message: String,
        userId: String,
        userName: String,
        channelId: String?
    ): Single<ChatMessage> {
        return remoteDataSource.sendMessage(message, userId, userName, channelId)
            .map { dto -> dto.toDomain() }
            .flatMap { domainMessage ->
                // Save to local cache
                localDataSource.insertMessage(domainMessage.toEntity())
                    .map { domainMessage }
            }
            .subscribeOn(Schedulers.io())
    }
    
    override fun syncMessages(): Single<List<ChatMessage>> {
        return remoteDataSource.getMessages()
            .map { dtos -> dtos.map { it.toDomain() } }
            .flatMap { messages ->
                // Save to local cache
                val entities = messages.map { it.toEntity() }
                localDataSource.insertMessages(entities)
                    .map { messages }
            }
            .subscribeOn(Schedulers.io())
    }
    
    override fun subscribeToRealtimeMessages(): Observable<ChatMessage> {
        return remoteDataSource.subscribeToMessages()
            .map { dto -> dto.toDomain() }
            .doOnNext { message ->
                // Save incoming message to local cache
                localDataSource.insertMessage(message.toEntity())
                    .subscribeOn(Schedulers.io())
                    .subscribe()
            }
            .subscribeOn(Schedulers.io())
    }
    
    override fun unsubscribeFromRealtime() {
        remoteDataSource.unsubscribe()
    }
    
    override fun clearMessages(): Single<Int> {
        return localDataSource.deleteAllMessages()
            .subscribeOn(Schedulers.io())
    }
}
