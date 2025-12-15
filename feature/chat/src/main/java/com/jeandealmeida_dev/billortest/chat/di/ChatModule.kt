package com.jeandealmeida_dev.billortest.chat.di

import android.content.Context
import androidx.room.Room
import com.jeandealmeida_dev.billortest.chat.data.local.ChatDatabase
import com.jeandealmeida_dev.billortest.chat.data.local.ChatLocalDataSource
import com.jeandealmeida_dev.billortest.chat.data.local.dao.ChatMessageDao
import com.jeandealmeida_dev.billortest.chat.data.remote.ChatRemoteDataSource
import com.jeandealmeida_dev.billortest.chat.data.remote.SupabaseChatRemoteDataSource
import com.jeandealmeida_dev.billortest.chat.data.repository.ChatRepository
import com.jeandealmeida_dev.billortest.chat.data.repository.ChatRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing chat-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ChatModule {
    
    @Binds
    @Singleton
    abstract fun bindChatRepository(
        chatRepositoryImpl: ChatRepositoryImpl
    ): ChatRepository
    
    @Binds
    @Singleton
    abstract fun bindChatRemoteDataSource(
        supabaseChatRemoteDataSource: SupabaseChatRemoteDataSource
    ): ChatRemoteDataSource
    
    companion object {
        
        @Provides
        @Singleton
        fun provideChatDatabase(
            @ApplicationContext context: Context
        ): ChatDatabase {
            return Room.databaseBuilder(
                context,
                ChatDatabase::class.java,
                ChatDatabase.DATABASE_NAME
            ).build()
        }
        
        @Provides
        @Singleton
        fun provideChatMessageDao(
            database: ChatDatabase
        ): ChatMessageDao {
            return database.chatMessageDao()
        }
    }
}
