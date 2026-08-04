package com.dewildte.capture.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.dewildte.capture.AiState
import com.dewildte.capture.data.Conversation
import com.dewildte.capture.events.BackToConversationsClicked
import com.dewildte.capture.events.SelectModelClicked
import com.dewildte.capture.events.SelectStorageFolderClicked

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiTopBar(
    currentConversation: Conversation?,
    selectedModelName: String?,
    isModelLoading: Boolean,
    error: String?,
    onBackClick: () -> Unit = {},
    onSelectFolderClick: () -> Unit = {},
    onSelectModelClick: () -> Unit = {},
) {
    TopAppBar(
        title = {
            androidx.compose.foundation.layout.Column {
                Text(
                    text = currentConversation?.title ?: "AI"
                )
                val subtitle = when {
                    isModelLoading -> "Loading model..."
                    error != null -> "Error: $error"
                    else -> selectedModelName ?: "No model selected"
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        error != null -> MaterialTheme.colorScheme.error
                        selectedModelName == null && !isModelLoading -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        },
        navigationIcon = {
            if (currentConversation != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to conversations",
                    )
                }
            }
        },
        actions = {
            if (currentConversation == null) {
                IconButton(onClick = onSelectModelClick) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "Select AI model",
                    )
                }
                IconButton(onClick = onSelectFolderClick) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "Select storage folder",
                    )
                }
            }
        }
    )
}

@Composable
fun AiTopBar(state: AiState) {
    AiTopBar(
        currentConversation = state.currentConversation,
        selectedModelName = state.selectedModelName,
        isModelLoading = state.isModelLoading,
        error = state.error,
        onBackClick = { state.tell(BackToConversationsClicked) },
        onSelectFolderClick = { state.tell(SelectStorageFolderClicked) },
        onSelectModelClick = { state.tell(SelectModelClicked) },
    )
}

@Preview
@Composable
private fun AiTopBarListPreview() {
    AiTopBar(
        currentConversation = null,
        selectedModelName = "Gemma 2B",
        isModelLoading = false,
        error = null
    )
}

@Preview
@Composable
private fun AiTopBarNoModelPreview() {
    AiTopBar(
        currentConversation = null,
        selectedModelName = null,
        isModelLoading = false,
        error = null
    )
}

@Preview
@Composable
private fun AiTopBarLoadingPreview() {
    AiTopBar(
        currentConversation = null,
        selectedModelName = "Gemma 2B",
        isModelLoading = true,
        error = null
    )
}

@Preview
@Composable
private fun AiTopBarChatPreview() {
    AiTopBar(
        currentConversation = Conversation(
            id = "1",
            title = "Trip Planning",
            messages = emptyList(),
            createdAt = 0,
            updatedAt = 0,
        ),
        selectedModelName = "Gemma 2B",
        isModelLoading = false,
        error = null
    )
}
