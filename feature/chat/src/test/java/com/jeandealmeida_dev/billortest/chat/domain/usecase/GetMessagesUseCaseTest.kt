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

class GetMessagesUseCaseTest {

    private lateinit var repository: ChatRepository
    private lateinit var useCase: GetMessagesUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetMessagesUseCase(repository)
    }

    @Test
    fun `invoke without channelId should return all messages from repository`() = runBlocking {
        // Given
        val expectedMessages = listOf(
            ChatMessage(
                id = "1",
                message = "Hello",
                userId = "user1",
                userName = "User 1",
                timestamp = 1234567890L,
                channelId = null,
                isSent = true
            ),
            ChatMessage(
                id = "2",
                message = "World",
                userId = "user2",
                userName = "User 2",
                timestamp = 1234567891L,
                channelId = null,
                isSent = false
            )
        )
        coEvery { repository.getMessages() } returns expectedMessages

        // When
        val result = useCase()

        // Then
        assertEquals(expectedMessages, result)
        coVerify(exactly = 1) { repository.getMessages() }
    }

    @Test
    fun `invoke with channelId should return messages for specific channel`() = runBlocking {
        // Given
        val channelId = "channel123"
        val expectedMessages = listOf(
            ChatMessage(
                id = "1",
                message = "Channel message",
                userId = "user1",
                userName = "User 1",
                timestamp = 1234567890L,
                channelId = channelId,
                isSent = true
            )
        )
        coEvery { repository.getMessagesByChannel(channelId) } returns expectedMessages

        // When
        val result = useCase(channelId)

        // Then
        assertEquals(expectedMessages, result)
        coVerify(exactly = 1) { repository.getMessagesByChannel(channelId) }
    }

    @Test
    fun `invoke should return empty list when repository returns empty`() = runBlocking {
        // Given
        val emptyList = emptyList<ChatMessage>()
        coEvery { repository.getMessages() } returns emptyList

        // When
        val result = useCase()

        // Then
        assertEquals(emptyList, result)
        coVerify(exactly = 1) { repository.getMessages() }
    }

    @Test
    fun `invoke should throw exception when repository fails`() {
        // Given
        coEvery { repository.getMessages() } throws Exception("Network error")

        // When/Then
        try {
            runBlocking {
                useCase()
            }
            throw AssertionError("Expected exception was not thrown")
        } catch (e: Exception) {
            assertEquals("Network error", e.message)
        }
    }
}
