package com.dewildte.capture.events

sealed interface AiContentEvent : Event

// Conversation list events
data object NewConversationClicked : AiContentEvent
data class ConversationClicked(val conversationId: String) : AiContentEvent
data class DeleteConversationClicked(val conversationId: String) : AiContentEvent

// Chat events
data class MessageInputChanged(val text: String) : AiContentEvent
data object SendMessageClicked : AiContentEvent
data object StopGeneratingClicked : AiContentEvent
data object BackToConversationsClicked : AiContentEvent
data object SelectStorageFolderClicked : AiContentEvent
data object SelectModelClicked : AiContentEvent
data class ModelSelected(val name: String) : AiContentEvent
