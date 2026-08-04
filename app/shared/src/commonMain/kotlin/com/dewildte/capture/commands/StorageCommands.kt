package com.dewildte.capture.commands

data object SelectAiStorageFolder : Command
data object LoadConversationsFromStorage : Command
data class SaveConversationToStorage(val conversation: com.dewildte.capture.data.Conversation) : Command
data class DeleteConversationFromStorage(val conversationId: String) : Command
