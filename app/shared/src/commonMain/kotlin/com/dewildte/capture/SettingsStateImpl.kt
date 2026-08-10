package com.dewildte.capture

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import com.dewildte.capture.commands.*
import com.dewildte.capture.data.LogData
import com.dewildte.capture.data.LogLevel
import com.dewildte.capture.data.TextFile
import com.dewildte.capture.events.*
import com.dewildte.capture.navigation.AppRoute

@Stable
class SettingsStateImpl(
    snippets: List<String> = emptyList()
) : SettingsState {

    private var context: MutableAppContext? = null
    private var previousState: AppState? = null

    override var snippets: List<String> = mutableStateListOf(*snippets.toTypedArray())
    override val mcpServers: List<String> get() = context?.mcpServers ?: emptyList()
    override val searchToolEnabled: Boolean get() = context?.searchToolEnabled ?: true
    override val isGoogleAuthenticated: Boolean get() = context?.isGoogleAuthenticated ?: false
    override val googleUserEmail: String? get() = context?.googleUserEmail
    override val googleDriveEnabled: Boolean get() = context?.googleDriveEnabled ?: true
    override val googleCalendarEnabled: Boolean get() = context?.googleCalendarEnabled ?: true
    override val googleGmailEnabled: Boolean get() = context?.googleGmailEnabled ?: true
    override val googleTasksEnabled: Boolean get() = context?.googleTasksEnabled ?: true
    override val googleDocsEnabled: Boolean get() = context?.googleDocsEnabled ?: true
    override val googleSheetsEnabled: Boolean get() = context?.googleSheetsEnabled ?: true
    override val googleSlidesEnabled: Boolean get() = context?.googleSlidesEnabled ?: true
    override val googleClientId: String? get() = context?.googleClientId

    override fun tell(message: Any) {
        when (message) {
            is SetContext -> {
                previousState = message.context.state
                context = message.context
            }

            is Start -> {
                context?.let { ctx ->
                    ctx.backNavigationEnabled = previousState != this
                    ctx.state = this

                    ctx.controller.tell(LoadSnippetsFile)
                }
            }

            is SnippetsFileLoaded -> {
                // TODO: Parse the Snippets out of the file.
                parseSnippets(message.file)
            }

            is BackClicked -> {
                context?.let { ctx ->
                    if (ctx.navBackStack.size > 1) {
                        ctx.navBackStack.removeAt(ctx.navBackStack.size - 1)
                        // Transition to the new top of stack is handled by syncStateToBackStack in AppContextImpl
                        // if we triggered this via SystemBackButtonClicked, but for BackClicked we need to call it or handle it.
                        // Actually, let's just make AppContextImpl handle BackClicked too or use a common event.
                        ctx.tell(SystemBackButtonClicked) 
                    } else {
                        ctx.navBackStack.clear()
                        ctx.navBackStack.add(AppRoute.Editor)
                        ctx.tell(TransitionToState(ctx.editorState!!))
                    }
                }
            }

            is SystemBackButtonClicked -> {
                // Handled in AppContextImpl, no-op here to avoid double-popping
            }

            is NavigationEvent -> {
                handleNavigationEvent(message)
            }

            is SettingsEvent -> {
                handleSettingsEvent(message)
            }
        }
    }

    private fun handleSettingsEvent(event: SettingsEvent) {
        val ctx = context ?: return

        when (event) {
            is AddMcpServerClicked -> {
                if (!ctx.mcpServers.contains(event.url)) {
                    ctx.mcpServers.add(event.url)
                    ctx.controller.tell(LogMessage(LogData(LogLevel.INFO, TAG, "Added MCP Server: ${event.url}")))
                }
            }
            is RemoveMcpServerClicked -> {
                ctx.mcpServers.remove(event.url)
            }
            is ToggleSearchToolClicked -> {
                ctx.searchToolEnabled = event.enabled
            }
            is SignInWithGoogleClicked -> {
                ctx.controller.tell(SignInWithGoogle)
            }
            is SignOutWithGoogleClicked -> {
                ctx.controller.tell(SignOutWithGoogle)
            }
            is GoogleAuthenticated -> {
                ctx.isGoogleAuthenticated = true
                ctx.googleUserEmail = event.email
            }
            is GoogleSignedOut -> {
                ctx.isGoogleAuthenticated = false
                ctx.googleUserEmail = null
            }
            is ToggleWorkspaceToolClicked -> {
                when (event.tool) {
                    WorkspaceTool.DRIVE -> ctx.googleDriveEnabled = event.enabled
                    WorkspaceTool.CALENDAR -> ctx.googleCalendarEnabled = event.enabled
                    WorkspaceTool.GMAIL -> ctx.googleGmailEnabled = event.enabled
                    WorkspaceTool.TASKS -> ctx.googleTasksEnabled = event.enabled
                    WorkspaceTool.DOCS -> ctx.googleDocsEnabled = event.enabled
                    WorkspaceTool.SHEETS -> ctx.googleSheetsEnabled = event.enabled
                    WorkspaceTool.SLIDES -> ctx.googleSlidesEnabled = event.enabled
                }
            }
            is UpdateGoogleClientIdClicked -> {
                ctx.googleClientId = event.clientId
                ctx.controller.tell(UpdateGoogleClientId(event.clientId))
            }
        }
        ctx.controller.tell(SaveSettings)
    }

    private fun handleNavigationEvent(event: NavigationEvent) {
        val ctx = context ?: return

        when (event) {
            is EditorTabClicked -> {
                ctx.navBackStack.clear()
                ctx.navBackStack.add(AppRoute.Editor)
            }

            is AiTabClicked -> {
                if (!ctx.navBackStack.contains(AppRoute.AiAssistant)) {
                    ctx.navBackStack.add(AppRoute.AiAssistant)
                }
            }

            is MenuTabClicked -> {
                ctx.controller.tell(SelectTextFile)
            }
        }
    }

    private fun parseSnippets(file: TextFile) {
        // TODO: Extract Snippets
        // TODO: Set the snippets data
    }

    companion object {
        private const val TAG = "SettingsState"
    }
}
