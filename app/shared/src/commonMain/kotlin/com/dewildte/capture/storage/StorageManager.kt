package com.dewildte.capture.storage

import com.dewildte.capture.data.Conversation
import com.dewildte.capture.data.FileNode

interface StorageManager {
    fun loadConversations(folderUri: String)
    fun saveConversation(folderUri: String, conversation: Conversation)
    fun deleteConversation(folderUri: String, conversationId: String)
    fun readTextFile(uri: String): String?
    fun writeTextFile(uri: String, content: String)
    fun searchFiles(folderUri: String, query: String): List<String>
    fun listWorkspaceFiles(folderUri: String)
    fun createFile(parentUri: String, fileName: String): String?
    fun createDirectory(parentUri: String, folderName: String): String?
    fun deleteNode(uri: String): Boolean
    fun renameNode(uri: String, newName: String): String?
}
