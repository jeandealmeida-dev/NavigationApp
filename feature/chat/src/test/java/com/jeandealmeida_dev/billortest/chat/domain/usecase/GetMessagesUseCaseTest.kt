package com.jeandealmeida_dev.billortest.chat.domain.usecase

import com.jeandealmeida_dev.billortest.chat.data.repository.ChatRepository
import com.jeandealmeida_dev.billortest.chat.domain.model.ChatMessage
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
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
    fun `invoke with channelId should return flow of messages for specific channel`() = runBlocking {
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
        val expectedFlow = flowOf(expectedMessages)
        coEvery { repository.getMessagesByChannel(channelId) } returns expectedFlow

        val result = useCase(channelId)

        assertEquals(expectedFlow, result)
        coVerify(exactly = 1) { repository.getMessagesByChannel(channelId) }
    }

    @Test
    fun `invoke should return flow with empty list when repository returns empty`() = runBlocking {
        val channelId = "channel123"
        val emptyList = emptyList<ChatMessage>()
        val expectedFlow = flowOf(emptyList)
        coEvery { repository.getMessagesByChannel(channelId) } returns expectedFlow

        val result = useCase(channelId)

        assertEquals(expectedFlow, result)
        coVerify(exactly = 1) { repository.getMessagesByChannel(channelId) }
    }

    @Test
    fun `invoke should throw exception when repository fails`() {
        val channelId = "channel123"
        coEvery { repository.getMessagesByChannel(channelId) } throws Exception("Network error")

        try {
            runBlocking {
                useCase(channelId)
            }
            throw AssertionError("Expected exception was not thrown")
        } catch (e: Exception) {
            assertEquals("Network error", e.message)
        }
    }

    @Test
    fun `invoke with different channelId should call repository with correct parameter`() = runBlocking {
        val channelId = "different-channel"
        val messages = listOf(
            ChatMessage(
                id = "2",
                message = "Different channel message",
                userId = "user2",
                userName = "User 2",
                timestamp = 1234567891L,
                channelId = channelId,
                isSent = false
            )
        )
        val expectedFlow = flowOf(messages)
        coEvery { repository.getMessagesByChannel(channelId) } returns expectedFlow

        val result = useCase(channelId)

        assertEquals(expectedFlow, result)
        coVerify(exactly = 1) { repository.getMessagesByChannel(channelId) }
    }
}
