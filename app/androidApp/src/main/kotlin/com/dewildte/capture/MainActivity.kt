package com.dewildte.capture

import android.app.ComponentCaller
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.dewildte.capture.ai.AiManager
import com.dewildte.capture.ai.AndroidAiManager
import com.dewildte.capture.ai.EditorTools
import com.dewildte.capture.ai.McpTools
import com.dewildte.capture.ai.SearchTools
import com.dewildte.capture.ai.SystemTools
import com.dewildte.capture.auth.GoogleAuthManager
import com.dewildte.capture.commands.*
import com.dewildte.capture.data.*
import com.dewildte.capture.events.*
import com.dewildte.capture.mcp.AndroidMcpManager
import com.dewildte.capture.mcp.McpManager
import com.dewildte.capture.queries.GetCurrentDateString
import com.dewildte.capture.queries.GetCurrentDateTimeString
import com.dewildte.capture.settings.AndroidSettingsManager
import com.dewildte.capture.settings.SettingsManager
import com.dewildte.capture.storage.AndroidStorageManager
import com.dewildte.capture.storage.StorageManager
import com.dewildte.capture.utils.Actor
import com.dewildte.capture.events.SelectWorkspaceFolderRequested
import com.dewildte.capture.events.WorkspaceFolderSelected
import com.dewildte.capture.events.RefreshWorkspaceRequested
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class MainActivity : ComponentActivity(), Actor {

    private val appContext: AppContextImpl = AppContextImpl(
        controller = this
    )

    private lateinit var settingsManager: SettingsManager
    private lateinit var storageManager: StorageManager
    private lateinit var aiManager: AiManager
    private lateinit var mcpManager: McpManager
    private lateinit var googleAuthManager: GoogleAuthManager

    private val fileSelector = registerForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            settingsManager.selectedFileUri = uri.toString()
            loadSelectedFile(uri)
        } else {
            appContext.tell(FailedToSelectFile())
        }
    }

    private val snippetsFileSelector = registerForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            settingsManager.selectedSnippetsFileUri = uri.toString()
            loadSelectedSnippetsFile(uri)
        } else {
            appContext.tell(FailedToSelectSnippetsFile())
        }
    }

    private val modelFileSelector = registerForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            settingsManager.selectedModelFileUri = uri.toString()
            val name = queryFileName(uri)
            val currentModels = settingsManager.availableModels.toMutableList()
            if (currentModels.none { it.uri == uri.toString() }) {
                currentModels.add(ModelInfo(name, uri.toString()))
                settingsManager.availableModels = currentModels
                appContext.tell(AvailableModelsLoaded(currentModels))
            }
            aiManager.initializeModel(uri.toString())
        }
    }

    private val aiStorageFolderSelector = registerForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            settingsManager.aiStorageFolderUri = uri.toString()
            appContext.tell(AiStorageFolderSelected(uri.toString()))
            storageManager.loadConversations(uri.toString())
        } else {
            appContext.tell(FailedToSelectStorageFolder)
        }
    }

    private val workspaceFolderSelector = registerForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            settingsManager.workspaceFolderUri = uri.toString()
            appContext.tell(WorkspaceFolderSelected(uri.toString()))
            storageManager.listWorkspaceFiles(uri.toString())
        }
    }

    private val googleAuthResolutionSelector = registerForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Log.i(TAG, "Google Auth resolution successful, retrying tool call...")
            // The next tool call will automatically fetch the new token
        } else {
            Log.e(TAG, "Google Auth resolution failed or cancelled.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        settingsManager = AndroidSettingsManager(this)
        storageManager = AndroidStorageManager(this, lifecycleScope, appContext)
        aiManager = AndroidAiManager(this, lifecycleScope, appContext, cacheDir)
        googleAuthManager = GoogleAuthManager(this, appContext)
        mcpManager = AndroidMcpManager(lifecycleScope)
        
        // Register tools
        aiManager.registerTool(EditorTools(appContext))
        aiManager.registerTool(SearchTools(storageManager, settingsManager))
        aiManager.registerTool(McpTools(mcpManager, aiManager as AndroidAiManager))
        aiManager.registerTool(SystemTools())

        setContent {
            val colorScheme = if (isSystemInDarkTheme()) {
                dynamicDarkColorScheme(this@MainActivity)
            } else {
                dynamicLightColorScheme(this@MainActivity)
            }

            MaterialTheme(
                colorScheme = colorScheme,
            ) {
                App(appContext = appContext)
            }

            BackHandler(enabled = appContext.backNavigationEnabled) {
                appContext.tell(SystemBackButtonClicked)
            }
        }

        appContext.mcpServers.addAll(settingsManager.mcpServers)
        appContext.searchToolEnabled = settingsManager.searchToolEnabled
        appContext.googleDriveEnabled = settingsManager.googleDriveEnabled
        appContext.googleCalendarEnabled = settingsManager.googleCalendarEnabled
        appContext.googleGmailEnabled = settingsManager.googleGmailEnabled
        appContext.googleTasksEnabled = settingsManager.googleTasksEnabled
        appContext.googleDocsEnabled = settingsManager.googleDocsEnabled
        appContext.googleSheetsEnabled = settingsManager.googleSheetsEnabled
        appContext.googleSlidesEnabled = settingsManager.googleSlidesEnabled
        appContext.googleClientId = settingsManager.googleClientId
        appContext.isGoogleAuthenticated = settingsManager.isGoogleAuthenticated
        appContext.googleUserEmail = settingsManager.googleUserEmail

        loadPersistedModel()
        
        appContext.tell(Start)
    }

    override fun onDestroy() {
        super.onDestroy()
        aiManager.close()
    }

    override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
        super.onNewIntent(intent, caller)
        val selectedText = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString() ?: ""
        Log.d(TAG, selectedText)
    }

    override fun tell(message: Any) {
        when (message) {
            is LogMessage -> logMessage(message.logData)

            is LoadSelectedFile -> {
                val uri = settingsManager.selectedFileUri?.toUri()
                if (uri != null) loadSelectedFile(uri) else appContext.tell(FailedToLoadSelectedFile())
            }

            is LoadFileFromUri -> {
                settingsManager.selectedFileUri = message.uri
                loadSelectedFile(message.uri.toUri())
            }

            is SelectTextFile -> fileSelector.launch(arrayOf("text/plain"))

            is SelectModelFile -> {
                Log.i(TAG, "Launching model selector")
                modelFileSelector.launch(arrayOf("application/octet-stream", "*/*"))
            }

            is SelectSnippetsFile -> snippetsFileSelector.launch(arrayOf("text/plain"))

            is LoadSnippetsFile -> {
                val uri = settingsManager.selectedSnippetsFileUri?.toUri()
                if (uri != null) loadSelectedSnippetsFile(uri) else appContext.tell(FailedToLoadSelectedSnippetsFile())
            }

            is UpdateSelectedFileContent -> {
                settingsManager.selectedFileUri?.let {
                    storageManager.writeTextFile(it, message.newContent.toString())
                }
            }

            is GetCurrentDateString -> message.onResult(LocalDateTime.now().toLocalDate().toString())
            
            is GetCurrentDateTimeString -> {
                val now = LocalDateTime.now()
                val timestamp = "${now.toLocalDate()}_${now.hour.toString().padStart(2, '0')}-${now.minute.toString().padStart(2, '0')}-${now.second.toString().padStart(2, '0')}"
                message.onResult(timestamp)
            }

            is SendAiMessage -> aiManager.sendMessage(
                message = message.message,
                conversation = message.conversation,
                systemInstruction = SYSTEM_INSTRUCTION
            )

            is StopAiGeneration -> aiManager.stopGeneration()

            is SelectAiStorageFolder -> aiStorageFolderSelector.launch(null)

            is SelectWorkspaceFolderRequested -> workspaceFolderSelector.launch(null)

            is WorkspaceFolderSelected -> {
                storageManager.listWorkspaceFiles(message.uri)
            }

            is RefreshWorkspaceRequested -> {
                settingsManager.workspaceFolderUri?.let { storageManager.listWorkspaceFiles(it) }
            }

            is CreateFileRequested -> {
                val parentUri = message.parentPath ?: settingsManager.workspaceFolderUri
                if (parentUri != null) {
                    val newFileUri = storageManager.createFile(parentUri, message.name)
                    if (newFileUri != null) {
                        appContext.tell(WorkspaceFileOperationSuccess)
                        message.onSuccess(newFileUri)
                    }
                }
            }

            is CreateFolderRequested -> {
                val parentUri = message.parentPath ?: settingsManager.workspaceFolderUri
                if (parentUri != null) {
                    val newFolderUri = storageManager.createDirectory(parentUri, message.name)
                    if (newFolderUri != null) {
                        appContext.tell(WorkspaceFileOperationSuccess)
                    }
                }
            }

            is RenameNodeRequested -> {
                val newUri = storageManager.renameNode(message.node.path, message.newName)
                if (newUri != null) {
                    appContext.tell(WorkspaceFileOperationSuccess)
                    message.onSuccess(newUri)
                }
            }

            is DeleteNodeRequested -> {
                if (storageManager.deleteNode(message.node.path)) {
                    appContext.tell(WorkspaceFileOperationSuccess)
                }
            }

            is LoadConversationsFromStorage -> {
                settingsManager.aiStorageFolderUri?.let { storageManager.loadConversations(it) }
            }

            is LoadAvailableModels -> {
                appContext.tell(AvailableModelsLoaded(settingsManager.availableModels))
            }

            is SwitchModel -> {
                aiManager.initializeModel(message.model.uri)
                settingsManager.selectedModelFileUri = message.model.uri
            }

            is DeleteModel -> {
                val currentModels = settingsManager.availableModels.toMutableList()
                currentModels.removeAll { it.uri == message.model.uri }
                settingsManager.availableModels = currentModels
                appContext.tell(AvailableModelsLoaded(currentModels))
                if (settingsManager.selectedModelFileUri == message.model.uri) {
                    settingsManager.selectedModelFileUri = null
                }
            }

            is SaveConversationToStorage -> {
                settingsManager.aiStorageFolderUri?.let {
                    storageManager.saveConversation(it, message.conversation)
                }
            }

            is SaveSettings -> {
                settingsManager.mcpServers = appContext.mcpServers
                settingsManager.searchToolEnabled = appContext.searchToolEnabled
                settingsManager.googleDriveEnabled = appContext.googleDriveEnabled
                settingsManager.googleCalendarEnabled = appContext.googleCalendarEnabled
                settingsManager.googleGmailEnabled = appContext.googleGmailEnabled
                settingsManager.googleTasksEnabled = appContext.googleTasksEnabled
                settingsManager.googleDocsEnabled = appContext.googleDocsEnabled
                settingsManager.googleSheetsEnabled = appContext.googleSheetsEnabled
                settingsManager.googleSlidesEnabled = appContext.googleSlidesEnabled
                settingsManager.googleClientId = appContext.googleClientId
                settingsManager.isGoogleAuthenticated = appContext.isGoogleAuthenticated
                settingsManager.googleUserEmail = appContext.googleUserEmail
                
                // Refresh Google MCP registrations if any toggle changed
                if (appContext.isGoogleAuthenticated) {
                    registerGoogleMcpServers()
                }
            }

            is UpdateGoogleClientId -> {
                settingsManager.googleClientId = message.clientId
            }

            is SignInWithGoogle -> {
                lifecycleScope.launch {
                    val result = googleAuthManager.signIn(appContext.googleClientId)
                    result.onSuccess { email ->
                        appContext.tell(GoogleAuthenticated(email))
                        registerGoogleMcpServers()
                        Toast.makeText(this@MainActivity, "Signed in as $email", Toast.LENGTH_SHORT).show()
                    }.onFailure { error ->
                        Toast.makeText(this@MainActivity, "Sign-in error: ${error.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }

            is SignOutWithGoogle -> {
                lifecycleScope.launch {
                    googleAuthManager.invalidateCurrentToken()
                    googleAuthManager.signOut()
                    appContext.tell(GoogleSignedOut)
                    // Disconnect Google MCP servers
                    unregisterGoogleMcpServers()
                }
            }

            is DeleteConversationFromStorage -> {
                settingsManager.aiStorageFolderUri?.let {
                    storageManager.deleteConversation(it, message.conversationId)
                }
            }

            is RequestGoogleAuthResolution -> {
                Log.i(TAG, "Launching Google Auth resolution intent")
                googleAuthResolutionSelector.launch(message.intent as Intent)
            }

            else -> Log.d(TAG, "MESSAGE: $message")
        }
    }

    private fun loadSelectedFile(uri: Uri) {
        storageManager.readTextFile(uri.toString())?.let { contents ->
            val name = uri.lastPathSegment?.replace("primary:", "") ?: ""
            appContext.state.tell(FileSelected(TextFile(uri.toString(), name, contents)))
        } ?: appContext.tell(FailedToLoadSelectedFile())
    }

    private fun loadSelectedSnippetsFile(uri: Uri) {
        storageManager.readTextFile(uri.toString())?.let { contents ->
            val name = uri.lastPathSegment?.replace("primary:", "") ?: ""
            appContext.state.tell(SnippetsFileSelected(TextFile(uri.toString(), name, contents)))
        } ?: appContext.tell(FailedToLoadSelectedSnippetsFile())
    }

    private fun loadPersistedModel() {
        settingsManager.selectedModelFileUri?.let { aiManager.initializeModel(it) }
        settingsManager.workspaceFolderUri?.let { 
            appContext.tell(WorkspaceFolderSelected(it))
            storageManager.listWorkspaceFiles(it) 
        }
        if (appContext.isGoogleAuthenticated) {
            registerGoogleMcpServers()
        }
    }

    private fun registerGoogleMcpServers() {
        lifecycleScope.launch {
            val authProvider: suspend () -> String? = { googleAuthManager.getAccessToken() }
            val onAuthError: suspend () -> Unit = { googleAuthManager.refreshAccessToken() }
            
            // Official Google MCP endpoints
            if (appContext.googleDriveEnabled) {
                mcpManager.connectToServer("https://drivemcp.googleapis.com/mcp/v1", authProvider, onAuthError)
            } else {
                mcpManager.disconnectFromServer("https://drivemcp.googleapis.com/mcp/v1")
            }
            
            if (appContext.googleCalendarEnabled) {
                mcpManager.connectToServer("https://calendarmcp.googleapis.com/mcp/v1", authProvider, onAuthError)
            } else {
                mcpManager.disconnectFromServer("https://calendarmcp.googleapis.com/mcp/v1")
            }

            if (appContext.googleGmailEnabled) {
                mcpManager.connectToServer("https://gmailmcp.googleapis.com/mcp/v1", authProvider, onAuthError)
            } else {
                mcpManager.disconnectFromServer("https://gmailmcp.googleapis.com/mcp/v1")
            }

            if (appContext.googleTasksEnabled) {
                mcpManager.connectToServer("https://tasksmcp.googleapis.com/mcp/v1", authProvider, onAuthError)
            } else {
                mcpManager.disconnectFromServer("https://tasksmcp.googleapis.com/mcp/v1")
            }

            if (appContext.googleDocsEnabled) {
                mcpManager.connectToServer("https://docsmcp.googleapis.com/mcp/v1", authProvider, onAuthError)
            } else {
                mcpManager.disconnectFromServer("https://docsmcp.googleapis.com/mcp/v1")
            }

            if (appContext.googleSheetsEnabled) {
                mcpManager.connectToServer("https://sheetsmcp.googleapis.com/mcp/v1", authProvider, onAuthError)
            } else {
                mcpManager.disconnectFromServer("https://sheetsmcp.googleapis.com/mcp/v1")
            }

            if (appContext.googleSlidesEnabled) {
                mcpManager.connectToServer("https://slidesmcp.googleapis.com/mcp/v1", authProvider, onAuthError)
            } else {
                mcpManager.disconnectFromServer("https://slidesmcp.googleapis.com/mcp/v1")
            }
        }
    }

    private fun unregisterGoogleMcpServers() {
        mcpManager.disconnectFromServer("https://drivemcp.googleapis.com/mcp/v1")
        mcpManager.disconnectFromServer("https://calendarmcp.googleapis.com/mcp/v1")
        mcpManager.disconnectFromServer("https://gmailmcp.googleapis.com/mcp/v1")
        mcpManager.disconnectFromServer("https://tasksmcp.googleapis.com/mcp/v1")
        mcpManager.disconnectFromServer("https://docsmcp.googleapis.com/mcp/v1")
        mcpManager.disconnectFromServer("https://sheetsmcp.googleapis.com/mcp/v1")
        mcpManager.disconnectFromServer("https://slidesmcp.googleapis.com/mcp/v1")
    }

    private fun queryFileName(uri: Uri): String {
        var result = "unknown_model.litertlm"
        if (uri.scheme == "content") {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = OpenableColumns.DISPLAY_NAME
                    val nameIndex = cursor.getColumnIndex(index)
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex)
                    }
                }
            }
        } else if (uri.scheme == "file") {
            result = uri.lastPathSegment ?: result
        }
        return result
    }

    private fun logMessage(message: LogData) {
        val msg = message.message
        val tag = message.tag
        val error = message.error
        when (message.level) {
            LogLevel.VERBOSE -> Log.v(tag, msg, error)
            LogLevel.DEBUG -> Log.d(tag, msg, error)
            LogLevel.INFO -> Log.i(tag, msg, error)
            LogLevel.WARN -> Log.w(tag, msg, error)
            LogLevel.ERROR -> Log.e(tag, msg, error)
            LogLevel.WTF -> Log.wtf(tag, msg, error)
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val SYSTEM_INSTRUCTION = """
# Role & Persona
You are a Personal Information Assistant for the 'Capture' app. 
Your primary goal is to help the user capture, organize, and manage their personal information across their notes and Google Workspace.
Be helpful, proactive, and professional.

# Tool Usage Guidelines
You have access to several toolsets. Use them strategically to fulfill user requests.

## 1. Editor Tools
- `readCurrentFileContent`: Use this to see the text currently open in the user's active editor tab.
- `writeToCurrentFile`: Use this to update or rewrite the active file. Always use this when the user asks you to "save", "write", or "fix" the current note.

## 2. Search Tools
- `searchFiles`: Use this to find information across ALL the user's saved text files. If a user asks a question and you don't see the answer in the current editor, search their files first.

## 3. System Tools
- `getCurrentDateTime`: Use this to get the exact current date and time. This is critical for resolving relative dates like "today", "yesterday", or "next week" in searches and calendar events.

## 4. Google Workspace (via MCP)
The following servers are pre-connected. To use them, follow this workflow:
1. SURVEY: Call `listMcpTools(url)` to see available tool names and high-level descriptions.
2. INSPECT: Once you identify a likely tool, call `getMcpToolSchema(url, toolName)` to see its formal JSON parameters.
3. EXECUTE: Call `callMcpTool(url, toolName, arguments)` with the correctly formatted JSON.

Pre-connected Servers:
- https://drivemcp.googleapis.com/mcp/v1 (Google Drive)
- https://calendarmcp.googleapis.com/mcp/v1 (Google Calendar)
- https://gmailmcp.googleapis.com/mcp/v1 (Gmail)
- https://tasksmcp.googleapis.com/mcp/v1 (Google Tasks)
- https://docsmcp.googleapis.com/mcp/v1 (Google Docs)
- https://sheetsmcp.googleapis.com/mcp/v1 (Google Sheets)
- https://slidesmcp.googleapis.com/mcp/v1 (Google Slides)

# Operational Rules
- **Plan First**: Before calling complex Workspace tools, briefly explain what you are going to do.
- **Output Format**: IMPORTANT: Output your responses in PLAIN TEXT ONLY. Do not use Markdown (no bolding, no italics, no code blocks with backticks). The UI cannot render rich text yet.
- **Be Concise**: Keep your answers focused on the user's request.
- **Clarify**: If a request is ambiguous (e.g., "Search for my flight"), ask for details like dates or flight numbers if the search returns too many results.

# Error Reporting
If a tool returns an error message (e.g., "Error calling tool...", "401 Unauthorized"), do not hide it. 
Explicitly tell the user that the tool failed and provide the technical reason returned by the tool.
        """
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
