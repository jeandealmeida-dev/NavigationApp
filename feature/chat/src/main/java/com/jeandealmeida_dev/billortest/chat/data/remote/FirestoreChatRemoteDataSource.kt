package com.jeandealmeida_dev.billortest.chat.data.remote

import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.jeandealmeida_dev.billortest.chat.data.remote.model.ChatMessageDto
import com.jeandealmeida_dev.billortest.chat.domain.ChatException
import com.jeandealmeida_dev.billortest.chat.domain.model.MessageType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class FirestoreChatRemoteDataSource @Inject constructor() : ChatRemoteDataSource {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val channelsCollection = firestore.collection(CHANNELS_COLLECTION)
    private val messagesCollection = firestore.collection(MESSAGES_COLLECTION)

    companion object {
        private const val MESSAGES_COLLECTION = "chat_messages"
        private const val CHANNELS_COLLECTION = "channels"
        private const val FIELD_ID = "id"
        private const val FIELD_MESSAGE = "message"
        private const val FIELD_USER_ID = "user_id"
        private const val FIELD_USER_NAME = "user_name"
        private const val FIELD_CREATED_AT = "created_at"
        private const val FIELD_CHANNEL_ID = "channel_id"
        private const val FIELD_MESSAGE_TYPE = "message_type"
        private const val FIELD_AUDIO_URL = "audio_url"
        private const val FIELD_AUDIO_DURATION = "audio_duration"
        private const val FIELD_IS_TYPING = "is_typing"
    }

    override suspend fun getMessages(): List<ChatMessageDto> {
        return try {
            val snapshot = messagesCollection
                .orderBy(FIELD_CREATED_AT, Query.Direction.ASCENDING)
                .get()
                .await()


            snapshot.documents.mapNotNull { document ->
                ChatMessageDto(
                    id = document.getString(FIELD_ID) ?: "",
                    message = document.getString(FIELD_MESSAGE) ?: "",
                    userId = document.getString(FIELD_USER_ID) ?: "",
                    userName = document.getString(FIELD_USER_NAME) ?: "",
                    createdAt = document.getString(FIELD_CREATED_AT) ?: "",
                    channelId = document.getString(FIELD_CHANNEL_ID),
                    messageType = document.getString(FIELD_MESSAGE_TYPE) ?: MessageType.TEXT.name,
                    audioUrl = document.getString(FIELD_AUDIO_URL),
                    audioDuration = document.getLong(FIELD_AUDIO_DURATION)?.toInt()
                )

            }
        } catch (e: Exception) {
            throw ChatException.GetMessageException(e.message ?: "Erro ao obter mensagens")
        }
    }

    override suspend fun sendMessage(
        message: String,
        userId: String,
        userName: String,
        channelId: String?
    ): ChatMessageDto {
        val messageId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis().toString()

        val messageData = hashMapOf(
            FIELD_ID to messageId,
            FIELD_MESSAGE to message,
            FIELD_USER_ID to userId,
            FIELD_USER_NAME to userName,
            FIELD_CREATED_AT to timestamp,
            FIELD_CHANNEL_ID to channelId,
            FIELD_MESSAGE_TYPE to MessageType.TEXT.name
        )

        return try {
            messagesCollection
                .document(messageId)
                .set(messageData)
                .await()

            ChatMessageDto(
                id = messageId,
                message = message,
                userId = userId,
                userName = userName,
                createdAt = timestamp,
                channelId = channelId,
                messageType = MessageType.TEXT.name
            )

        } catch (exception: Exception) {
            throw ChatException.MessageSentException(exception.message ?: "Erro ao enviar")
        }
    }

    override suspend fun sendAudioMessage(
        audioUrl: String,
        audioDuration: Int,
        userId: String,
        userName: String,
        channelId: String?
    ): ChatMessageDto {
        val messageId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis().toString()

        val messageData = hashMapOf(
            FIELD_ID to messageId,
            FIELD_MESSAGE to "Audio message",
            FIELD_USER_ID to userId,
            FIELD_USER_NAME to userName,
            FIELD_CREATED_AT to timestamp,
            FIELD_CHANNEL_ID to channelId,
            FIELD_MESSAGE_TYPE to MessageType.AUDIO.name,
            FIELD_AUDIO_URL to audioUrl,
            FIELD_AUDIO_DURATION to audioDuration
        )

        return try {
            messagesCollection
                .document(messageId)
                .set(messageData)
                .await()

            ChatMessageDto(
                id = messageId,
                message = "Audio message",
                userId = userId,
                userName = userName,
                createdAt = timestamp,
                channelId = channelId,
                messageType = MessageType.AUDIO.name,
                audioUrl = audioUrl,
                audioDuration = audioDuration
            )
        } catch (exception: Exception) {
            throw ChatException.MessageSentException(exception.message ?: "Erro ao enviar")
        }
    }

    override suspend fun sendTypingStatus(
        isTyping: Boolean,
        userId: String,
        userName: String,
        channelId: String
    ) {
        val messageId = UUID.randomUUID().toString()
        val typingDocId = "${channelId}_${userId}"

        val messageData = hashMapOf(
            FIELD_ID to messageId,
            FIELD_CHANNEL_ID to channelId,
            FIELD_IS_TYPING to isTyping,
            FIELD_USER_ID to userId,
            FIELD_USER_NAME to userName,
        )

        try {
            channelsCollection
                .document(typingDocId)
                .set(messageData)
                .await()
        } catch (exception: Exception) {
            throw ChatException.TypingStatusException(exception.message ?: "Erro ao enviar")
        }
    }

    override suspend fun subscribeToMessages(channelId: String): Flow<ChatMessageDto> =
        callbackFlow {
            // Start listening to Firestore real-time updates
            val registration = messagesCollection
                .whereEqualTo(FIELD_CHANNEL_ID, channelId)
                .orderBy(FIELD_CREATED_AT, Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    snapshot?.documentChanges?.forEach { change ->
                        when (change.type) {
                            DocumentChange.Type.ADDED -> {
                                try {
                                    val document = change.document
                                    val messageDto = ChatMessageDto(
                                        id = document.getString(FIELD_ID) ?: "",
                                        message = document.getString(FIELD_MESSAGE) ?: "",
                                        userId = document.getString(FIELD_USER_ID) ?: "",
                                        userName = document.getString(FIELD_USER_NAME) ?: "",
                                        createdAt = document.getString(FIELD_CREATED_AT) ?: "",
                                        channelId = document.getString(FIELD_CHANNEL_ID),
                                        messageType = document.getString(FIELD_MESSAGE_TYPE)
                                            ?: MessageType.TEXT.name,
                                        audioUrl = document.getString(FIELD_AUDIO_URL),
                                        audioDuration = document.getLong(FIELD_AUDIO_DURATION)
                                            ?.toInt()
                                    )
                                    trySend(messageDto)
                                } catch (e: Exception) {
                                    close(e)
                                }
                            }

                            else -> {}
                        }
                    }
                }

            awaitClose { registration.remove() }
        }

    override suspend fun subscribeToTypingStatus(channelId: String): Flow<List<String>> =
        callbackFlow {
            val registration = channelsCollection
                .whereEqualTo(FIELD_CHANNEL_ID, channelId)
                .whereEqualTo(FIELD_IS_TYPING, true)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    val typingUsers = snapshot?.documents?.mapNotNull {
                        it.getString(FIELD_USER_NAME)
                    } ?: emptyList()

                    trySend(typingUsers)
                }

            awaitClose { registration.remove() }
        }
}
