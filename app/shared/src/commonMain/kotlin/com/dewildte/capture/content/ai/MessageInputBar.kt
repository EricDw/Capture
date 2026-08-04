package com.dewildte.capture.content.ai

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MessageInputBar(
    textFieldState: TextFieldState,
    isGenerating: Boolean,
    isModelReady: Boolean,
    onSendClick: () -> Unit,
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            state = textFieldState,
            modifier = Modifier.weight(1f),
            enabled = isModelReady && !isGenerating,
            placeholder = { 
                androidx.compose.material3.Text(
                    if (isModelReady) "Type a message..." else "AI is initializing..."
                ) 
            },
        )

        if (isGenerating) {
            IconButton(onClick = onStopClick) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Stop generating",
                )
            }
        } else {
            IconButton(
                onClick = onSendClick,
                enabled = isModelReady && textFieldState.text.isNotBlank(),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send message",
                )
            }
        }
    }
}
