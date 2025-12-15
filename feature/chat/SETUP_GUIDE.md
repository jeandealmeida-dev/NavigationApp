# Chat Feature - Quick Setup Guide

This guide will help you quickly set up and integrate the realtime chat feature in your application.

## Project Structure Created

```
feature/chat/
├── build.gradle.kts                          # Module build configuration
├── README.md                                  # Comprehensive documentation
├── SETUP_GUIDE.md                            # This file
└── src/main/
    ├── java/com/jeandealmeida_dev/billortest/chat/
    │   ├── data/
    │   │   ├── local/
    │   │   │   ├── dao/
    │   │   │   │   └── ChatMessageDao.kt          # Room DAO interface
    │   │   │   ├── entity/
    │   │   │   │   └── ChatMessageEntity.kt       # Room entity
    │   │   │   └── ChatLocalDataSource.kt         # Local data source
    │   │   ├── remote/
    │   │   │   ├── model/
    │   │   │   │   └── ChatMessageDto.kt          # API DTO
    │   │   │   ├── ChatRemoteDataSource.kt        # Remote interface
    │   │   │   ├── SupabaseChatRemoteDataSource.kt # Supabase implementation
    │   │   │   └── SupabaseConfig.kt              # Supabase configuration
    │   │   ├── mapper/
    │   │   │   └── ChatMessageMapper.kt           # Layer mappers
    │   │   └── repository/
    │   │       ├── ChatRepository.kt              # Repository interface
    │   │       └── ChatRepositoryImpl.kt          # Repository implementation
    │   ├── domain/
    │   │   ├── model/
    │   │   │   └── ChatMessage.kt                 # Domain model
    │   │   └── usecase/
    │   │       ├── GetMessagesUseCase.kt          # Get messages use case
    │   │       ├── SendMessageUseCase.kt          # Send message use case
    │   │       └── SubscribeToMessagesUseCase.kt  # Realtime subscription
    │   └── ui/
    │       ├── adapter/
    │       │   └── ChatMessageAdapter.kt          # RecyclerView adapter
    │       ├── viewmodel/
    │       │   └── ChatViewModel.kt               # ViewModel
    │       └── ChatFragment.kt                    # Main UI fragment
    └── res/
        ├── layout/
        │   ├── fragment_chat.xml                  # Main chat layout
        │   ├── item_message_sent.xml              # Sent message item
        │   └── item_message_received.xml          # Received message item
        └── values/
            └── strings.xml                        # String resources
```

## Quick Start

### Step 1: Supabase Project Setup

