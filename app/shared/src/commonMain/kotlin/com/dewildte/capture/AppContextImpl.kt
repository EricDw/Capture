package com.dewildte.capture

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.dewildte.capture.commands.ClearActivePermissionRequest
import com.dewildte.capture.commands.Command
import com.dewildte.capture.commands.LoadFileFromUri
import com.dewildte.capture.commands.SetContext
import com.dewildte.capture.commands.Start
import com.dewildte.capture.commands.TransitionToState
import com.dewildte.capture.data.FileNode
import com.dewildte.capture.data.ModelInfo
import com.dewildte.capture.events.*
import com.dewildte.capture.queries.Query
import com.dewildte.capture.queries.GetCurrentDateTimeString
import com.dewildte.capture.utils.Actor
import com.dewildte.capture.utils.tellDebugLog

@Stable
class AppContextImpl(
    error: Throwable? = null,
    showLoading: Boolean = true,
    state: AppState = InitialStateImpl(),
    controller: Actor = {},
) : MutableAppContext, Actor {

    override var showLoading: Boolean by mutableStateOf(showLoading)
    override var backNavigationEnabled: Boolean by mutableStateOf(false)
    override var error: Throwable? by mutableStateOf(error)
    override var state: AppState by mutableStateOf(state)
    override var stateStack: MutableList<AppState> = mutableStateListOf(state)
    private var controller: Actor = controller

    private var _editorState: EditorState? by mutableStateOf(null)
    override var editorState: EditorState?
        get() {
            val current = _editorState
            if (current != null) return current
            val new = EditorStateImpl()
            new.tell(SetContext(this))
            _editorState = new
            return new
        }
        set(value) {
            _editorState = value
        }

    private var _aiState: AiState? by mutableStateOf(null)
    override var aiState: AiState?
        get() {
            val current = _aiState
            if (current != null) return current
            val new = AiStateImpl()
            new.tell(SetContext(this))
            _aiState = new
            return new
        }
        set(value) {
            _aiState = value
        }

    private var _fileListState: FileListState? by mutableStateOf(null)
    override var fileListState: FileListState?
        get() {
            val current = _fileListState
            if (current != null) return current
            val new = FileListStateImpl()
            new.tell(SetContext(this))
            _fileListState = new
            return new
        }
        set(value) {
            _fileListState = value
        }

    private var _settingsState: SettingsState? by mutableStateOf(null)
    override var settingsState: SettingsState?
        get() {
            val current = _settingsState
            if (current != null) return current
            val new = SettingsStateImpl()
            new.tell(SetContext(this))
            _settingsState = new
            return new
        }
        set(value) {
            _settingsState = value
        }

    override var isAiModelLoading: Boolean by mutableStateOf(false)
    override var isAiModelReady: Boolean by mutableStateOf(false)
    override var selectedAiModelName: String? by mutableStateOf(null)
    override var availableAiModels: MutableList<ModelInfo> = mutableStateListOf()
    override var aiModelError: String? by mutableStateOf(null)

    override var mcpServers: MutableList<String> = mutableStateListOf()
    override var searchToolEnabled: Boolean by mutableStateOf(true)

    override var isGoogleAuthenticated: Boolean by mutableStateOf(false)
    override var googleUserEmail: String? by mutableStateOf(null)

    override var googleDriveEnabled: Boolean by mutableStateOf(true)
    override var googleCalendarEnabled: Boolean by mutableStateOf(true)
    override var googleGmailEnabled: Boolean by mutableStateOf(true)
    override var googleTasksEnabled: Boolean by mutableStateOf(true)
    override var googleDocsEnabled: Boolean by mutableStateOf(true)
    override var googleSheetsEnabled: Boolean by mutableStateOf(true)
    override var googleSlidesEnabled: Boolean by mutableStateOf(true)

    override var googleClientId: String? by mutableStateOf(null)

    override var activePermissionRequest: AiToolCallPermissionRequest? by mutableStateOf(null)
    override var activeToolName: String? by mutableStateOf(null)
    override var tokenUsage: Int? by mutableStateOf(null)

    override var workspaceNodes: MutableList<FileNode> = mutableStateListOf()
    override var workspaceFolderUri: String? by mutableStateOf(null)
    override var isWorkspaceLoading: Boolean by mutableStateOf(false)
    override var isDrawerOpen: Boolean by mutableStateOf(false)
    override val expandedFolders: MutableMap<String, Boolean> = mutableStateMapOf()
    override var isAiAssistantVisible: Boolean by mutableStateOf(false)

    override fun tell(message: Any) {
        val currentState = state
        when (message) {
            is Event -> {
                handleEvent(message)
            }
            is Command -> {
                handleCommand(message)
            }
            is Query -> {
                handleQuery(message)
            }
        }
        if (message !is Command && message !is Query) {
            currentState.tell(message)
        }
    }

    private fun handleEvent(event: Event) {
        when (event) {
            is ModelInitializationStarted -> {
                isAiModelLoading = true
                isAiModelReady = false
                aiModelError = null
            }
            is ModelInitializationSuccess -> {
                isAiModelLoading = false
                isAiModelReady = true
                selectedAiModelName = event.name
                aiModelError = null
            }
            is AvailableModelsLoaded -> {
                availableAiModels.clear()
                availableAiModels.addAll(event.models)
            }
            is AiResponseChunk -> {
                if (event.tokenUsage != null) {
                    tokenUsage = event.tokenUsage
                }
            }
            is ModelInitializationFailed -> {
                isAiModelLoading = false
                isAiModelReady = false
                aiModelError = event.error
            }
            is AiToolCallStarted -> {
                activeToolName = event.toolName
            }
            is AiToolCallFinished -> {
                activeToolName = null
            }
            is AiToolCallPermissionRequest -> {
                activePermissionRequest = event
            }
            is WorkspaceEvent -> {
                handleWorkspaceEvent(event)
            }
            is SetDrawerOpen -> {
                isDrawerOpen = event.isOpen
            }
            is EditorTabClicked -> {
                stateStack.clear()
                tell(TransitionToState(fileListState!!))
            }
            is NewFileClicked -> {
                controller.tellDebugLog(TAG, "NewFileClicked event received")
                handleNewFileClicked()
            }
            is GoBack, is SystemBackButtonClicked -> {
                handleBackNavigation()
            }
            is ToggleAiAssistant -> {
                isAiAssistantVisible = !isAiAssistantVisible
            }
            else -> {
                controller.tell(event)
            }
        }
    }

    private fun handleBackNavigation() {
        if (stateStack.size > 1) {
            stateStack.removeAt(stateStack.size - 1)
            val prevState = stateStack.last()
            
            // Re-apply the state
            this.state = prevState
            prevState.tell(SetContext(this))
            prevState.tell(Start)
            
            backNavigationEnabled = stateStack.size > 1
        }
    }

    private fun handleCommand(command: Command) {
        when (command) {
            is Start -> {
                state.tell(SetContext(this))
                state.tell(Start)
            }
            is TransitionToState -> {
                controller.tellDebugLog(
                    tag = TAG,
                    message = "$command"
                )
                if (this.state != command.newState) {
                    this.state = command.newState
                    if (stateStack.lastOrNull() != command.newState) {
                        stateStack.add(command.newState)
                    }
                    command.newState.tell(SetContext(this))
                    command.newState.tell(Start)
                    
                    backNavigationEnabled = stateStack.size > 1
                }
            }
            is ClearActivePermissionRequest -> {
                activePermissionRequest = null
            }
            else -> {
                state.tell(command)
                controller.tell(command)
            }
        }
    }

    private fun handleQuery(query: Query) {
        state.tell(query)
        controller.tell(query)
    }

    private fun handleNewFileClicked() {
        val rootUri = workspaceFolderUri ?: run {
            controller.tellDebugLog(TAG, "handleNewFileClicked: No workspace folder selected")
            return
        }
        
        controller.tellDebugLog(TAG, "handleNewFileClicked: Requesting timestamp")
        tell(GetCurrentDateTimeString { timestamp ->
            val filename = "Note_$timestamp.txt"
            controller.tellDebugLog(TAG, "handleNewFileClicked: Creating file $filename")
            controller.tell(CreateFileRequested(rootUri, filename) { newUri ->
                controller.tellDebugLog(TAG, "handleNewFileClicked: File created at $newUri")
                controller.tell(LoadFileFromUri(newUri))
                
                val editor = editorState!!
                editor.tell(SetIsNewFile(true))
                
                tell(TransitionToState(editor))
            })
        })
    }

    private fun handleWorkspaceEvent(event: WorkspaceEvent) {
        when (event) {
            is ToggleDrawerClicked -> {
                isDrawerOpen = !isDrawerOpen
            }
            is WorkspaceFolderSelected -> {
                workspaceFolderUri = event.uri
                isWorkspaceLoading = true
                expandedFolders.clear()
                controller.tell(event)
                // If we are in Initial or Empty state, transition to File List
                if (state is InitialState || state is EmptyState) {
                    tell(TransitionToState(fileListState!!))
                }
            }
            is WorkspaceNodesLoaded -> {
                workspaceNodes.clear()
                workspaceNodes.addAll(event.nodes)
                isWorkspaceLoading = false
            }
            is FileInDrawerClicked -> {
                isDrawerOpen = false
                controller.tell(LoadFileFromUri(event.file.path))
                tell(TransitionToState(editorState!!))
            }
            is SelectWorkspaceFolderRequested -> {
                controller.tell(event)
            }
            is RefreshWorkspaceRequested -> {
                isWorkspaceLoading = true
                controller.tell(event)
            }
            is CreateFileRequested,
            is CreateFolderRequested,
            is RenameNodeRequested,
            is DeleteNodeRequested -> {
                controller.tell(event)
            }
            is WorkspaceFileOperationSuccess -> {
                tell(RefreshWorkspaceRequested)
            }
            is SetDrawerOpen -> {
                isDrawerOpen = event.isOpen
            }
        }
    }

    companion object {
        private const val TAG = "AppContext"
    }

}

@Composable
fun rememberAppContext(
    error: Throwable? = null,
    state: AppState = InitialStateImpl(),
    controller: Actor = {}
): AppContextImpl {
    return remember {
        AppContextImpl(
            error = error,
            state = state,
            controller = controller,
        )
    }
}
