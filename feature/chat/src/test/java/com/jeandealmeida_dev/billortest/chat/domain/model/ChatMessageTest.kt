package com.jeandealmeida_dev.billortest.chat.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatMessageTest {

    @Test
    fun `isMine should return true when userId matches currentUserId`() {
        val message = ChatMessage(
            id = "1",
            message = "Hello",
            userId = "user123",
            userName = "Test User",
            timestamp = 1234567890L
        )

        val result = message.isMine("user123")

        assertTrue(result)
    }

    @Test
    fun `isMine should return false when userId does not match currentUserId`() {
        val message = ChatMessage(
            id = "1",
            message = "Hello",
            userId = "user123",
            userName = "Test User",
            timestamp = 1234567890L
        )

        val result = message.isMine("user456")

        assertFalse(result)
    }

    @Test
    fun `isMine should return false when userId is empty and currentUserId is not`() {
        val message = ChatMessage(
            id = "1",
            message = "Hello",
            userId = "",
            userName = "Test User",
            timestamp = 1234567890L
        )

        val result = message.isMine("user123")

        assertFalse(result)
    }

    @Test
    fun `isMine should return true when both userId and currentUserId are empty`() {
        val message = ChatMessage(
            id = "1",
            message = "Hello",
            userId = "",
            userName = "Test User",
            timestamp = 1234567890L
        )

        val result = message.isMine("")

        assertTrue(result)
    }

    @Test
    fun `ChatMessage should have correct default values`() {
        val message = ChatMessage(
            id = "1",
            message = "Hello",
            userId = "user123",
            userName = "Test User",
            timestamp = 1234567890L
        )

        assertEquals(null, message.channelId)
        assertEquals(true, message.isSent)
        assertEquals(MessageType.TEXT, message.messageType)
        assertEquals(null, message.audioUrl)
        assertEquals(null, message.audioDuration)
    }

    @Test
    fun `ChatMessage with audio type should store audio properties correctly`() {
        val message = ChatMessage(
            id = "1",
            message = "",
            userId = "user123",
            userName = "Test User",
            timestamp = 1234567890L,
            messageType = MessageType.AUDIO,
            audioUrl = "https://example.com/audio.m4a",
            audioDuration = 30
        )

        assertEquals(MessageType.AUDIO, message.messageType)
        assertEquals("https://example.com/audio.m4a", message.audioUrl)
        assertEquals(30, message.audioDuration)
    }

    @Test
    fun `ChatMessage with channelId should store it correctly`() {
        val message = ChatMessage(
            id = "1",
            message = "Hello",
            userId = "user123",
            userName = "Test User",
            timestamp = 1234567890L,
            channelId = "channel456"
        )

        assertEquals("channel456", message.channelId)
    }

    @Test
    fun `ChatMessage with isSent false should store it correctly`() {
        val message = ChatMessage(
            id = "1",
            message = "Hello",
            userId = "user123",
            userName = "Test User",
            timestamp = 1234567890L,
            isSent = false
        )

        assertFalse(message.isSent)
    }

    @Test
    fun `ChatMessage equality should work correctly`() {
        val message1 = ChatMessage(
            id = "1",
            message = "Hello",
            userId = "user123",
            userName = "Test User",
            timestamp = 1234567890L
        )

        val message2 = ChatMessage(
            id = "1",
            message = "Hello",
            userId = "user123",
            userName = "Test User",
            timestamp = 1234567890L
        )

        assertEquals(message1, message2)
    }

    @Test
    fun `MessageType enum should have TEXT and AUDIO values`() {
        val textType = MessageType.TEXT
        val audioType = MessageType.AUDIO

        assertEquals("TEXT", textType.name)
        assertEquals("AUDIO", audioType.name)
    }
}
