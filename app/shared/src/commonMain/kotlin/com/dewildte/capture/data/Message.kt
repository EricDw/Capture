package com.dewildte.capture.data

data class Message(
    val id: String,
    val role: MessageRole,
    val content: String,
    val timestamp: Long,
)

enum class MessageRole { USER, AI }
