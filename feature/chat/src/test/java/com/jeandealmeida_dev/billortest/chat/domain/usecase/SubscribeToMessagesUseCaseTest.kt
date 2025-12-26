package com.jeandealmeida_dev.billortest.chat.domain.usecase

import com.jeandealmeida_dev.billortest.chat.data.repository.ChatRepository
import com.jeandealmeida_dev.billortest.chat.domain.model.ChatMessage
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SubscribeToMessagesUseCaseTest {

    private lateinit var repository: ChatRepository
    private lateinit var useCase: SubscribeToMessagesUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = SubscribeToMessagesUseCase(repository)
    }

    @Test
    fun `invoke should return flow from repository`() = runBlocking {
        // Given
        val message = ChatMessage(
            id = "msg1",
            message = "New message",
            userId = "user123",
            userName = "John Doe",
            timestamp = 1234567890L,
            channelId = null,
            isSent = false
        )
        
        val messageFlow = flowOf(message)
        coEvery { repository.subscribeToRealtimeMessages() } returns messageFlow

        // When
        val result = useCase()

        // Then
        val receivedMessage = result.first()
        assertEquals(message, receivedMessage)
        coVerify(exactly = 1) { repository.subscribeToRealtimeMessages() }
    }

    @Test
    fun `invoke should emit multiple messages`() = runBlocking {
        // Given
        val messages = listOf(
            ChatMessage(
                id = "msg1",
                message = "First message",
                userId = "user1",
                userName = "User 1",
                timestamp = 1234567890L,
                channelId = null,
                isSent = false
            ),
            ChatMessage(
                id = "msg2",
                message = "Second message",
                userId = "user2",
                userName = "User 2",
                timestamp = 1234567891L,
                channelId = null,
                isSent = false
            ),
            ChatMessage(
                id = "msg3",
                message = "Third message",
                userId = "user3",
                userName = "User 3",
                timestamp = 1234567892L,
                channelId = null,
                isSent = false
            )
        )
        
        val messageFlow = flow {
            messages.forEach { emit(it) }
        }
        
        coEvery { repository.subscribeToRealtimeMessages() } returns messageFlow

        // When
        val result = useCase()

        // Then
        val receivedMessages = result.toList()
        assertEquals(messages.size, receivedMessages.size)
        assertEquals(messages, receivedMessages)
        coVerify(exactly = 1) { repository.subscribeToRealtimeMessages() }
    }

    @Test
    fun `invoke should handle empty flow`() = runBlocking {
        // Given
        val emptyFlow = flow<ChatMessage> { }
        coEvery { repository.subscribeToRealtimeMessages() } returns emptyFlow

        // When
        val result = useCase()

        // Then
        val receivedMessages = result.toList()
        assertEquals(0, receivedMessages.size)
        coVerify(exactly = 1) { repository.subscribeToRealtimeMessages() }
    }

    @Test
    fun `invoke should propagate repository exception`() {
        // Given
        coEvery { repository.subscribeToRealtimeMessages() } throws Exception("Connection error")

        // When/Then
        try {
            runBlocking {
                useCase()
            }
            throw AssertionError("Expected exception was not thrown")
        } catch (e: Exception) {
            assertEquals("Connection error", e.message)
        }
        
        coVerify(exactly = 1) { repository.subscribeToRealtimeMessages() }
    }

    @Test
    fun `invoke should handle messages from different channels`() = runBlocking {
        // Given
        val messages = listOf(
            ChatMessage(
                id = "msg1",
                message = "Channel 1 message",
                userId = "user1",
                userName = "User 1",
                timestamp = 1234567890L,
                channelId = "channel1",
                isSent = false
            ),
            ChatMessage(
                id = "msg2",
                message = "Channel 2 message",
                userId = "user2",
                userName = "User 2",
                timestamp = 1234567891L,
                channelId = "channel2",
                isSent = false
            ),
            ChatMessage(
                id = "msg3",
                message = "No channel message",
                userId = "user3",
                userName = "User 3",
                timestamp = 1234567892L,
                channelId = null,
                isSent = false
            )
        )
        
        val messageFlow = flow {
            messages.forEach { emit(it) }
        }
        
        coEvery { repository.subscribeToRealtimeMessages() } returns messageFlow

        // When
        val result = useCase()

        // Then
        val receivedMessages = result.toList()
        assertEquals(messages.size, receivedMessages.size)
        assertEquals("channel1", receivedMessages[0].channelId)
        assertEquals("channel2", receivedMessages[1].channelId)
        assertEquals(null, receivedMessages[2].channelId)
    }
}
