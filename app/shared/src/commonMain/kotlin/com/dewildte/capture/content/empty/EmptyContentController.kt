package com.dewildte.capture.content.empty

import androidx.compose.runtime.*
import com.dewildte.capture.EmptyState

@Composable
fun EmptyContentController(
    state: EmptyState,
) {
    EmptyContent(onEvent = state::tell)
}