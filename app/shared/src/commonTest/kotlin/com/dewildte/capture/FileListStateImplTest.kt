package com.dewildte.capture

import com.dewildte.capture.commands.SetContext
import com.dewildte.capture.commands.Start
import com.dewildte.capture.data.FileNode
import com.dewildte.capture.events.RefreshWorkspaceRequested
import com.dewildte.capture.events.WorkspaceNodesLoaded
import com.dewildte.capture.events.FileInDrawerClicked
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FileListStateImplTest {

    @Test
    fun start_triggers_refresh() {
        var refreshRequested = false
        val controller: (Any) -> Unit = { message ->
            if (message is RefreshWorkspaceRequested) {
                refreshRequested = true
            }
        }
        
        val appContext = AppContextImpl(controller = controller)
        val fileListState = FileListStateImpl()
        fileListState.tell(SetContext(appContext))
        fileListState.tell(Start)
        
        assertTrue(refreshRequested)
        assertTrue(fileListState.isRefreshing)
    }

    @Test
    fun nodes_loaded_filters_supported_files() {
        val fileListState = FileListStateImpl()
        val nodes = listOf(
            FileNode.File("note1.txt", "path1"),
            FileNode.File("readme.md", "path2"),
            FileNode.File("data.json", "path3"),
            FileNode.File("log.log", "path4"),
            FileNode.File("model.litertlm", "path5"),
            FileNode.File("image.png", "path6"),
            FileNode.Folder("docs", "path7")
        )
        
        fileListState.tell(WorkspaceNodesLoaded(nodes))
        
        assertEquals(5, fileListState.files.size)
        assertTrue(fileListState.files.any { it.name == "note1.txt" })
        assertTrue(fileListState.files.any { it.name == "readme.md" })
        assertTrue(fileListState.files.any { it.name == "data.json" })
        assertTrue(fileListState.files.any { it.name == "log.log" })
        assertTrue(fileListState.files.any { it.name == "model.litertlm" })
        assertFalse(fileListState.files.any { it.name == "image.png" })
        assertFalse(fileListState.isRefreshing)
    }

    @Test
    fun file_clicked_transitions_to_editor() {
        val appContext = AppContextImpl()
        val fileListState = FileListStateImpl()
        fileListState.tell(SetContext(appContext))
        
        val file = FileNode.File("test.txt", "path")
        fileListState.tell(FileInDrawerClicked(file))
        
        assertTrue(appContext.state is EditorStateImpl)
    }
}
