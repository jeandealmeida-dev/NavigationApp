package com.jeandealmeida_dev.billortest.chat.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jeandealmeida_dev.billortest.chat.data.local.dao.ChatMessageDao
import com.jeandealmeida_dev.billortest.chat.data.local.entity.ChatMessageEntity

/**
 * Room database for chat messages
 */
@Database(
    entities = [ChatMessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ChatDatabase : RoomDatabase() {
    
    abstract fun chatMessageDao(): ChatMessageDao
    
    companion object {
        const val DATABASE_NAME = "chat_database"
    }
}
