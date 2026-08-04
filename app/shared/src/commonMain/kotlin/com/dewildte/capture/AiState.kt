package com.dewildte.capture

import com.dewildte.capture.data.Conversation

interface AiState : AppState {
    val conversations: List<Conversation>
    val currentConversation: Conversation?
    val currentMessage: String
    val isGenerating: Boolean
    val selectedModelName: String?
    val isModelLoading: Boolean
    val isModelReady: Boolean
    val error: String?
}
