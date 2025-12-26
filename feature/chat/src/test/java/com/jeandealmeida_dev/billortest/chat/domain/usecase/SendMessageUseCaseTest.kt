package com.jeandealmeida_dev.billortest.chat.domain.usecase

import com.jeandealmeida_dev.billortest.chat.data.repository.ChatRepository
import com.jeandealmeida_dev.billortest.chat.domain.model.ChatMessage
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SendMessageUseCaseTest {

    private lateinit var repository: ChatRepository
    private lateinit var useCase: SendMessageUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = SendMessageUseCase(repository)
    }

    @Test
    fun `invoke should send message successfully`() = runBlocking {
        // Given
        val message = "Hello World"
        val userId = "user123"
        val userName = "John Doe"
        val channelId = "channel1"
        
        val expectedMessage = ChatMessage(
            id = "msg1",
            message = message,
            userId = userId,
            userName = userName,
            timestamp = 1234567890L,
            channelId = channelId,
            isSent = true
        )
        
        coEvery { 
            repository.sendMessage(message, userId, userName, channelId) 
        } returns expectedMessage

        // When
        val result = useCase(message, userId, userName, channelId)

        // Then
        assertEquals(expectedMessage, result)
        coVerify(exactly = 1) { 
            repository.sendMessage(message, userId, userName, channelId) 
        }
    }

    @Test
    fun `invoke should send message with channelId`() = runBlocking {
        // Given
        val message = "Hello"
        val userId = "user456"
        val userName = "Jane Smith"
        val channelId = "channel2"
        
        val expectedMessage = ChatMessage(
            id = "msg2",
            message = message,
            userId = userId,
            userName = userName,
            timestamp = 1234567890L,
            channelId = channelId,
            isSent = true
        )
        
        coEvery { 
            repository.sendMessage(message, userId, userName, channelId) 
        } returns expectedMessage

        // When
        val result = useCase(message, userId, userName, channelId)

        // Then
        assertEquals(expectedMessage, result)
        coVerify(exactly = 1) { 
            repository.sendMessage(message, userId, userName, channelId) 
        }
    }

    @Test
    fun `invoke should trim message before sending`() = runBlocking {
        // Given
        val message = "  Hello with spaces  "
        val trimmedMessage = "Hello with spaces"
        val userId = "user789"
        val userName = "Bob"
        val channelId = "channel3"
        
        val expectedMessage = ChatMessage(
            id = "msg3",
            message = trimmedMessage,
            userId = userId,
            userName = userName,
            timestamp = 1234567890L,
            channelId = channelId,
            isSent = true
        )
        
        coEvery { 
            repository.sendMessage(trimmedMessage, userId, userName, channelId) 
        } returns expectedMessage

        // When
        val result = useCase(message, userId, userName, channelId)

        // Then
        assertEquals(expectedMessage, result)
        coVerify(exactly = 1) { 
            repository.sendMessage(trimmedMessage, userId, userName, channelId) 
        }
    }

    @Test
    fun `invoke should throw exception when message is blank`() {
        // Given
        val blankMessage = "   "
        val userId = "user123"
        val userName = "John"
        val channelId = "channel4"

        // When/Then
        try {
            runBlocking {
                useCase(blankMessage, userId, userName, channelId)
            }
            throw AssertionError("Expected IllegalArgumentException was not thrown")
        } catch (e: IllegalArgumentException) {
            assertEquals("Message cannot be blank", e.message)
        }
    }

    @Test
    fun `invoke should throw exception when userId is blank`() {
        // Given
        val message = "Hello"
        val blankUserId = ""
        val userName = "John"
        val channelId = "channel5"

        // When/Then
        try {
            runBlocking {
                useCase(message, blankUserId, userName, channelId)
            }
            throw AssertionError("Expected IllegalArgumentException was not thrown")
        } catch (e: IllegalArgumentException) {
            assertEquals("User ID cannot be blank", e.message)
        }
    }

    @Test
    fun `invoke should throw exception when userName is blank`() {
        // Given
        val message = "Hello"
        val userId = "user123"
        val blankUserName = "  "
        val channelId = "channel6"

        // When/Then
        try {
            runBlocking {
                useCase(message, userId, blankUserName, channelId)
            }
            throw AssertionError("Expected IllegalArgumentException was not thrown")
        } catch (e: IllegalArgumentException) {
            assertEquals("User name cannot be blank", e.message)
        }
    }

    @Test
    fun `invoke should throw exception when channelId is blank`() {
        // Given
        val message = "Hello"
        val userId = "user123"
        val userName = "John"
        val blankChannelId = "  "

        // When/Then
        try {
            runBlocking {
                useCase(message, userId, userName, blankChannelId)
            }
            throw AssertionError("Expected IllegalArgumentException was not thrown")
        } catch (e: IllegalArgumentException) {
            assertEquals("Channel ID cannot be blank", e.message)
        }
    }

    @Test
    fun `invoke should propagate repository exception`() {
        // Given
        val message = "Hello"
        val userId = "user123"
        val userName = "John"
        val channelId = "channel7"
        
        coEvery { 
            repository.sendMessage(message, userId, userName, channelId) 
        } throws Exception("Network error")

        // When/Then
        try {
            runBlocking {
                useCase(message, userId, userName, channelId)
            }
            throw AssertionError("Expected exception was not thrown")
        } catch (e: Exception) {
            assertEquals("Network error", e.message)
        }
    }
}
