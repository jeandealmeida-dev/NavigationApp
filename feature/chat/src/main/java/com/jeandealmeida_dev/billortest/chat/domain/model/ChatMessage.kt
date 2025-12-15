package com.jeandealmeida_dev.billortest.chat.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

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
    val isSent: Boolean = true
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
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        return format.format(date)
    }
}
