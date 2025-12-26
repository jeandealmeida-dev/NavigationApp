package com.jeandealmeida_dev.billortest.chat.domain.usecase

import com.jeandealmeida_dev.billortest.chat.data.repository.ChatRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SendTypingStatusUseCaseTest {

    private lateinit var repository: ChatRepository
    private lateinit var useCase: SendTypingStatusUseCase

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        useCase = SendTypingStatusUseCase(repository)
    }

    @Test
    fun `invoke with valid parameters should call repository`() = runBlocking {
        val isTyping = true
        val userId = "user123"
        val userName = "Test User"
        val channelId = "channel456"

        coEvery { repository.sendTypingStatus(isTyping, userId, userName, channelId) } returns Unit

        useCase(isTyping, userId, userName, channelId)

        coVerify(exactly = 1) { repository.sendTypingStatus(isTyping, userId, userName, channelId) }
    }

    @Test
    fun `invoke with isTyping false should call repository`() = runBlocking {
        val isTyping = false
        val userId = "user123"
        val userName = "Test User"
        val channelId = "channel456"

        coEvery { repository.sendTypingStatus(isTyping, userId, userName, channelId) } returns Unit

        useCase(isTyping, userId, userName, channelId)

        coVerify(exactly = 1) { repository.sendTypingStatus(isTyping, userId, userName, channelId) }
    }

    @Test
    fun `invoke with blank userId should throw IllegalArgumentException`() {
        try {
            runBlocking {
                useCase(true, "", "Test User", "channel456")
            }
            throw AssertionError("Expected IllegalArgumentException was not thrown")
        } catch (e: IllegalArgumentException) {
            assertEquals("User ID cannot be blank", e.message)
        }
    }

    @Test
    fun `invoke with blank userName should throw IllegalArgumentException`() {
        try {
            runBlocking {
                useCase(true, "user123", "", "channel456")
            }
            throw AssertionError("Expected IllegalArgumentException was not thrown")
        } catch (e: IllegalArgumentException) {
            assertEquals("User name cannot be blank", e.message)
        }
    }

    @Test
    fun `invoke with blank channelId should throw IllegalArgumentException`() {
        try {
            runBlocking {
                useCase(true, "user123", "Test User", "")
            }
            throw AssertionError("Expected IllegalArgumentException was not thrown")
        } catch (e: IllegalArgumentException) {
            assertEquals("Channel ID cannot be blank", e.message)
        }
    }

    @Test
    fun `invoke with whitespace-only userId should throw IllegalArgumentException`() {
        try {
            runBlocking {
                useCase(true, "   ", "Test User", "channel456")
            }
            throw AssertionError("Expected IllegalArgumentException was not thrown")
        } catch (e: IllegalArgumentException) {
            assertEquals("User ID cannot be blank", e.message)
        }
    }

    @Test
    fun `invoke with whitespace-only userName should throw IllegalArgumentException`() {
        try {
            runBlocking {
                useCase(true, "user123", "   ", "channel456")
            }
            throw AssertionError("Expected IllegalArgumentException was not thrown")
        } catch (e: IllegalArgumentException) {
            assertEquals("User name cannot be blank", e.message)
        }
    }

    @Test
    fun `invoke with whitespace-only channelId should throw IllegalArgumentException`() {
        try {
            runBlocking {
                useCase(true, "user123", "Test User", "   ")
            }
            throw AssertionError("Expected IllegalArgumentException was not thrown")
        } catch (e: IllegalArgumentException) {
            assertEquals("Channel ID cannot be blank", e.message)
        }
    }

    @Test
    fun `invoke should propagate repository exceptions`() {
        val userId = "user123"
        val userName = "Test User"
        val channelId = "channel456"
        val expectedException = RuntimeException("Network error")

        coEvery { repository.sendTypingStatus(any(), any(), any(), any()) } throws expectedException

        try {
            runBlocking {
                useCase(true, userId, userName, channelId)
            }
            throw AssertionError("Expected RuntimeException was not thrown")
        } catch (e: RuntimeException) {
            assertEquals("Network error", e.message)
        }
    }
}
