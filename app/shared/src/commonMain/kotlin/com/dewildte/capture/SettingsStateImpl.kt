package com.dewildte.capture

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import com.dewildte.capture.commands.*
import com.dewildte.capture.data.LogData
import com.dewildte.capture.data.LogLevel
import com.dewildte.capture.data.TextFile
import com.dewildte.capture.events.*

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
                    ctx.state = this

                    ctx.tell(LoadSnippetsFile)
                }
            }

            is SnippetsFileLoaded -> {
                // TODO: Parse the Snippets out of the file.
                parseSnippets(message.file)
            }

            is BackClicked -> {
                context?.tell(GoBack)
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
                    ctx.tell(LogMessage(LogData(LogLevel.INFO, TAG, "Added MCP Server: ${event.url}")))
                }
            }
            is RemoveMcpServerClicked -> {
                ctx.mcpServers.remove(event.url)
            }
            is ToggleSearchToolClicked -> {
                ctx.searchToolEnabled = event.enabled
            }
            is SignInWithGoogleClicked -> {
                ctx.tell(SignInWithGoogle)
            }
            is SignOutWithGoogleClicked -> {
                ctx.tell(SignOutWithGoogle)
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
                ctx.tell(UpdateGoogleClientId(event.clientId))
            }
        }
        ctx.tell(SaveSettings)
    }

    private fun handleNavigationEvent(event: NavigationEvent) {
        val ctx = context ?: return

        when (event) {
            is EditorTabClicked -> {
                // Handled in AppContextImpl
            }

            is AiTabClicked -> {
                ctx.tell(ToggleAiAssistant)
            }

            is MenuTabClicked -> {
                ctx.tell(SelectTextFile)
            }

            else -> {
                // Handled in AppContextImpl
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
