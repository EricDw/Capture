package com.dewildte.capture

import com.dewildte.capture.commands.SetContext
import com.dewildte.capture.commands.SaveSettings
import com.dewildte.capture.events.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SettingsStateImplTest {

    @Test
    fun adding_mcp_server_updates_context_and_saves() {
        var saveRequested = false
        val controller: (Any) -> Unit = { message ->
            if (message is SaveSettings) {
                saveRequested = true
            }
        }
        
        val appContext = AppContextImpl(controller = controller)
        val settingsState = SettingsStateImpl()
        settingsState.tell(SetContext(appContext))
        
        settingsState.tell(AddMcpServerClicked("http://server.com"))
        
        assertTrue(appContext.mcpServers.contains("http://server.com"))
        assertTrue(saveRequested)
    }

    @Test
    fun removing_mcp_server_updates_context_and_saves() {
        val appContext = AppContextImpl()
        appContext.mcpServers.add("http://server.com")
        
        val settingsState = SettingsStateImpl()
        settingsState.tell(SetContext(appContext))
        
        settingsState.tell(RemoveMcpServerClicked("http://server.com"))
        
        assertFalse(appContext.mcpServers.contains("http://server.com"))
    }

    @Test
    fun toggling_search_tool_updates_context() {
        val appContext = AppContextImpl()
        appContext.searchToolEnabled = true
        
        val settingsState = SettingsStateImpl()
        settingsState.tell(SetContext(appContext))
        
        settingsState.tell(ToggleSearchToolClicked(false))
        assertFalse(appContext.searchToolEnabled)
        
        settingsState.tell(ToggleSearchToolClicked(true))
        assertTrue(appContext.searchToolEnabled)
    }

    @Test
    fun google_auth_events_update_context() {
        val appContext = AppContextImpl()
        val settingsState = SettingsStateImpl()
        settingsState.tell(SetContext(appContext))
        
        settingsState.tell(GoogleAuthenticated("test@gmail.com"))
        assertTrue(appContext.isGoogleAuthenticated)
        assertEquals("test@gmail.com", appContext.googleUserEmail)
        
        settingsState.tell(GoogleSignedOut)
        assertFalse(appContext.isGoogleAuthenticated)
        assertNull(appContext.googleUserEmail)
    }

    @Test
    fun toggling_workspace_tools_updates_context() {
        val appContext = AppContextImpl()
        val settingsState = SettingsStateImpl()
        settingsState.tell(SetContext(appContext))
        
        settingsState.tell(ToggleWorkspaceToolClicked(WorkspaceTool.DRIVE, false))
        assertFalse(appContext.googleDriveEnabled)
        
        settingsState.tell(ToggleWorkspaceToolClicked(WorkspaceTool.GMAIL, false))
        assertFalse(appContext.googleGmailEnabled)
    }

    @Test
    fun update_google_client_id_updates_context() {
        val appContext = AppContextImpl()
        val settingsState = SettingsStateImpl()
        settingsState.tell(SetContext(appContext))
        
        settingsState.tell(UpdateGoogleClientIdClicked("new-client-id"))
        assertEquals("new-client-id", appContext.googleClientId)
    }
}
