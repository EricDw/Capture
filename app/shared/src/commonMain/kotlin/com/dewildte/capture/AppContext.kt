package com.dewildte.capture

import com.dewildte.capture.data.TextFile
import com.dewildte.capture.utils.Actor

interface AppContext : Actor {
    val showLoading: Boolean
    val backNavigationEnabled: Boolean
    val error: Throwable?
    val state: AppState
    val stateStack: List<AppState>

    val editorState: EditorState?
    val aiState: AiState?

    val isAiModelLoading: Boolean
    val isAiModelReady: Boolean
    val selectedAiModelName: String?
    val availableAiModels: List<com.dewildte.capture.data.ModelInfo>
    val aiModelError: String?

    val controller: Actor

    override fun tell(message: Any) {
        /* no-op */
    }
}

interface MutableAppContext: AppContext {
    override var showLoading: Boolean
    override var backNavigationEnabled: Boolean
    override var error: Throwable?
    override var state: AppState
    override var controller: Actor
    override val stateStack: MutableList<AppState>

    override var editorState: EditorState?
    override var aiState: AiState?

    override var isAiModelLoading: Boolean
    override var isAiModelReady: Boolean
    override var selectedAiModelName: String?
    override var availableAiModels: MutableList<com.dewildte.capture.data.ModelInfo>
    override var aiModelError: String?
}