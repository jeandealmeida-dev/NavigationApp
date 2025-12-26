package com.jeandealmeida_dev.billortest.chat.domain.usecase

import com.jeandealmeida_dev.billortest.chat.data.repository.ChatRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SubscribeToTypingStatusUseCaseTest {

    private lateinit var repository: ChatRepository
    private lateinit var useCase: SubscribeToTypingStatusUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = SubscribeToTypingStatusUseCase(repository)
    }

    @Test
    fun `invoke should return flow of typing users from repository`() = runBlocking {
        val channelId = "channel123"
        val typingUsers = listOf("user1", "user2")
        val expectedFlow = flowOf(typingUsers)

        coEvery { repository.subscribeToRealtimeTypingStatus(channelId) } returns expectedFlow

        val result = useCase(channelId)

        assertEquals(expectedFlow, result)
        coVerify(exactly = 1) { repository.subscribeToRealtimeTypingStatus(channelId) }
    }

    @Test
    fun `invoke should return flow with empty list when no users typing`() = runBlocking {
        val channelId = "channel123"
        val emptyList = emptyList<String>()
        val expectedFlow = flowOf(emptyList)

        coEvery { repository.subscribeToRealtimeTypingStatus(channelId) } returns expectedFlow

        val result = useCase(channelId)

        assertEquals(expectedFlow, result)
        coVerify(exactly = 1) { repository.subscribeToRealtimeTypingStatus(channelId) }
    }

    @Test
    fun `invoke with different channelId should call repository with correct parameter`() = runBlocking {
        val channelId = "different-channel"
        val typingUsers = listOf("user3")
        val expectedFlow = flowOf(typingUsers)

        coEvery { repository.subscribeToRealtimeTypingStatus(channelId) } returns expectedFlow

        val result = useCase(channelId)

        assertEquals(expectedFlow, result)
        coVerify(exactly = 1) { repository.subscribeToRealtimeTypingStatus(channelId) }
    }

    @Test
    fun `invoke should propagate repository exceptions`() {
        val channelId = "channel123"
        val expectedException = RuntimeException("Connection error")

        coEvery { repository.subscribeToRealtimeTypingStatus(channelId) } throws expectedException

        try {
            runBlocking {
                useCase(channelId)
            }
            throw AssertionError("Expected RuntimeException was not thrown")
        } catch (e: RuntimeException) {
            assertEquals("Connection error", e.message)
        }
    }
}
