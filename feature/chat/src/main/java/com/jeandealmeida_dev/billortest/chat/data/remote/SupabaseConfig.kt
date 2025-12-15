package com.jeandealmeida_dev.billortest.chat.data.remote

/**
 * Configuration class for Supabase connection
 * 
 * To use this, you need to:
 * 1. Add Supabase dependencies to build.gradle.kts
 * 2. Configure your Supabase URL and API key
 * 3. Set up your Supabase database tables
 */
object SupabaseConfig {
    
    /**
     * Supabase project URL
     * Replace with your actual Supabase project URL
     */
    const val SUPABASE_URL = "https://your-project-id.supabase.co"
    
    /**
     * Supabase anonymous API key
     * Replace with your actual Supabase anonymous key
     */
    const val SUPABASE_ANON_KEY = "your-supabase-anon-key"
    
    /**
     * Name of the chat messages table in Supabase
     */
    const val MESSAGES_TABLE = "messages"
    
    /**
     * Name of the realtime channel for chat
     */
    const val CHAT_CHANNEL = "public:messages"
    
    /**
     * Timeout for network operations in seconds
     */
    const val TIMEOUT_SECONDS = 30L
}
