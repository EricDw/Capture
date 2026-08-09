package com.dewildte.capture.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dewildte.capture.AiState
import com.dewildte.capture.data.Conversation
import com.dewildte.capture.data.ModelInfo
import com.dewildte.capture.events.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiTopBar(
    currentConversation: Conversation?,
    selectedModelName: String?,
    availableModels: List<ModelInfo>,
    isModelLoading: Boolean,
    error: String?,
    activeToolName: String? = null,
    tokenUsage: Int? = null,
    onBackClick: () -> Unit = {},
    onSelectFolderClick: () -> Unit = {},
    onSelectModelClick: () -> Unit = {},
    onSwitchModel: (ModelInfo) -> Unit = {},
    onDeleteModel: (ModelInfo) -> Unit = {},
) {
    var showModelMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Column {
                Text(
                    text = currentConversation?.title ?: "AI"
                )
                val subtitle = when {
                    activeToolName != null -> "AI is working: $activeToolName..."
                    isModelLoading -> "Loading model..."
                    error != null -> "Error: $error"
                    else -> buildString {
                        append(selectedModelName ?: "No model selected")
                        if (tokenUsage != null) {
                            val formatted = if (tokenUsage >= 1000) {
                                "${tokenUsage / 1000}.${(tokenUsage % 1000) / 100}k"
                            } else {
                                "$tokenUsage"
                            }
                            append(" • $formatted / 8k")
                        }
                    }
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        activeToolName != null -> MaterialTheme.colorScheme.primary
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
                Box {
                    IconButton(onClick = { showModelMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Select AI model",
                        )
                    }
                    DropdownMenu(
                        expanded = showModelMenu,
                        onDismissRequest = { showModelMenu = false }
                    ) {
                        if (availableModels.isNotEmpty()) {
                            availableModels.forEach { model ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = model.name,
                                                fontWeight = if (model.name == selectedModelName) FontWeight.Bold else FontWeight.Normal,
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(onClick = { onDeleteModel(model) }) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete model",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        onSwitchModel(model)
                                        showModelMenu = false
                                    }
                                )
                            }
                            HorizontalDivider()
                        }
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Add new model...")
                                }
                            },
                            onClick = {
                                onSelectModelClick()
                                showModelMenu = false
                            }
                        )
                    }
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
        availableModels = state.availableModels,
        isModelLoading = state.isModelLoading,
        error = state.error,
        activeToolName = state.activeToolName,
        tokenUsage = state.tokenUsage,
        onBackClick = { state.tell(BackToConversationsClicked) },
        onSelectFolderClick = { state.tell(SelectStorageFolderClicked) },
        onSelectModelClick = { state.tell(SelectModelClicked) },
        onSwitchModel = { state.tell(SwitchModelClicked(it)) },
        onDeleteModel = { state.tell(DeleteModelClicked(it)) },
    )
}
