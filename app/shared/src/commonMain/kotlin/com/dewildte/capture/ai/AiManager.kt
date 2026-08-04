package com.dewildte.capture.ai

import com.dewildte.capture.data.Conversation
import com.dewildte.capture.data.ModelInfo

interface AiManager {
    fun initializeModel(uri: String)
    fun sendMessage(message: String, conversation: Conversation)
    fun stopGeneration()
    fun close()
}