1. Go to [supabase.com](https://supabase.com) and create a new project
2. In the SQL Editor, run this SQL to create the messages table:

```sql
CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    message TEXT NOT NULL,
    user_id TEXT NOT NULL,
    user_name TEXT NOT NULL,
    channel_id TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

ALTER PUBLICATION supabase_realtime ADD TABLE messages;

CREATE INDEX idx_messages_created_at ON messages(created_at);
CREATE INDEX idx_messages_channel_id ON messages(channel_id);
```

3. Get your project URL and anon key from Settings > API

### Step 2: Configure Supabase in Your App

Edit `feature/chat/src/main/java/.../chat/data/remote/SupabaseConfig.kt`:

```kotlin
const val SUPABASE_URL = "https://xxxxxxxxxxxxx.supabase.co"  // Your project URL
const val SUPABASE_ANON_KEY = "eyJhbGc..."                    // Your anon key
```

### Step 3: Add Supabase Dependencies

Edit `feature/chat/build.gradle.kts` and uncomment the Supabase dependencies:

```kotlin
// Update versions to latest
implementation("io.github.jan-tennert.supabase:postgrest-kt:2.0.0")
implementation("io.github.jan-tennert.supabase:realtime-kt:2.0.0")
implementation("io.ktor:ktor-client-android:2.3.0")
```

Also add to your project's `libs.versions.toml` if using version catalog:

```toml
[versions]
supabase = "2.0.0"
ktor = "2.3.0"

[libraries]
supabase-postgrest = { module = "io.github.jan-tennert.supabase:postgrest-kt", version.ref = "supabase" }
supabase-realtime = { module = "io.github.jan-tennert.supabase:realtime-kt", version.ref = "supabase" }
ktor-client-android = { module = "io.ktor:ktor-client-android", version.ref = "ktor" }
```

### Step 4: Implement Supabase Client

Edit `feature/chat/src/main/java/.../chat/data/remote/SupabaseChatRemoteDataSource.kt`:

Uncomment and implement the TODO sections following the example code in the comments.

### Step 5: Add Chat to Navigation

Add ChatFragment to your navigation graph:

```xml
<fragment
    android:id="@+id/nav_chat"
    android:name="com.jeandealmeida_dev.billortest.chat.ui.ChatFragment"
    android:label="@string/chat_title"
    tools:layout="@layout/fragment_chat" />
```

### Step 6: Set Up Dependency Injection

If using Dagger/Hilt, create a module:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object ChatModule {
    
    @Provides
    @Singleton
    fun provideChatRemoteDataSource(): ChatRemoteDataSource {
        return SupabaseChatRemoteDataSource()
    }
    
    @Provides
    @Singleton
    fun provideChatRepository(
        localDataSource: ChatLocalDataSource,
        remoteDataSource: ChatRemoteDataSource
    ): ChatRepository {
        return ChatRepositoryImpl(localDataSource, remoteDataSource)
    }
}
```

### Step 7: Set Up Room Database

Add ChatMessageDao to your Room database:

```kotlin
@Database(
    entities = [
        // ... your other entities
        ChatMessageEntity::class
    ],
    version = X  // Increment your version
)
abstract class AppDatabase : RoomDatabase() {
    // ... your other DAOs
    abstract fun chatMessageDao(): ChatMessageDao
}
```

## Testing the Feature

### Basic Test Flow

1. Launch the app and navigate to the chat screen
2. Send a message - it should appear in your list with green background (right-aligned)
3. Open another device/emulator or browser with Supabase Studio
4. Insert a message via Supabase Studio or another device
5. The message should appear in realtime with white background (left-aligned)

### Test Message via Supabase Studio

In Supabase Studio, go to Table Editor > messages and insert:

```json
{
  "message": "Hello from Supabase!",
  "user_id": "test_user_2",
  "user_name": "Test User"
}
```

## Common Issues & Solutions

### Issue: Messages not appearing in realtime

**Solution**: 
- Check that you ran `ALTER PUBLICATION supabase_realtime ADD TABLE messages;`
- Verify your Supabase anon key is correct
- Check Logcat for connection errors

### Issue: Build errors about missing dependencies

**Solution**:
- Sync Gradle files
- Make sure all required dependencies are in libs.versions.toml
- Clean and rebuild project

### Issue: Room migration errors

**Solution**:
- Increment your database version
- Provide a migration strategy or use `.fallbackToDestructiveMigration()`

## Key Features Implemented

✅ **Realtime Chat**: Messages sync instantly across all connected clients  
✅ **Offline Support**: Messages cached locally with Room database  
✅ **Clean Architecture**: Separated UI, Domain, and Data layers  
✅ **Reactive**: RxJava for reactive data streams  
✅ **Modern UI**: Material Design with distinct sent/received message bubbles  
✅ **Validation**: Message validation before sending  
✅ **Error Handling**: Comprehensive error handling and user feedback  

## Next Steps

1. Integrate with your authentication system
2. Add user profile pictures
3. Implement typing indicators
4. Add push notifications for new messages
5. Support for channels/groups
6. Message editing and deletion
7. File/image sharing

## Resources

- [Supabase Documentation](https://supabase.com/docs)
- [Supabase Kotlin Client](https://github.com/supabase-community/supabase-kt)
- [RxJava Documentation](https://github.com/ReactiveX/RxJava)
- Full feature documentation: See `README.md`

## Support

For detailed architecture and API documentation, refer to `README.md` in this directory.
