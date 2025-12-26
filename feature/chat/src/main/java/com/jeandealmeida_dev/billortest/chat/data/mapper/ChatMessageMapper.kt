package com.jeandealmeida_dev.billortest.chat.data.mapper

import com.jeandealmeida_dev.billortest.chat.data.local.entity.ChatMessageEntity
import com.jeandealmeida_dev.billortest.chat.data.remote.model.ChatMessageDto
import com.jeandealmeida_dev.billortest.chat.domain.model.ChatMessage
import com.jeandealmeida_dev.billortest.chat.domain.model.MessageType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Mapper for converting between different chat message representations
 */
object ChatMessageMapper {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    /**
     * Convert DTO to Domain model
     */
    fun ChatMessageDto.toDomain(): ChatMessage {
        return ChatMessage(
            id = id,
            message = message,
            messageType = MessageType.entries.find { it.name == messageType } ?: MessageType.TEXT,
            userId = userId,
            userName = userName,
            timestamp = parseTimestamp(createdAt),
            channelId = channelId,
            audioUrl = audioUrl,
            audioDuration = audioDuration,
            isSent = true
        )
    }

    /**
     * Convert Entity to Domain model
     */
    fun ChatMessageEntity.toDomain(): ChatMessage {
        return ChatMessage(
            id = id,
            message = message,
            messageType =MessageType.entries.find { it.name == messageType } ?: MessageType.TEXT,
            userId = userId,
            userName = userName,
            timestamp = createdAt,
            channelId = channelId,
            audioUrl = audioUrl,
            audioDuration = audioDuration,
            isSent = isSent
        )
    }

    /**
     * Convert Domain model to Entity
     */
    fun ChatMessage.toEntity(): ChatMessageEntity {
        return ChatMessageEntity(
            id = id,
            message = message,
            messageType = messageType.name,
            userId = userId,
            userName = userName,
            createdAt = timestamp,
            channelId = channelId,
            audioUrl = audioUrl,
            audioDuration = audioDuration,
            isSent = isSent
        )
    }

    /**
     * Convert DTO to Entity
     */
    fun ChatMessageDto.toEntity(): ChatMessageEntity {
        return ChatMessageEntity(
            id = id,
            message = message,
            messageType = messageType,
            userId = userId,
            userName = userName,
            createdAt = parseTimestamp(createdAt),
            channelId = channelId,
            audioUrl = audioUrl,
            audioDuration = audioDuration,
            isSent = true
        )
    }

    /**
     * Parse timestamp string to Long
     */
    private fun parseTimestamp(timestamp: String): Long {
        return try {
            // Try parsing as ISO format first
            dateFormat.parse(timestamp)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            // If parsing fails, try as Long
            timestamp.toLongOrNull() ?: System.currentTimeMillis()
        }
    }

    /**
     * Format timestamp to readable string
     */
    fun formatTimestamp(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }
}
