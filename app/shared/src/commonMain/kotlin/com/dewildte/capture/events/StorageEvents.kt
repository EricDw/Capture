package com.dewildte.capture.events

import com.dewildte.capture.data.Conversation

sealed interface StorageEvent : Event
data class AiStorageFolderSelected(val folderPath: String) : StorageEvent
data class ConversationsLoaded(val conversations: List<Conversation>) : StorageEvent
data object FailedToSelectStorageFolder : StorageEvent
