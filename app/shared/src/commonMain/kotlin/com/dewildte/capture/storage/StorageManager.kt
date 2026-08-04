package com.dewildte.capture.storage

import com.dewildte.capture.data.Conversation

interface StorageManager {
    fun loadConversations(folderUri: String)
    fun saveConversation(folderUri: String, conversation: Conversation)
    fun deleteConversation(folderUri: String, conversationId: String)
    fun readTextFile(uri: String): String?
    fun writeTextFile(uri: String, content: String)
}
