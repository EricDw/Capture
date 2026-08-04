package com.dewildte.capture.storage

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.dewildte.capture.data.Conversation
import com.dewildte.capture.data.Message
import com.dewildte.capture.data.MessageRole
import com.dewildte.capture.events.ConversationsLoaded
import com.dewildte.capture.utils.Actor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

class AndroidStorageManager(
    private val context: Context,
    private val scope: CoroutineScope,
    private val uiActor: Actor
) : StorageManager {

    override fun loadConversations(folderUri: String) {
        val rootUri = Uri.parse(folderUri)
        val root = DocumentFile.fromTreeUri(context, rootUri) ?: return

        scope.launch(Dispatchers.IO) {
            val conversations = mutableListOf<Conversation>()
            root.listFiles().forEach { file ->
                if (file.isFile && file.name?.endsWith(".txt") == true) {
                    try {
                        context.contentResolver.openInputStream(file.uri)?.use { inputStream ->
                            val content = inputStream.reader().readText()
                            parseConversation(file.name!!, content)?.let {
                                conversations.add(it)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse conversation file: ${file.name}", e)
                    }
                }
            }
            uiActor.tell(ConversationsLoaded(conversations.sortedByDescending { it.updatedAt }))
        }
    }

    override fun saveConversation(folderUri: String, conversation: Conversation) {
        val rootUri = Uri.parse(folderUri)
        val root = DocumentFile.fromTreeUri(context, rootUri) ?: return

        scope.launch(Dispatchers.IO) {
            val fileName = "${conversation.id}.txt"
            var file = root.findFile(fileName)
            if (file == null) {
                file = root.createFile("text/plain", fileName)
            }

            file?.let {
                context.contentResolver.openOutputStream(it.uri, "wt")?.use { outputStream ->
                    val content = StringBuilder()
                    content.append("Title: ").append(conversation.title).append("\n")
                    content.append("UpdatedAt: ").append(conversation.updatedAt).append("\n")
                    content.append("---\n")
                    conversation.messages.forEach { msg ->
                        val roleTag = if (msg.role == MessageRole.USER) "[USER]" else "[AI]"
                        content.append(roleTag).append(": ").append(msg.content).append("\n\n")
                    }
                    outputStream.write(content.toString().toByteArray())
                    outputStream.flush()
                }
            }
        }
    }

    override fun deleteConversation(folderUri: String, conversationId: String) {
        val rootUri = Uri.parse(folderUri)
        val root = DocumentFile.fromTreeUri(context, rootUri) ?: return

        scope.launch(Dispatchers.IO) {
            val fileName = "$conversationId.txt"
            root.findFile(fileName)?.delete()
        }
    }

    override fun readTextFile(uri: String): String? {
        return try {
            context.contentResolver.openInputStream(Uri.parse(uri))?.use { inputStream ->
                inputStream.reader().readText()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read text file: $uri", e)
            null
        }
    }

    override fun writeTextFile(uri: String, content: String) {
        scope.launch(Dispatchers.IO) {
            try {
                context.contentResolver.openOutputStream(Uri.parse(uri), "wt")?.use { stream ->
                    stream.write(content.toByteArray())
                    stream.flush()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to write text file: $uri", e)
            }
        }
    }

    private fun parseConversation(fileName: String, content: String): Conversation? {
        try {
            val id = fileName.removeSuffix(".txt")
            val lines = content.lines()
            if (lines.isEmpty()) return null

            var title = "Untitled"
            var updatedAt = 0L
            var messageStartLine = 0

            for (i in lines.indices) {
                val line = lines[i]
                if (line.startsWith("Title: ")) title = line.removePrefix("Title: ")
                if (line.startsWith("UpdatedAt: ")) updatedAt = line.removePrefix("UpdatedAt: ").toLongOrNull() ?: 0L
                if (line == "---") {
                    messageStartLine = i + 1
                    break
                }
            }

            val messages = mutableListOf<Message>()
            var currentRole: MessageRole? = null
            var currentContent = StringBuilder()

            for (i in messageStartLine until lines.size) {
                val line = lines[i]
                if (line.startsWith("[USER]: ")) {
                    if (currentRole != null) {
                        messages.add(Message(Uuid.random().toString(), currentRole, currentContent.toString().trim(), 0))
                    }
                    currentRole = MessageRole.USER
                    currentContent = StringBuilder(line.removePrefix("[USER]: "))
                } else if (line.startsWith("[AI]: ")) {
                    if (currentRole != null) {
                        messages.add(Message(Uuid.random().toString(), currentRole, currentContent.toString().trim(), 0))
                    }
                    currentRole = MessageRole.AI
                    currentContent = StringBuilder(line.removePrefix("[AI]: "))
                } else {
                    currentContent.append("\n").append(line)
                }
            }
            if (currentRole != null) {
                messages.add(Message(Uuid.random().toString(), currentRole, currentContent.toString().trim(), 0))
            }

            return Conversation(id, title, messages, updatedAt, updatedAt)
        } catch (e: Exception) {
            return null
        }
    }

    companion object {
        private const val TAG = "AndroidStorageManager"
    }
}
