package com.dewildte.capture.content.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.dewildte.capture.data.Conversation
import com.dewildte.capture.data.Message
import com.dewildte.capture.data.MessageRole
import com.dewildte.capture.events.*

@Composable
fun ChatContent(
    conversation: Conversation,
    currentMessage: String,
    isGenerating: Boolean,
    isModelReady: Boolean,
    onEvent: (AiContentEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(conversation.messages.size) {
        if (conversation.messages.isNotEmpty()) {
            listState.animateScrollToItem(conversation.messages.size - 1)
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = conversation.messages,
                key = { it.id }
            ) { message ->
                MessageBubble(
                    message = message,
                    onOpenFile = { onEvent(OpenFileRequested(it)) }
                )
            }

            if (isGenerating) {
                item {
                    TypingIndicator()
                }
            }
        }

        val textFieldState = rememberTextFieldState(currentMessage)

        // Sync external state changes (like clearing) to the text field
        LaunchedEffect(currentMessage) {
            if (currentMessage.isEmpty() && textFieldState.text.isNotEmpty()) {
                textFieldState.edit { 
                    replace(0, length, "")
                }
            }
        }

        // Sync text field state changes back to the state
        val textFlow = remember(textFieldState) {
            snapshotFlow { textFieldState.text }
        }

        LaunchedEffect(textFieldState) {
            textFlow.collect { newText ->
                onEvent(MessageInputChanged(newText.toString()))
            }
        }

        MessageInputBar(
            textFieldState = textFieldState,
            isGenerating = isGenerating,
            isModelReady = isModelReady,
            onSendClick = { onEvent(SendMessageClicked) },
            onStopClick = { onEvent(StopGeneratingClicked) },
        )
    }
}

@Composable
private fun MessageBubble(
    message: Message,
    onOpenFile: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val isUser = message.role == MessageRole.USER

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
    ) {
        if (!isUser && !message.thinking.isNullOrBlank()) {
            var expanded by remember { mutableStateOf(false) }
            
            Surface(
                onClick = { expanded = !expanded },
                color = Color.Transparent,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Thinking...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            if (expanded) {
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp, bottom = 8.dp, end = 32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = message.thinking,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (!isUser && message.toolCall != null) {
            val isError = message.isToolError || 
                        message.content.contains("Error calling", ignoreCase = true) || 
                        message.content.contains("401 Unauthorized", ignoreCase = true)
            
            val icon = if (isError) Icons.Default.Error else Icons.Default.Build

            AssistChip(
                onClick = {},
                label = {
                    Text(
                        text = if (isError) "Tool error: ${message.toolCall}" else "Used tool: ${message.toolCall}",
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    leadingIconContentColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
        }
        
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp,
                    )
                )
                .background(
                    if (isUser) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    }
                )
                .padding(12.dp)
        ) {
            val annotatedContent = buildAnnotatedString {
                val text = message.content
                val regex = Regex("[a-zA-Z0-9_-]+\\.txt")
                var lastIndex = 0
                regex.findAll(text).forEach { match ->
                    append(text.substring(lastIndex, match.range.first))
                    val fileName = match.value
                    val link = androidx.compose.ui.text.LinkAnnotation.Clickable(
                        tag = "FILE",
                        styles = androidx.compose.ui.text.TextLinkStyles(
                            style = SpanStyle(
                                color = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                            )
                        ),
                        linkInteractionListener = {
                            onOpenFile(fileName)
                        }
                    )
                    withLink(link) {
                        append(fileName)
                    }
                    lastIndex = match.range.last + 1
                }
                append(text.substring(lastIndex))
            }

            Text(
                text = annotatedContent,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (isUser) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    }
                )
            )
        }
    }
}

@Composable
private fun TypingIndicator(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(vertical = 4.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.Start,
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "● ● ●",
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "AI is generating...",
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

