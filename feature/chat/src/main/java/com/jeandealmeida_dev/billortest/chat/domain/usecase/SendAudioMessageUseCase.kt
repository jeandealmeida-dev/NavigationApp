package com.jeandealmeida_dev.billortest.chat.domain.usecase

import com.jeandealmeida_dev.billortest.chat.data.repository.ChatRepository
import com.jeandealmeida_dev.billortest.chat.domain.model.ChatMessage
import java.io.File
import javax.inject.Inject

/**
 * Use case for sending audio messages
 * Coordinates audio upload to Firebase Storage and message creation in Firestore
 */
class SendAudioMessageUseCase @Inject constructor(
    private val uploadAudioUseCase: UploadAudioUseCase,
    private val repository: ChatRepository
) {

    /**
     * Upload audio file and send audio message
     * @param audioFile The audio file to upload
     * @param audioDuration Duration of the audio in seconds
     * @param userId Current user ID
     * @param userName Current user name
     * @param channelId Optional channel ID
     * @return Single that emits the created ChatMessage
     */
    operator suspend fun invoke(
        audioFile: File,
        audioDuration: Int,
        userId: String,
        userName: String,
        channelId: String? = null
    ): ChatMessage {
        return uploadAudioUseCase(audioFile).let { audioUrl ->
            repository.sendAudioMessage(
                audioUrl = audioUrl,
                audioDuration = audioDuration,
                userId = userId,
                userName = userName,
                channelId = channelId
            )
        }
    }
}
