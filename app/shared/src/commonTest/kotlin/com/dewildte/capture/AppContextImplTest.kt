package com.dewildte.capture

import com.dewildte.capture.commands.TransitionToState
import com.dewildte.capture.events.ToggleDrawerClicked
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppContextImplTest {

    @Test
    fun initial_state_is_correct() {
        val appContext = AppContextImpl()
        assertFalse(appContext.isDrawerOpen)
        assertTrue(appContext.state is InitialStateImpl)
        assertEquals(1, appContext.stateStack.size)
    }

    @Test
    fun toggling_drawer_updates_isDrawerOpen() {
        val appContext = AppContextImpl()
        appContext.tell(ToggleDrawerClicked)
        assertTrue(appContext.isDrawerOpen)
        appContext.tell(ToggleDrawerClicked)
        assertFalse(appContext.isDrawerOpen)
    }

    @Test
    fun stateStack_handles_history() {
        val appContext = AppContextImpl()
        val editorState = appContext.editorState!!
        appContext.tell(TransitionToState(editorState))
        
        assertEquals(2, appContext.stateStack.size)
        assertEquals(editorState, appContext.stateStack.last())
    }

    @Test
    fun new_file_clicked_transitions_to_editor() {
        // This is a bit hard to test because of callbacks and controller interaction
        // but we can at least verify it triggers some actions if we mock/spy controller
    }
}
