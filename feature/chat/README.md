# Chat Feature Module

This module implements a realtime chat feature with Supabase integration, following Clean Architecture principles with UI, Domain, and Data layers.

## Architecture Overview

### Data Layer
- **Local Data Source**: Room database for offline caching
  - `ChatMessageEntity`: Room entity for local storage
  - `ChatMessageDao`: DAO for database operations
  - `ChatLocalDataSource`: Wrapper for local operations

- **Remote Data Source**: Supabase integration for realtime chat
  - `ChatRemoteDataSource`: Interface for remote operations
  - `SupabaseChatRemoteDataSource`: Supabase implementation with realtime subscriptions
  - `ChatMessageDto`: Data transfer object for API communication
  - `SupabaseConfig`: Configuration for Supabase connection

- **Repository**: Coordinates local and remote data sources
  - `ChatRepository`: Repository interface
  - `ChatRepositoryImpl`: Implementation with caching and sync logic

- **Mapper**: Converts between layers
  - `ChatMessageMapper`: Maps between Entity, DTO, and Domain models

### Domain Layer
- **Models**:
  - `ChatMessage`: Core domain model with business logic

- **Use Cases**:
  - `GetMessagesUseCase`: Retrieve messages from repository
  - `SendMessageUseCase`: Send new messages with validation
  - `SubscribeToMessagesUseCase`: Subscribe to realtime message updates

### UI Layer
- **ViewModels**:
  - `ChatViewModel`: Manages UI state and business logic

- **Views**:
  - `ChatFragment`: Main chat UI with message list and input
  - `ChatMessageAdapter`: RecyclerView adapter with separate views for sent/received messages

- **Layouts**:
  - `fragment_chat.xml`: Main chat screen layout
  - `item_message_sent.xml`: Layout for sent messages (right-aligned, green)
  - `item_message_received.xml`: Layout for received messages (left-aligned, white)

## Supabase Integration

### Prerequisites
1. Create a Supabase project at https://supabase.com
2. Set up a `messages` table with the following schema:

```sql
CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    message TEXT NOT NULL,
    user_id TEXT NOT NULL,
    user_name TEXT NOT NULL,
    channel_id TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Enable realtime for the table
ALTER PUBLICATION supabase_realtime ADD TABLE messages;

-- Create index for better query performance
CREATE INDEX idx_messages_created_at ON messages(created_at);
CREATE INDEX idx_messages_channel_id ON messages(channel_id);
```

### Setup Steps

1. **Update SupabaseConfig.kt**:
   ```kotlin
   const val SUPABASE_URL = "https://your-project-id.supabase.co"
   const val SUPABASE_ANON_KEY = "your-supabase-anon-key"
   ```

2. **Add Supabase Dependencies** (in `build.gradle.kts`):
   ```kotlin
   // Uncomment these lines and update VERSION to the latest
   implementation("io.github.jan-tennert.supabase:postgrest-kt:2.0.0")
   implementation("io.github.jan-tennert.supabase:realtime-kt:2.0.0")
   implementation("io.ktor:ktor-client-android:2.3.0")
   ```

3. **Initialize Supabase Client** (in `SupabaseChatRemoteDataSource.kt`):
   ```kotlin
   private val supabaseClient = createSupabaseClient {
       install(Postgrest)
       install(Realtime)
       supabaseUrl = SupabaseConfig.SUPABASE_URL
       supabaseKey = SupabaseConfig.SUPABASE_ANON_KEY
   }
   ```

4. **Implement API Calls**:
   - Uncomment and implement the TODO sections in `SupabaseChatRemoteDataSource.kt`
   - Follow the example code provided in the comments

## Features

- ✅ Realtime message synchronization
- ✅ Offline support with local caching
- ✅ Message sending with validation
- ✅ Distinct UI for sent vs received messages
- ✅ Automatic timestamp formatting
- ✅ Clean Architecture (UI/Domain/Data separation)
- ✅ Reactive programming with RxJava
- ✅ Room database for persistence

## Dependencies

### Core
- AndroidX (ConstraintLayout, RecyclerView, CardView)
- Material Components
- ViewModel & LiveData

### Data
- Room Database
- Retrofit
- Moshi (JSON serialization)
- RxJava 3 & RxAndroid

### Dependency Injection
- Dagger/Hilt

### To Add (Supabase)
- Supabase Kotlin Client
- Ktor Client (for Supabase)

## Usage

### In Navigation Graph
Add the ChatFragment to your navigation graph:

```xml
<fragment
    android:id="@+id/nav_chat"
    android:name="com.jeandealmeida_dev.billortest.chat.ui.ChatFragment"
    android:label="@string/chat_title"
    tools:layout="@layout/fragment_chat" />
```

### Setting User Information
```kotlin
chatViewModel.setCurrentUser(
    userId = "user_123",
    userName = "John Doe"
)
```

### Observing Messages
```kotlin
chatViewModel.messages.observe(viewLifecycleOwner) { messages ->
    // Handle messages update
}
```

### Sending Messages
```kotlin
chatViewModel.sendMessage("Hello, world!")
```

## Testing

The module is structured for easy testing:
- **Unit Tests**: Test use cases and ViewModels in isolation
- **Integration Tests**: Test Repository with fake data sources
- **UI Tests**: Test Fragment and Adapter behavior

## Future Enhancements

- [ ] Message read receipts
- [ ] Typing indicators
- [ ] Image/file sharing
- [ ] Push notifications
- [ ] Message search
- [ ] User presence status
- [ ] Message editing/deletion
- [ ] Channel management
- [ ] Authentication integration

## Notes

- The current implementation has placeholder code for Supabase integration
- All TODO comments indicate where actual Supabase code should be implemented
- The structure is ready; just add Supabase dependencies and implement the TODOs
- User authentication should be integrated with your app's auth system
