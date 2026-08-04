package com.dewildte.capture.content.ai

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dewildte.capture.AiState

@Composable
fun AiContent(
    state: AiState,
    modifier: Modifier = Modifier,
) {
    val currentConversation = state.currentConversation

    Column(modifier = modifier.fillMaxSize()) {
        if (state.isModelLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        if (currentConversation == null) {
            ConversationListContent(
                conversations = state.conversations,
                isModelReady = state.isModelReady,
                onEvent = state::tell,
                modifier = Modifier.weight(1f),
            )
        } else {
            ChatContent(
                conversation = currentConversation,
                currentMessage = state.currentMessage,
                isGenerating = state.isGenerating,
                isModelReady = state.isModelReady,
                onEvent = state::tell,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
