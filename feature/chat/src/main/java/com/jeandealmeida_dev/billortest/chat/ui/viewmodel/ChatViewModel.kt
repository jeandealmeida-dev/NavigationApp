package com.jeandealmeida_dev.billortest.chat.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jeandealmeida_dev.billortest.chat.domain.model.ChatMessage
import com.jeandealmeida_dev.billortest.chat.domain.usecase.GetMessagesUseCase
import com.jeandealmeida_dev.billortest.chat.domain.usecase.SendMessageUseCase
import com.jeandealmeida_dev.billortest.chat.domain.usecase.SubscribeToMessagesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import javax.inject.Inject

/**
 * ViewModel for Chat screen
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val subscribeToMessagesUseCase: SubscribeToMessagesUseCase
) : ViewModel() {
    
    private val disposables = CompositeDisposable()
    
    // LiveData for messages list
    private val _messages = MutableLiveData<List<ChatMessage>>()
    val messages: LiveData<List<ChatMessage>> = _messages
    
    // LiveData for loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    // LiveData for errors
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    
    // LiveData for message sent confirmation
    private val _messageSent = MutableLiveData<Boolean>()
    val messageSent: LiveData<Boolean> = _messageSent
    
    // Current user info (should be injected or retrieved from auth service)
    private var currentUserId: String = "user_${System.currentTimeMillis()}"
    private var currentUserName: String = "User"
    
    init {
        loadMessages()
        subscribeToRealtimeMessages()
    }
    
    /**
     * Load messages from repository
     */
    fun loadMessages() {
        getMessagesUseCase()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { messagesList ->
                    _messages.value = messagesList
                    _isLoading.value = false
                },
                { throwable ->
                    _error.value = throwable.message ?: "Failed to load messages"
                    _isLoading.value = false
                }
            )
            .addTo(disposables)
    }
    
    /**
     * Subscribe to realtime message updates
     */
    private fun subscribeToRealtimeMessages() {
        subscribeToMessagesUseCase()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { message ->
                    // New message received, the local cache will be updated
                    // and getMessagesUseCase will emit the updated list
                },
                { throwable ->
                    _error.value = "Realtime connection error: ${throwable.message}"
                }
            )
            .addTo(disposables)
    }
    
    /**
     * Send a new message
     */
    fun sendMessage(messageText: String) {
        if (messageText.isBlank()) {
            _error.value = "Message cannot be empty"
            return
        }
        
        _isLoading.value = true
        
        sendMessageUseCase(
            message = messageText,
            userId = currentUserId,
            userName = currentUserName
        )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { message ->
                    _messageSent.value = true
                    _isLoading.value = false
                },
                { throwable ->
                    _error.value = throwable.message ?: "Failed to send message"
                    _isLoading.value = false
                    _messageSent.value = false
                }
            )
            .addTo(disposables)
    }
    
    /**
     * Set current user information
     */
    fun setCurrentUser(userId: String, userName: String) {
        this.currentUserId = userId
        this.currentUserName = userName
    }
    
    /**
     * Get current user ID
     */
    fun getCurrentUserId(): String = currentUserId
    
    override fun onCleared() {
        super.onCleared()
        subscribeToMessagesUseCase.unsubscribe()
        disposables.clear()
    }
}
