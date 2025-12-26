package com.jeandealmeida_dev.billortest.chat.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeandealmeida_dev.billortest.chat.domain.ChatException
import com.jeandealmeida_dev.billortest.chat.domain.model.ChatMessage
import com.jeandealmeida_dev.billortest.chat.domain.usecase.GetMessagesUseCase
import com.jeandealmeida_dev.billortest.chat.domain.usecase.SendAudioMessageUseCase
import com.jeandealmeida_dev.billortest.chat.domain.usecase.SendMessageUseCase
import com.jeandealmeida_dev.billortest.chat.domain.usecase.SendTypingStatusUseCase
import com.jeandealmeida_dev.billortest.chat.domain.usecase.SubscribeToMessagesUseCase
import com.jeandealmeida_dev.billortest.chat.domain.usecase.SubscribeToTypingStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed class ChatViewState {
    object Idle : ChatViewState()
    object Loading : ChatViewState()
    object MessageSent : ChatViewState()
    data class Error(val exception: ChatException) : ChatViewState()
    data class Messages(val messages: List<ChatMessage>) : ChatViewState()
    data class NewMessage(val message: ChatMessage) : ChatViewState()
    data class Typing(val usersTyping: List<String>) : ChatViewState()
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val sendAudioMessageUseCase: SendAudioMessageUseCase,
    private val subscribeToMessagesUseCase: SubscribeToMessagesUseCase,
    private val subscribeToTypingStatusUseCase: SubscribeToTypingStatusUseCase,
    private val sendTypingStatusUseCase: SendTypingStatusUseCase
) : ViewModel() {

    private var typingJob: Job? = null

    private val _state = MutableStateFlow<ChatViewState>(ChatViewState.Idle)
    val state: StateFlow<ChatViewState> = _state.asStateFlow()

    private var currentUserId: String = ""
    private var currentUserName: String = ""
    private val currentChannelId: String = "channelId"

    fun start() {
        subscribeToRealtimeMessages()
        subscribeToTypingStatus()
    }

    /**
     * Set current user information
     */
    fun setCurrentUser(userId: String, userName: String = "user_${userId.substring(0, 4)}") {
        this.currentUserId = userId
        this.currentUserName = userName
    }

    /**
     * Get current user ID
     */
    fun getCurrentUserId(): String = currentUserId


    //region Realtime Connection
    /**
     * Subscribe to realtime message updates
     */
    private fun subscribeToRealtimeMessages() {
        viewModelScope.launch {
            getMessagesUseCase.invoke(currentChannelId)
                .catch { exception ->
                    _state.value = ChatViewState.Error(
                        ChatException.GetMessageException(
                            exception.message ?: "Failed to load messages"
                        )
                    )
                }
                .collect { messages ->
                    _state.value = ChatViewState.Messages(messages)
                }

            runCatching {
                subscribeToMessagesUseCase.invoke(channelId = currentChannelId)
            }.onFailure { exception ->
                _state.value = ChatViewState.Error(
                    ChatException.RealtimeConnectionException(
                        exception.message ?: "Failed to load messages"
                    )
                )
            }
        }
    }

    /**
     * Subscribe to realtime message updates
     */
    private fun subscribeToTypingStatus() {
        viewModelScope.launch {
            subscribeToTypingStatusUseCase.invoke(channelId = currentChannelId)
                .catch { exception ->
                    _state.value = ChatViewState.Error(
                        ChatException.MessageSentException(
                            exception.message ?: "Failed to load messages"
                        )
                    )
                }
                .collect { users ->
                    _state.value = ChatViewState.Typing(users.filterNot { it == currentUserName })
                }


        }
    }

    //endregion

    //region Actions
    /**
     * Send a new message
     */
    fun sendMessage(messageText: String) {
        viewModelScope.launch {
            if (messageText.isBlank()) {
                _state.value = ChatViewState.Error(
                    ChatException.EmptyMessageException()
                )
                return@launch
            }

            try {
                sendMessageUseCase(
                    message = messageText,
                    userId = currentUserId,
                    userName = currentUserName,
                    channelId = currentChannelId
                )

                _state.value = ChatViewState.MessageSent
            } catch (exception: Exception) {
                _state.value = ChatViewState.Error(
                    ChatException.MessageSentException(
                        exception.message ?: "Failed to send message"
                    )
                )
            }
        }
    }

    /**
     * Send a new audio message
     */
    fun sendAudioMessage(audioFile: File?, duration: Int) {
        viewModelScope.launch {
            if (audioFile == null || duration <= 0) {
                _state.value = ChatViewState.Error(
                    ChatException.EmptyMessageException()
                )
                return@launch
            }

            // delay to SO release audio file
            delay(500)

            try {
                sendAudioMessageUseCase(
                    audioFile = audioFile,
                    audioDuration = duration,
                    userId = currentUserId,
                    userName = currentUserName,
                    channelId = currentChannelId
                )

                _state.value = ChatViewState.MessageSent
            } catch (exception: Exception) {
                _state.value = ChatViewState.Error(
                    ChatException.MessageSentException(
                        exception.message ?: "Failed to send message"
                    )
                )
            }
        }
    }

    /**
     * Send typing status
     */

    fun sendTypingStatus() {
        viewModelScope.launch {
            sendTypingStatusUseCase(
                isTyping = true,
                userId = currentUserId,
                userName = currentUserName,
                channelId = currentChannelId
            )

            typingJob?.cancel()

            typingJob = viewModelScope.launch {
                delay(2000)
                sendTypingStatusUseCase(
                    isTyping = false,
                    userId = currentUserId,
                    userName = currentUserName,
                    channelId = currentChannelId
                )
            }
        }
    }

    //endregion
}
