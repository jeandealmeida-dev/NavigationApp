package com.jeandealmeida_dev.billortest.chat.domain

sealed class ChatException(message: String) : Exception(message) {
    class MessageSentException(message: String) : ChatException(message)
    class EmptyMessageException() : ChatException("Message cannot be empty")
    class GetMessageException(message: String) : ChatException(message)
    class AudioSentException(message: String) : ChatException(message)
    class ClearMessageException(message: String) : ChatException(message)

    data class RealtimeConnectionException(val realtimeErrorMessage: String) :
        ChatException(realtimeErrorMessage) {

    }

    class TypingStatusException(message: String) : ChatException(message)
}