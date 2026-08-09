package com.dewildte.capture.ai

import com.dewildte.capture.data.Conversation

interface AiManager {
    fun initializeModel(uri: String)
    fun registerTool(tool: Any)
    fun sendMessage(message: String, conversation: Conversation, systemInstruction: String? = null)
    fun stopGeneration()
    fun close()
}
