package com.jeandealmeida_dev.billortest.chat.domain.usecase

import com.jeandealmeida_dev.billortest.chat.data.repository.ChatRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SubscribeToMessagesUseCaseTest {

    private lateinit var repository: ChatRepository
    private lateinit var useCase: SubscribeToMessagesUseCase

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        useCase = SubscribeToMessagesUseCase(repository)
    }

    @Test
    fun `invoke should call repository subscribeToRealtimeMessages`() = runBlocking {
        val channelId = "channel123"

        coEvery { repository.subscribeToRealtimeMessages(channelId) } returns Unit

        useCase(channelId)

        coVerify(exactly = 1) { repository.subscribeToRealtimeMessages(channelId) }
    }

    @Test
    fun `invoke with different channelId should call repository with correct parameter`() = runBlocking {
        val channelId = "different-channel"

        coEvery { repository.subscribeToRealtimeMessages(channelId) } returns Unit

        useCase(channelId)

        coVerify(exactly = 1) { repository.subscribeToRealtimeMessages(channelId) }
    }

    @Test
    fun `invoke should propagate repository exception`() {
        val channelId = "channel123"
        val expectedException = RuntimeException("Connection error")

        coEvery { repository.subscribeToRealtimeMessages(channelId) } throws expectedException

        try {
            runBlocking {
                useCase(channelId)
            }
            throw AssertionError("Expected RuntimeException was not thrown")
        } catch (e: RuntimeException) {
            assertEquals("Connection error", e.message)
        }
        
        coVerify(exactly = 1) { repository.subscribeToRealtimeMessages(channelId) }
    }
}
