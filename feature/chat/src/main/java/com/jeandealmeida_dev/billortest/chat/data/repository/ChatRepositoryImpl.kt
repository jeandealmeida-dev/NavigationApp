package com.jeandealmeida_dev.billortest.chat.data.repository

import com.jeandealmeida_dev.billortest.chat.data.local.ChatLocalDataSource
import com.jeandealmeida_dev.billortest.chat.data.mapper.ChatMessageMapper.toDomain
import com.jeandealmeida_dev.billortest.chat.data.mapper.ChatMessageMapper.toEntity
import com.jeandealmeida_dev.billortest.chat.data.remote.ChatRemoteDataSource
import com.jeandealmeida_dev.billortest.chat.domain.ChatException
import com.jeandealmeida_dev.billortest.chat.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of ChatRepository
 * Coordinates between local and remote data sources
 */
class ChatRepositoryImpl @Inject constructor(
    private val localDataSource: ChatLocalDataSource,
    private val remoteDataSource: ChatRemoteDataSource
) : ChatRepository {

    //region Send Events
    override suspend fun sendMessage(
        message: String,
        userId: String,
        userName: String,
        channelId: String?
    ): ChatMessage {
        try {
            return remoteDataSource.sendMessage(
                message = message,
                userId = userId,
                userName = userName,
                channelId = channelId
            ).toDomain()
        } catch (exception: Exception) {
            throw ChatException.MessageSentException(exception.message ?: "Erro ao obter mensagens")
        }
    }

    override suspend fun sendAudioMessage(
        audioUrl: String,
        audioDuration: Int,
        userId: String,
        userName: String,
        channelId: String?
    ): ChatMessage {
        try {

            return remoteDataSource.sendAudioMessage(
                audioUrl,
                audioDuration,
                userId,
                userName,
                channelId
            ).toDomain()
        } catch (exception: Exception) {
            throw ChatException.MessageSentException(exception.message ?: "Erro ao obter mensagens")
        }
    }

    override suspend fun sendTypingStatus(
        isTyping: Boolean,
        userId: String,
        userName: String,
        channelId: String
    ) {
        try {
            remoteDataSource.sendTypingStatus(isTyping, userId, userName, channelId)
        } catch (exception: Exception) {
            throw ChatException.TypingStatusException(
                exception.message ?: "Erro ao obter mensagens"
            )
        }
    }

    //endregion

    //region Realtime Connection

    override suspend fun getMessagesByChannel(channelId: String): Flow<List<ChatMessage>> {
        return try {
            localDataSource.getMessagesByChannel(channelId)
                .map { entity -> entity.map { it.toDomain() } }
        } catch (exception: Exception) {
            throw ChatException.GetMessageException(exception.message ?: "Erro ao obter mensagens")
        }
    }

    override suspend fun subscribeToRealtimeMessages(channelId: String) {
        try {
            remoteDataSource.subscribeToMessages(channelId)
                .collect { dto ->
                    localDataSource.insertMessage(dto.toEntity())
                }
        } catch (exception: Exception) {
            throw ChatException.RealtimeConnectionException(
                exception.message ?: "Erro ao obter mensagens"
            )
        }
    }

    override suspend fun subscribeToRealtimeTypingStatus(channelId: String): Flow<List<String>> {
        try {
            return remoteDataSource.subscribeToTypingStatus(channelId)
        } catch (exception: Exception) {
            throw ChatException.TypingStatusException(
                exception.message ?: "Erro ao obter mensagens"
            )
        }
    }

    //endregion

    override suspend fun clearMessages(): Int {
        try {
            localDataSource.deleteAllMessages()
        } catch (exception: Exception) {
            throw ChatException.ClearMessageException(
                exception.message ?: "Erro ao obter mensagens"
            )
        }
        return localDataSource.deleteAllMessages()
    }
}
