package com.dewildte.capture

import com.dewildte.capture.commands.SetContext
import com.dewildte.capture.commands.Start
import com.dewildte.capture.data.TextFile
import com.dewildte.capture.events.FailedToLoadSelectedFile
import com.dewildte.capture.events.FileSelected
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InitialStateImplTest {

    @Test
    fun start_with_workspace_transitions_to_file_list() {
        val appContext = AppContextImpl()
        appContext.workspaceFolderUri = "content://test"
        
        val initialState = InitialStateImpl()
        initialState.tell(SetContext(appContext))
        initialState.tell(Start)
        
        assertTrue(appContext.state is FileListStateImpl)
    }

    @Test
    fun file_selected_transitions_to_editor() {
        val appContext = AppContextImpl()
        val initialState = InitialStateImpl()
        initialState.tell(SetContext(appContext))
        
        val textFile = TextFile(name = "test.txt", contents = "hello", path = "path")
        initialState.tell(FileSelected(textFile))
        
        assertTrue(appContext.state is EditorStateImpl)
        assertEquals("test.txt", appContext.editorState?.title)
    }

    @Test
    fun failed_to_load_transitions_to_empty_state() {
        val appContext = AppContextImpl()
        val initialState = InitialStateImpl()
        initialState.tell(SetContext(appContext))
        
        initialState.tell(FailedToLoadSelectedFile(Throwable("error")))
        
        assertTrue(appContext.state is EmptyStateImpl)
    }
}
