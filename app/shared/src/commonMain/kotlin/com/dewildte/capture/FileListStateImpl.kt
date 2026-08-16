package com.dewildte.capture

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dewildte.capture.commands.SetContext
import com.dewildte.capture.commands.Start
import com.dewildte.capture.commands.TransitionToState
import com.dewildte.capture.data.FileNode
import com.dewildte.capture.events.FileInDrawerClicked
import com.dewildte.capture.events.RefreshWorkspaceRequested
import com.dewildte.capture.events.WorkspaceNodesLoaded

@Stable
class FileListStateImpl : FileListState {

    private lateinit var context: MutableAppContext
    private var previousState: AppState? = null

    override var files: List<FileNode.File> by mutableStateOf(emptyList())
    override var isRefreshing: Boolean by mutableStateOf(false)

    override fun tell(message: Any) {
        when (message) {
            is SetContext -> {
                context = message.context
                if (context.state != this) {
                    previousState = context.state
                }
            }

            is Start -> {
                context.apply {
                    state = this@FileListStateImpl
                    showLoading = false
                }
                refreshFiles()
            }

            is WorkspaceNodesLoaded -> {
                isRefreshing = false
                // Filter for .txt files in the root for the list view
                files = message.nodes.filterIsInstance<FileNode.File>()
                    .filter { it.extension == "txt" }
            }

            is FileInDrawerClicked -> {
                // Clicking a file in the grid or drawer
                val editorState = context.editorState!!
                // We'll let AppContext handle the LoadFileFromUri via TransitionToState or similar
                // But for now, let's just transition.
                context.tell(TransitionToState(editorState))
                // Note: The actual loading of file content is usually triggered by LoadFileFromUri
                // which is sent by AppContextImpl when FileInDrawerClicked is received.
            }
        }
    }

    private fun refreshFiles() {
        isRefreshing = true
        context.tell(RefreshWorkspaceRequested)
    }

    companion object {
        private const val TAG = "FileListState"
    }
}
