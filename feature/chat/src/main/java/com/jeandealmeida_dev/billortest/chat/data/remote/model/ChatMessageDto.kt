package com.jeandealmeida_dev.billortest.chat.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data transfer object for chat messages from Firebase/Firestore
 */
@JsonClass(generateAdapter = true)
data class ChatMessageDto(
    @Json(name = "id")
    val id: String,
    
    @Json(name = "message")
    val message: String,
    
    @Json(name = "user_id")
    val userId: String,
    
    @Json(name = "user_name")
    val userName: String,
    
    @Json(name = "created_at")
    val createdAt: String,
    
    @Json(name = "channel_id")
    val channelId: String? = null,
    
    @Json(name = "message_type")
    val messageType: String = "TEXT",
    
    @Json(name = "audio_url")
    val audioUrl: String? = null,
    
    @Json(name = "audio_duration")
    val audioDuration: Int? = null
)
