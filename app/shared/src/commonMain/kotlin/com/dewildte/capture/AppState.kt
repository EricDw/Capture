package com.dewildte.capture

import androidx.compose.runtime.Stable
import com.dewildte.capture.data.FileNode
import com.dewildte.capture.data.TextFile
import com.dewildte.capture.utils.Actor

@Stable
sealed interface AppState: Actor {
    override fun tell(message: Any) {
        /* no-op */
    }
}

interface InitialState : AppState

interface EmptyState : AppState

interface FileListState : AppState {
    val files: List<FileNode.File>
    val isRefreshing: Boolean
}

interface EditorState : AppState {

    val textFile: TextFile
    val title: String
    val contents: String
    val isNewFile: Boolean

    val searchMode: Boolean

    val searchTerm: String

    val moreMenuExpanded: Boolean

    val snippetSelectorExpanded: Boolean
    val snippetSelectorIsEmpty: Boolean
    val snippetToInsert: String?

    val snippets: List<String>
}

interface SettingsState : AppState {
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
}
