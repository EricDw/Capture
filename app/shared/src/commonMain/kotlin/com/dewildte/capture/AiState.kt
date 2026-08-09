package com.dewildte.capture

import com.dewildte.capture.data.Conversation

interface AiState : AppState {
    val conversations: List<Conversation>
    val currentConversation: Conversation?
    val currentMessage: String
    val isGenerating: Boolean
    val selectedModelName: String?
    val availableModels: List<com.dewildte.capture.data.ModelInfo>
    val isModelLoading: Boolean
    val isModelReady: Boolean
    val error: String?
    val activeToolName: String?
    val tokenUsage: Int?
}
