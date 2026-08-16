package com.dewildte.capture

import com.dewildte.capture.commands.SetContext
import com.dewildte.capture.commands.UpdateSelectedFileContent
import com.dewildte.capture.data.TextFile
import com.dewildte.capture.events.DeleteNodeRequested
import com.dewildte.capture.events.FileTextChanged
import com.dewildte.capture.events.TitleChanged
import com.dewildte.capture.events.SystemBackButtonClicked
import com.dewildte.capture.events.FileSelected
import com.dewildte.capture.events.SnippetsFileLoaded
import com.dewildte.capture.events.SetIsNewFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EditorStateImplTest {

    @Test
    fun initial_state_is_correct() {
        val textFile = TextFile(name = "test.txt", contents = "hello")
        val editorState = EditorStateImpl(textFile = textFile)
        
        assertEquals("test.txt", editorState.title)
        assertEquals("hello", editorState.contents)
        assertFalse(editorState.isNewFile)
    }

    @Test
    fun text_change_updates_contents() {
        val editorState = EditorStateImpl()
        editorState.tell(FileTextChanged("new content"))
        assertEquals("new content", editorState.contents)
    }

    @Test
    fun title_change_updates_title() {
        val editorState = EditorStateImpl()
        editorState.tell(TitleChanged("new title"))
        assertEquals("new title", editorState.title)
    }

    @Test
    fun snippets_loading_updates_snippets_list() {
        val editorState = EditorStateImpl()
        val snippetsContent = "Snippet 1\n\n\nSnippet 2"
        val snippetsFile = TextFile(name = "snippets.txt", contents = snippetsContent)
        
        editorState.tell(SnippetsFileLoaded(snippetsFile))
        
        assertEquals(2, editorState.snippets.size)
        assertEquals("Snippet 1", editorState.snippets[0])
        assertEquals("Snippet 2", editorState.snippets[1])
        assertFalse(editorState.snippetSelectorIsEmpty)
    }

    @Test
    fun back_navigation_on_empty_new_file_triggers_delete() {
        var deleteRequested = false
        val controller: (Any) -> Unit = { message ->
            if (message is DeleteNodeRequested) {
                deleteRequested = true
            }
        }
        
        val appContext = AppContextImpl(controller = controller)
        val editorState = EditorStateImpl(textFile = TextFile(name = "Note_123.txt", contents = ""), isNewFile = true)
        editorState.tell(SetContext(appContext))
        
        editorState.tell(SystemBackButtonClicked)
        
        assertTrue(deleteRequested, "Delete should be requested for empty new note")
    }

    @Test
    fun back_navigation_on_modified_file_triggers_save() {
        var saveRequested = false
        val controller: (Any) -> Unit = { message ->
            if (message is UpdateSelectedFileContent) {
                saveRequested = true
            }
        }
        
        val appContext = AppContextImpl(controller = controller)
        val editorState = EditorStateImpl(textFile = TextFile(name = "test.txt", contents = "original"))
        editorState.tell(SetContext(appContext))
        
        editorState.tell(FileTextChanged("modified"))
        editorState.tell(SystemBackButtonClicked)
        
        assertTrue(saveRequested, "Save should be requested on back navigation")
    }
}
