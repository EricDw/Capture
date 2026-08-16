package com.dewildte.capture

import com.dewildte.capture.data.FileNode
import com.dewildte.capture.events.AiToolCallPermissionRequest
import com.dewildte.capture.utils.Actor

/**
 * The read-only view of the application context, intended for use in the UI layer.
 *
 * Casting from [AppContext] to [MutableAppContext] in the UI is strictly prohibited to
 * maintain clear separation between state consumption and state modification.
 */
interface AppContext : Actor {
    val showLoading: Boolean
    val backNavigationEnabled: Boolean
    val error: Throwable?
    val state: AppState
    val stateStack: List<AppState>

    val editorState: EditorState?
    val aiState: AiState?
    val fileListState: FileListState?
    val settingsState: SettingsState?

    val isAiModelLoading: Boolean
    val isAiModelReady: Boolean
    val selectedAiModelName: String?
    val availableAiModels: List<com.dewildte.capture.data.ModelInfo>
    val aiModelError: String?

    val mcpServers: List<String>
    val searchToolEnabled: Boolean

    val isGoogleAuthenticated: Boolean
    val googleUserEmail: String?

    val googleDriveEnabled: Boolean
    val googleCalendarEnabled: Boolean
    val googleGmailEnabled: Boolean
    val googleTasksEnabled: Boolean
    val googleDocsEnabled: Boolean
    val googleSheetsEnabled: Boolean
    val googleSlidesEnabled: Boolean

    val googleClientId: String?

    val activePermissionRequest: AiToolCallPermissionRequest?
    val activeToolName: String?
    val tokenUsage: Int?

    val workspaceNodes: List<FileNode>
    val workspaceFolderUri: String?
    val isWorkspaceLoading: Boolean
    val isDrawerOpen: Boolean
    val expandedFolders: Map<String, Boolean>
    val isAiAssistantVisible: Boolean

    override fun tell(message: Any) {
        /* no-op */
    }
}

/**
 * The mutable view of the application context, intended for use within state implementations.
 */
interface MutableAppContext: AppContext {
    override var showLoading: Boolean
    override var backNavigationEnabled: Boolean
    override var error: Throwable?
    override var state: AppState
    /**
     * The history of application states. Popping from this list navigates back.
     */
    override val stateStack: MutableList<AppState>

    override var editorState: EditorState?
    override var aiState: AiState?
    override var fileListState: FileListState?
    override var settingsState: SettingsState?

    override var isAiModelLoading: Boolean
    override var isAiModelReady: Boolean
    override var selectedAiModelName: String?
    override var availableAiModels: MutableList<com.dewildte.capture.data.ModelInfo>
    override var aiModelError: String?

    override var mcpServers: MutableList<String>
    override var searchToolEnabled: Boolean

    override var isGoogleAuthenticated: Boolean
    override var googleUserEmail: String?

    override var googleDriveEnabled: Boolean
    override var googleCalendarEnabled: Boolean
    override var googleGmailEnabled: Boolean
    override var googleTasksEnabled: Boolean
    override var googleDocsEnabled: Boolean
    override var googleSheetsEnabled: Boolean
    override var googleSlidesEnabled: Boolean

    override var googleClientId: String?

    override var activePermissionRequest: AiToolCallPermissionRequest?
    override var activeToolName: String?
    override var tokenUsage: Int?

    override var workspaceNodes: MutableList<FileNode>
    override var workspaceFolderUri: String?
    override var isWorkspaceLoading: Boolean
    override var isDrawerOpen: Boolean
    override val expandedFolders: MutableMap<String, Boolean>
    override var isAiAssistantVisible: Boolean
}
