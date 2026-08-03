package com.dewildte.capture.content.editor

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dewildte.capture.EditorState

@Composable
fun EditorContentController(
    state: EditorState,
) {
    with(state) {
        EditorContent(
            modifier = Modifier.fillMaxSize(),
            textFile = textFile,
            searchMode = searchMode,
            searchTerm = searchTerm,
            onEvent = state::tell
        )
    }
}