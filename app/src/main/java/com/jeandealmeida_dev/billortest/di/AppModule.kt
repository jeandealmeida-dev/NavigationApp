package com.jeandealmeida_dev.billortest.di

import com.jeandealmeida_dev.billortest.chat.di.ChatModule
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Main application-level Hilt module
 * 
 * This module serves as the central configuration point for dependency injection
 * and explicitly includes feature modules for better visibility and organization.
 * 
 * Included feature modules:
 * - ChatModule: Provides chat-related dependencies (from feature:chat)
 * - Future feature modules will be added here
 */
@Module(includes = [ChatModule::class])
@InstallIn(SingletonComponent::class)
object AppModule {
    // Application-wide dependencies can be provided here
    // Feature-specific dependencies are provided in their respective modules
}
