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
import java.io.File

class SendAudioMessageUseCaseTest {

    private lateinit var uploadAudioUseCase: UploadAudioUseCase
    private lateinit var repository: ChatRepository
    private lateinit var useCase: SendAudioMessageUseCase

    @Before
    fun setup() {
        uploadAudioUseCase = mockk()
        repository = mockk()
        useCase = SendAudioMessageUseCase(uploadAudioUseCase, repository)
    }

    @Test
    fun `invoke should upload audio and send audio message successfully`() = runBlocking {
        // Given
        val audioFile = mockk<File>(relaxed = true)
        val audioDuration = 30
        val userId = "user123"
        val userName = "John Doe"
        val channelId = "channel1"
        val audioUrl = "https://storage.example.com/audio123.m4a"
        
        val expectedMessage = ChatMessage(
            id = "msg1",
            message = "Audio message",
            userId = userId,
            userName = userName,
            timestamp = 1234567890L,
            channelId = channelId,
            isSent = true
        )
        
        coEvery { uploadAudioUseCase(audioFile) } returns audioUrl
        coEvery { 
            repository.sendAudioMessage(audioUrl, audioDuration, userId, userName, channelId) 
        } returns expectedMessage

        // When
        val result = useCase(audioFile, audioDuration, userId, userName, channelId)

        // Then
        assertEquals(expectedMessage, result)
        coVerify(exactly = 1) { uploadAudioUseCase(audioFile) }
        coVerify(exactly = 1) { 
            repository.sendAudioMessage(audioUrl, audioDuration, userId, userName, channelId) 
        }
    }

    @Test
    fun `invoke should send audio message without channelId`() = runBlocking {
        // Given
        val audioFile = mockk<File>(relaxed = true)
        val audioDuration = 45
        val userId = "user456"
        val userName = "Jane Smith"
        val audioUrl = "https://storage.example.com/audio456.m4a"
        
        val expectedMessage = ChatMessage(
            id = "msg2",
            message = "Audio message",
            userId = userId,
            userName = userName,
            timestamp = 1234567890L,
            channelId = null,
            isSent = true
        )
        
        coEvery { uploadAudioUseCase(audioFile) } returns audioUrl
        coEvery { 
            repository.sendAudioMessage(audioUrl, audioDuration, userId, userName, null) 
        } returns expectedMessage

        // When
        val result = useCase(audioFile, audioDuration, userId, userName)

        // Then
        assertEquals(expectedMessage, result)
        coVerify(exactly = 1) { uploadAudioUseCase(audioFile) }
        coVerify(exactly = 1) { 
            repository.sendAudioMessage(audioUrl, audioDuration, userId, userName, null) 
        }
    }

    @Test
    fun `invoke should handle zero duration`() = runBlocking {
        // Given
        val audioFile = mockk<File>(relaxed = true)
        val audioDuration = 0
        val userId = "user789"
        val userName = "Bob"
        val audioUrl = "https://storage.example.com/audio789.m4a"
        
        val expectedMessage = ChatMessage(
            id = "msg3",
            message = "Audio message",
            userId = userId,
            userName = userName,
            timestamp = 1234567890L,
            channelId = null,
            isSent = true
        )
        
        coEvery { uploadAudioUseCase(audioFile) } returns audioUrl
        coEvery { 
            repository.sendAudioMessage(audioUrl, audioDuration, userId, userName, null) 
        } returns expectedMessage

        // When
        val result = useCase(audioFile, audioDuration, userId, userName)

        // Then
        assertEquals(expectedMessage, result)
        coVerify(exactly = 1) { uploadAudioUseCase(audioFile) }
    }

    @Test
    fun `invoke should propagate upload exception`() {
        // Given
        val audioFile = mockk<File>(relaxed = true)
        val audioDuration = 30
        val userId = "user123"
        val userName = "John"
        
        coEvery { uploadAudioUseCase(audioFile) } throws Exception("Upload failed")

        // When/Then
        try {
            runBlocking {
                useCase(audioFile, audioDuration, userId, userName)
            }
            throw AssertionError("Expected exception was not thrown")
        } catch (e: Exception) {
            assertEquals("Upload failed", e.message)
        }
        
        coVerify(exactly = 1) { uploadAudioUseCase(audioFile) }
    }

    @Test
    fun `invoke should propagate repository exception after successful upload`() {
        // Given
        val audioFile = mockk<File>(relaxed = true)
        val audioDuration = 30
        val userId = "user123"
        val userName = "John"
        val audioUrl = "https://storage.example.com/audio.m4a"
        
        coEvery { uploadAudioUseCase(audioFile) } returns audioUrl
        coEvery { 
            repository.sendAudioMessage(audioUrl, audioDuration, userId, userName, null) 
        } throws Exception("Repository error")

        // When/Then
        try {
            runBlocking {
                useCase(audioFile, audioDuration, userId, userName)
            }
            throw AssertionError("Expected exception was not thrown")
        } catch (e: Exception) {
            assertEquals("Repository error", e.message)
        }
        
        coVerify(exactly = 1) { uploadAudioUseCase(audioFile) }
        coVerify(exactly = 1) { 
            repository.sendAudioMessage(audioUrl, audioDuration, userId, userName, null) 
        }
    }
}
