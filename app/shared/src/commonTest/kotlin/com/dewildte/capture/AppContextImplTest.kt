package com.dewildte.capture

import com.dewildte.capture.commands.TransitionToState
import com.dewildte.capture.data.ModelInfo
import com.dewildte.capture.events.*
import com.dewildte.capture.queries.GetCurrentDateTimeString
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
        assertFalse(appContext.backNavigationEnabled)
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
        assertTrue(appContext.backNavigationEnabled)
    }

    @Test
    fun back_navigation_pops_stack() {
        val appContext = AppContextImpl()
        val editorState = appContext.editorState!!
        appContext.tell(TransitionToState(editorState))
        assertEquals(2, appContext.stateStack.size)
        
        appContext.tell(GoBack)
        assertEquals(1, appContext.stateStack.size)
        assertTrue(appContext.state is InitialStateImpl)
        assertFalse(appContext.backNavigationEnabled)
    }

    @Test
    fun model_initialization_updates_state() {
        val appContext = AppContextImpl()
        
        appContext.tell(ModelInitializationStarted)
        assertTrue(appContext.isAiModelLoading)
        assertFalse(appContext.isAiModelReady)
        
        appContext.tell(ModelInitializationSuccess("Test Model"))
        assertFalse(appContext.isAiModelLoading)
        assertTrue(appContext.isAiModelReady)
        assertEquals("Test Model", appContext.selectedAiModelName)
        
        appContext.tell(ModelInitializationFailed("Error message"))
        assertFalse(appContext.isAiModelLoading)
        assertFalse(appContext.isAiModelReady)
        assertEquals("Error message", appContext.aiModelError)
    }

    @Test
    fun available_models_loaded_updates_list() {
        val appContext = AppContextImpl()
        val models = listOf(ModelInfo("Model 1", "path1"), ModelInfo("Model 2", "path2"))
        
        appContext.tell(AvailableModelsLoaded(models))
        assertEquals(2, appContext.availableAiModels.size)
        assertEquals("Model 1", appContext.availableAiModels[0].name)
    }

    @Test
    fun workspace_folder_selected_updates_uri() {
        val appContext = AppContextImpl()
        appContext.tell(WorkspaceFolderSelected("content://test"))
        
        assertEquals("content://test", appContext.workspaceFolderUri)
        assertTrue(appContext.isWorkspaceLoading)
        // It should also transition to FileListState if it was in InitialState
        assertTrue(appContext.state is FileListStateImpl)
    }

    @Test
    fun toggle_ai_assistant_updates_visibility() {
        val appContext = AppContextImpl()
        assertFalse(appContext.isAiAssistantVisible)
        
        appContext.tell(ToggleAiAssistant)
        assertTrue(appContext.isAiAssistantVisible)
        
        appContext.tell(ToggleAiAssistant)
        assertFalse(appContext.isAiAssistantVisible)
    }

    @Test
    fun new_file_clicked_triggers_creation_flow() {
        var queryHandled = false
        var createFileRequested = false
        
        val controller: (Any) -> Unit = { message ->
            when (message) {
                is GetCurrentDateTimeString -> {
                    queryHandled = true
                    message.onResult("20231027_120000")
                }
                is CreateFileRequested -> {
                    createFileRequested = true
                    message.onSuccess("content://new_file_uri")
                }
            }
        }
        
        val appContext = AppContextImpl(controller = controller)
        appContext.workspaceFolderUri = "content://workspace"
        
        appContext.tell(NewFileClicked)
        
        assertTrue(queryHandled, "GetCurrentDateTimeString query should be handled")
        assertTrue(createFileRequested, "CreateFileRequested event should be triggered")
        assertTrue(appContext.state is EditorStateImpl, "Should transition to EditorState")
        assertTrue(appContext.editorState?.isNewFile == true, "Editor should be in new file mode")
    }
}
