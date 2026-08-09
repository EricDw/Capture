package com.dewildte.capture

import androidx.compose.runtime.Stable
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

interface EditorState : AppState {

    val textFile: TextFile
    val searchMode: Boolean

    val searchTerm: String

    val moreMenuExpanded: Boolean

    val snippetSelectorExpanded: Boolean
    val snippetSelectorIsEmpty: Boolean
    val snippetToInsert: String?

    val snippets: List<String>
}

interface SettingsState : AppState {
    val snippets: List<String>
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
