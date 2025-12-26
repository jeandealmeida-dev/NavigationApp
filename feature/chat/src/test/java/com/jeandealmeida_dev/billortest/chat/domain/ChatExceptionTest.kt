package com.jeandealmeida_dev.billortest.chat.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatExceptionTest {

    @Test
    fun `MessageSentException should have correct message`() {
        val exception = ChatException.MessageSentException("Failed to send message")

        assertEquals("Failed to send message", exception.message)
        assertTrue(exception is ChatException)
    }

    @Test
    fun `EmptyMessageException should have default message`() {
        val exception = ChatException.EmptyMessageException()

        assertEquals("Message cannot be empty", exception.message)
        assertTrue(exception is ChatException)
    }

    @Test
    fun `GetMessageException should have correct message`() {
        val exception = ChatException.GetMessageException("Failed to retrieve messages")

        assertEquals("Failed to retrieve messages", exception.message)
        assertTrue(exception is ChatException)
    }

    @Test
    fun `AudioSentException should have correct message`() {
        val exception = ChatException.AudioSentException("Failed to send audio")

        assertEquals("Failed to send audio", exception.message)
        assertTrue(exception is ChatException)
    }

    @Test
    fun `ClearMessageException should have correct message`() {
        val exception = ChatException.ClearMessageException("Failed to clear messages")

        assertEquals("Failed to clear messages", exception.message)
        assertTrue(exception is ChatException)
    }

    @Test
    fun `RealtimeConnectionException should have correct message`() {
        val exception = ChatException.RealtimeConnectionException("Connection lost")

        assertEquals("Connection lost", exception.message)
        assertEquals("Connection lost", exception.realtimeErrorMessage)
        assertTrue(exception is ChatException)
    }

    @Test
    fun `TypingStatusException should have correct message`() {
        val exception = ChatException.TypingStatusException("Failed to update typing status")

        assertEquals("Failed to update typing status", exception.message)
        assertTrue(exception is ChatException)
    }

    @Test
    fun `ChatException should be throwable as Exception`() {
        val exception = ChatException.MessageSentException("Test error")

        assertTrue(exception is Exception)
        assertTrue(exception is Throwable)
    }

    @Test
    fun `RealtimeConnectionException equality should work with same message`() {
        val exception1 = ChatException.RealtimeConnectionException("Error")
        val exception2 = ChatException.RealtimeConnectionException("Error")

        assertEquals(exception1, exception2)
    }

    @Test
    fun `All exception types should be instances of ChatException`() {
        val messageSent = ChatException.MessageSentException("error")
        val emptyMessage = ChatException.EmptyMessageException()
        val getMessage = ChatException.GetMessageException("error")
        val audioSent = ChatException.AudioSentException("error")
        val clearMessage = ChatException.ClearMessageException("error")
        val realtimeConnection = ChatException.RealtimeConnectionException("error")
        val typingStatus = ChatException.TypingStatusException("error")

        assertTrue(messageSent is ChatException)
        assertTrue(emptyMessage is ChatException)
        assertTrue(getMessage is ChatException)
        assertTrue(audioSent is ChatException)
        assertTrue(clearMessage is ChatException)
        assertTrue(realtimeConnection is ChatException)
        assertTrue(typingStatus is ChatException)
    }
}
