package com.dewildte.capture.data

data class Message(
    val id: String,
    val role: MessageRole,
    val content: String,
    val timestamp: Long,
    val thinking: String? = null,
    val toolCall: String? = null,
    val isToolError: Boolean = false,
)

enum class MessageRole { USER, AI }
