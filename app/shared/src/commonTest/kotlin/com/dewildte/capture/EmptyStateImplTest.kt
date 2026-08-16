package com.dewildte.capture

import com.dewildte.capture.commands.SelectTextFile
import com.dewildte.capture.commands.SetContext
import com.dewildte.capture.commands.Start
import com.dewildte.capture.content.empty.SelectTextFileClicked
import com.dewildte.capture.data.TextFile
import com.dewildte.capture.events.AiTabClicked
import com.dewildte.capture.events.FileSelected
import com.dewildte.capture.events.MenuTabClicked
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EmptyStateImplTest {

    @Test
    fun start_updates_context_state() {
        val appContext = AppContextImpl(showLoading = true)
        val emptyState = EmptyStateImpl()
        emptyState.tell(SetContext(appContext))
        emptyState.tell(Start)

        assertFalse(appContext.showLoading)
        assertEquals(emptyState, appContext.state)
    }

    @Test
    fun select_text_file_clicked_triggers_command() {
        var commandTriggered = false
        val controller: (Any) -> Unit = { message ->
            if (message is SelectTextFile) {
                commandTriggered = true
            }
        }
        val appContext = AppContextImpl(controller = controller)
        val emptyState = EmptyStateImpl()
        emptyState.tell(SetContext(appContext))

        emptyState.tell(SelectTextFileClicked)

        assertTrue(commandTriggered)
    }

    @Test
    fun file_selected_transitions_to_editor() {
        val appContext = AppContextImpl()
        val emptyState = EmptyStateImpl()
        emptyState.tell(SetContext(appContext))

        val textFile = TextFile(name = "test.txt", contents = "content")
        emptyState.tell(FileSelected(textFile))

        assertTrue(appContext.state is EditorStateImpl)
        assertEquals("test.txt", appContext.editorState?.title)
        assertEquals("content", appContext.editorState?.contents)
    }

    @Test
    fun ai_tab_clicked_transitions_to_ai_state() {
        val appContext = AppContextImpl()
        val emptyState = EmptyStateImpl()
        emptyState.tell(SetContext(appContext))

        emptyState.tell(AiTabClicked)

        assertTrue(appContext.state is AiStateImpl)
    }

    @Test
    fun menu_tab_clicked_triggers_select_text_file() {
        var commandTriggered = false
        val controller: (Any) -> Unit = { message ->
            if (message is SelectTextFile) {
                commandTriggered = true
            }
        }
        val appContext = AppContextImpl(controller = controller)
        val emptyState = EmptyStateImpl()
        emptyState.tell(SetContext(appContext))

        emptyState.tell(MenuTabClicked)

        assertTrue(commandTriggered)
    }
}
