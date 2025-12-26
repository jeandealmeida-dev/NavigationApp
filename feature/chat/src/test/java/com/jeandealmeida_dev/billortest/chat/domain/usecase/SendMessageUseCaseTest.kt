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
    fun `invoke should send message without channelId`() = runBlocking {
        // Given
        val message = "Hello"
        val userId = "user456"
        val userName = "Jane Smith"
        
        val expectedMessage = ChatMessage(
            id = "msg2",
            message = message,
            userId = userId,
            userName = userName,
            timestamp = 1234567890L,
            channelId = null,
            isSent = true
        )
        
        coEvery { 
            repository.sendMessage(message, userId, userName, null) 
        } returns expectedMessage

        // When
        val result = useCase(message, userId, userName)

        // Then
        assertEquals(expectedMessage, result)
        coVerify(exactly = 1) { 
            repository.sendMessage(message, userId, userName, null) 
        }
    }

    @Test
    fun `invoke should trim message before sending`() = runBlocking {
        // Given
        val message = "  Hello with spaces  "
        val trimmedMessage = "Hello with spaces"
        val userId = "user789"
        val userName = "Bob"
        
        val expectedMessage = ChatMessage(
            id = "msg3",
            message = trimmedMessage,
            userId = userId,
            userName = userName,
            timestamp = 1234567890L,
            channelId = null,
            isSent = true
        )
        
        coEvery { 
            repository.sendMessage(trimmedMessage, userId, userName, null) 
        } returns expectedMessage

        // When
        val result = useCase(message, userId, userName)

        // Then
        assertEquals(expectedMessage, result)
        coVerify(exactly = 1) { 
            repository.sendMessage(trimmedMessage, userId, userName, null) 
        }
    }

    @Test
    fun `invoke should throw exception when message is blank`() {
        // Given
        val blankMessage = "   "
        val userId = "user123"
        val userName = "John"

        // When/Then
        try {
            runBlocking {
                useCase(blankMessage, userId, userName)
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

        // When/Then
        try {
            runBlocking {
                useCase(message, blankUserId, userName)
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

        // When/Then
        try {
            runBlocking {
                useCase(message, userId, blankUserName)
            }
            throw AssertionError("Expected IllegalArgumentException was not thrown")
        } catch (e: IllegalArgumentException) {
            assertEquals("User name cannot be blank", e.message)
        }
    }

    @Test
    fun `invoke should propagate repository exception`() {
        // Given
        val message = "Hello"
        val userId = "user123"
        val userName = "John"
        
        coEvery { 
            repository.sendMessage(message, userId, userName, null) 
        } throws Exception("Network error")

        // When/Then
        try {
            runBlocking {
                useCase(message, userId, userName)
            }
            throw AssertionError("Expected exception was not thrown")
        } catch (e: Exception) {
            assertEquals("Network error", e.message)
        }
    }
}
