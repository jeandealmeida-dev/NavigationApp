package com.jeandealmeida_dev.billortest.chat.domain.model

import android.os.Parcelable
import com.jeandealmeida_dev.billortest.commons.ui.extensions.formatInHoursMinutes
import kotlinx.parcelize.Parcelize

/**
 * Message type enumeration
 */
enum class MessageType {
    TEXT,
    AUDIO
}

/**
 * Domain model representing a chat message
 */
@Parcelize
data class ChatMessage(
    val id: String,
    val message: String,
    val userId: String,
    val userName: String,
    val timestamp: Long,
    val channelId: String? = null,
    val isSent: Boolean = true,
    val messageType: MessageType = MessageType.TEXT,
    val audioUrl: String? = null,
    val audioDuration: Int? = null // Duration in seconds
) : Parcelable {

    /**
     * Check if this message was sent by the current user
     */
    fun isMine(currentUserId: String): Boolean {
        return userId == currentUserId
    }

    /**
     * Get formatted time for display
     */
    fun getFormattedTime(): String {
        return timestamp.formatInHoursMinutes()
    }
}
