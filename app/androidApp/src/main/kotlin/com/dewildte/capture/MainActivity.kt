package com.dewildte.capture

import android.app.ComponentCaller
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
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
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import com.dewildte.capture.commands.DeleteConversationFromStorage
import com.dewildte.capture.commands.DeleteModel
import com.dewildte.capture.commands.LoadAvailableModels
import com.dewildte.capture.commands.LoadConversationsFromStorage
import com.dewildte.capture.commands.LoadSelectedFile
import com.dewildte.capture.commands.LoadSnippetsFile
import com.dewildte.capture.commands.LogMessage
import com.dewildte.capture.commands.SaveConversationToStorage
import com.dewildte.capture.commands.SelectAiStorageFolder
import com.dewildte.capture.commands.SelectModelFile
import com.dewildte.capture.commands.SelectSnippetsFile
import com.dewildte.capture.commands.SelectTextFile
import com.dewildte.capture.commands.SendAiMessage
import com.dewildte.capture.commands.StopAiGeneration
import com.dewildte.capture.commands.SwitchModel
import com.dewildte.capture.commands.UpdateSelectedFileContent
import com.dewildte.capture.data.Conversation
import com.dewildte.capture.data.LogData
import com.dewildte.capture.data.LogLevel
import com.dewildte.capture.data.Message
import com.dewildte.capture.data.MessageRole
import com.dewildte.capture.data.ModelInfo
import com.dewildte.capture.data.TextFile
import com.dewildte.capture.events.AiResponseChunk
import com.dewildte.capture.events.AiResponseComplete
import com.dewildte.capture.events.AiResponseError
import com.dewildte.capture.events.AiStorageFolderSelected
import com.dewildte.capture.events.AvailableModelsLoaded
import com.dewildte.capture.events.ConversationsLoaded
import com.dewildte.capture.events.FailedToLoadSelectedFile
import com.dewildte.capture.events.FailedToLoadSelectedSnippetsFile
import com.dewildte.capture.events.FailedToSelectFile
import com.dewildte.capture.events.FailedToSelectModelFile
import com.dewildte.capture.events.FailedToSelectSnippetsFile
import com.dewildte.capture.events.FailedToSelectStorageFolder
import com.dewildte.capture.events.FailedToUpdateFileContent
import com.dewildte.capture.events.FileSelected
import com.dewildte.capture.events.ModelInitializationFailed
import com.dewildte.capture.events.ModelInitializationStarted
import com.dewildte.capture.events.ModelInitializationSuccess
import com.dewildte.capture.events.SnippetsFileSelected
import com.dewildte.capture.events.SystemBackButtonClicked
import com.dewildte.capture.queries.GetCurrentDateString
import com.dewildte.capture.utils.Actor
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import com.google.ai.edge.litertlm.Message as LmMessage

class MainActivity : ComponentActivity(), Actor {

    private val appContext: AppContextImpl = AppContextImpl(
        controller = this
    )
    private val fileSelector = registerForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        try {
            if (uri != null) {
                Log.d(TAG, "Uri:\n$uri")
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                getPreferences(MODE_PRIVATE).edit {
                    putString(KEY_SELECTED_FILE_URI, uri.toString())
                }

                onSelectedFileUriFound(uri)

            } else {
                appContext.tell(FailedToSelectFile())
            }
        } catch (cause: Throwable) {
            appContext.tell(FailedToSelectFile(cause))
        }
    }

    private val snippetsFileSelector = registerForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        try {
            if (uri != null) {
                Log.d(TAG, "Uri:\n$uri")
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                getPreferences(MODE_PRIVATE).edit {
                    putString(KEY_SELECTED_SNIPPETS_FILE_URI, uri.toString())
                }

                onSelectedSnippetsFileUriFound(uri)

            } else {
                appContext.tell(FailedToSelectSnippetsFile())
            }
        } catch (cause: Throwable) {
            appContext.tell(FailedToSelectSnippetsFile(cause))
        }
    }

    private val modelFileSelector = registerForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        try {
            if (uri != null) {
                Log.d(TAG, "Uri:\n$uri")
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                getPreferences(MODE_PRIVATE).edit {
                    putString(KEY_SELECTED_MODEL_FILE_URI, uri.toString())
                }

                onSelectedModelFileUriFound(uri)

            } else {
//                appContext.tell(FailedToSelectModelFile())
            }
        } catch (cause: Throwable) {
//            appContext.tell(FailedToSelectModelFile(cause))
        }
    }

    private val aiStorageFolderSelector = registerForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        try {
            if (uri != null) {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                getPreferences(MODE_PRIVATE).edit {
                    putString(KEY_AI_STORAGE_FOLDER_URI, uri.toString())
                }

                appContext.tell(AiStorageFolderSelected(uri.toString()))
                loadConversations()
            } else {
                appContext.tell(FailedToSelectStorageFolder)
            }
        } catch (cause: Throwable) {
            appContext.tell(FailedToSelectStorageFolder)
        }
    }

    private lateinit var engine: Engine
    private val engineMutex = Mutex()
    private var aiJob: Job? = null
    private var aiConversation: com.google.ai.edge.litertlm.Conversation? = null
    private var currentAiConversationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

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

        loadPersistedModel()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::engine.isInitialized) {
            try {
                engine.close()
                logMessage(LogData(LogLevel.INFO, TAG, "AI Engine closed on destruction"))
            } catch (e: Exception) {
                Log.e(TAG, "Error closing engine on destroy", e)
            }
        }
    }

    override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
        super.onNewIntent(intent, caller)
        val selectedText = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString() ?: ""
        Log.d(TAG, selectedText)
    }

    override fun tell(message: Any) {
        when (message) {
            is LogMessage -> {
                logMessage(message.logData)
            }

            is LoadSelectedFile -> {
                try {
                    val uri = getPreferences(MODE_PRIVATE)
                        .getString(
                            KEY_SELECTED_FILE_URI,
                            null
                        )
                        ?.toUri()

                    if (uri != null) {
                        onSelectedFileUriFound(uri)
                    } else {
                        getPreferences(MODE_PRIVATE).edit {
                            putString(KEY_SELECTED_FILE_URI, null)
                        }
                        appContext.tell(FailedToLoadSelectedFile())
                    }
                } catch (cause: Throwable) {
                    appContext.tell(FailedToLoadSelectedFile(cause))
                }
            }

            is SelectTextFile -> {
                try {
                    fileSelector.launch(arrayOf("text/plain"))
                } catch (cause: Throwable) {
                    appContext.tell(FailedToSelectFile(cause))
                }
            }

            is SelectModelFile -> {
                try {
                    logMessage(LogData(LogLevel.INFO, TAG, "Launching model selector"))
                    modelFileSelector.launch(arrayOf("application/octet-stream", "*/*"))
                } catch (cause: Throwable) {
                    logMessage(LogData(LogLevel.ERROR, TAG, "Failed to launch model selector", cause))
                    appContext.tell(FailedToSelectModelFile(cause))
                }
            }

            is SelectSnippetsFile -> {
                try {
                    snippetsFileSelector.launch(arrayOf("text/plain"))
                } catch (cause: Throwable) {
                    appContext.tell(FailedToSelectSnippetsFile(cause))
                }
            }

            is LoadSnippetsFile -> {
                try {
                    val uri = getPreferences(MODE_PRIVATE)
                        .getString(
                            KEY_SELECTED_SNIPPETS_FILE_URI,
                            null
                        )
                        ?.toUri()

                    if (uri != null) {
                        onSelectedSnippetsFileUriFound(uri)
                    } else {
                        getPreferences(MODE_PRIVATE).edit {
                            putString(KEY_SELECTED_SNIPPETS_FILE_URI, null)
                        }
                        appContext.tell(FailedToLoadSelectedSnippetsFile())
                    }
                } catch (cause: Throwable) {
                    appContext.tell(FailedToLoadSelectedSnippetsFile(cause))
                }
            }

            is UpdateSelectedFileContent -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    logMessage(
                        LogData(
                            level = LogLevel.DEBUG,
                            tag = TAG,
                            message = message.newContent.toString()
                        )
                    )
                    try {

                        val uri = getPreferences(MODE_PRIVATE)
                            .getString(
                                KEY_SELECTED_FILE_URI,
                                null
                            )
                            ?.toUri()

                        if (uri != null) {
                            contentResolver.openOutputStream(uri, "wt")?.use { stream ->
                                stream.write(message.newContent.toString().toByteArray())
                                stream.flush()
                            }
                        } else {
                            getPreferences(MODE_PRIVATE).edit {
                                putString(KEY_SELECTED_FILE_URI, null)
                            }
                        }
                    } catch (cause: Throwable) {
                        appContext.tell(FailedToUpdateFileContent(cause))
                    }
                }
            }

            is GetCurrentDateString -> {
                val date = LocalDateTime.now().toLocalDate().toString()
                message.onResult(date)
            }

            is SendAiMessage -> {
                sendAiMessage(message.message, message.conversation)
            }

            is StopAiGeneration -> {
                logMessage(LogData(LogLevel.INFO, TAG, "Stopping AI generation"))
                aiJob?.cancel()
                aiJob = null
                appContext.tell(AiResponseComplete)
            }

            is SelectAiStorageFolder -> {
                try {
                    aiStorageFolderSelector.launch(null)
                } catch (cause: Throwable) {
                    appContext.tell(FailedToSelectStorageFolder)
                }
            }

            is LoadConversationsFromStorage -> {
                loadConversations()
            }

            is LoadAvailableModels -> {
                appContext.tell(AvailableModelsLoaded(getAvailableModels()))
            }

            is SwitchModel -> {
                onSelectedModelFileUriFound(Uri.parse(message.model.uri))
            }

            is DeleteModel -> {
                deleteModel(message.model)
            }

            is SaveConversationToStorage -> {
                saveConversation(message.conversation)
            }

            is DeleteConversationFromStorage -> {
                deleteConversation(message.conversationId)
            }

            else -> {
                logMessage(
                    LogData(
                        level = LogLevel.DEBUG,
                        tag = TAG,
                        message = "MESSAGE: $message"
                    )
                )
            }
        }

    }

    private fun sendAiMessage(message: String, conversation: Conversation) {
        logMessage(LogData(LogLevel.INFO, TAG, "Sending AI message: ${message.take(50)}..."))
        if (!::engine.isInitialized) {
            logMessage(LogData(LogLevel.ERROR, TAG, "AI Engine not initialized"))
            appContext.tell(AiResponseError(IllegalStateException("AI Engine not initialized. Please select a model file.")))
            return
        }

        aiJob?.cancel()
        aiJob = lifecycleScope.launch(Dispatchers.IO) {
            try {
                if (aiConversation == null || currentAiConversationId != conversation.id) {
                    aiConversation?.close()
                    
                    // Map history: all messages EXCEPT the last two (new user message and empty AI message)
                    val history = conversation.messages
                        .dropLast(2)
                        .map { msg ->
                            when (msg.role) {
                                MessageRole.USER -> LmMessage.user(msg.content)
                                MessageRole.AI -> LmMessage.model(msg.content)
                            }
                        }
                    
                    val config = ConversationConfig(initialMessages = history)
                    aiConversation = engine.createConversation(config)
                    currentAiConversationId = conversation.id
                    logMessage(LogData(LogLevel.DEBUG, TAG, "AI Conversation (re)initialized for: ${conversation.id} with history size: ${history.size}"))
                }

                aiConversation?.let { convo ->
                    convo.sendMessageAsync(message).collect { chunk ->
                        val textChunk = (chunk.contents.contents.firstOrNull() as? com.google.ai.edge.litertlm.Content.Text)?.text ?: ""
                        appContext.tell(AiResponseChunk(textChunk))
                    }
                    appContext.tell(AiResponseComplete)
                }
            } catch (cause: Throwable) {
                appContext.tell(AiResponseError(cause))
            } finally {
                aiJob = null
            }
        }
    }

    private fun loadConversations() {
        val uriString = getPreferences(MODE_PRIVATE).getString(KEY_AI_STORAGE_FOLDER_URI, null) ?: return
        val rootUri = Uri.parse(uriString)
        val root = DocumentFile.fromTreeUri(this, rootUri) ?: return

        lifecycleScope.launch(Dispatchers.IO) {
            val conversations = mutableListOf<Conversation>()
            root.listFiles().forEach { file ->
                if (file.isFile && file.name?.endsWith(".txt") == true) {
                    try {
                        contentResolver.openInputStream(file.uri)?.use { inputStream ->
                            val content = inputStream.reader().readText()
                            parseConversation(file.name!!, content)?.let {
                                conversations.add(it)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse conversation file: ${file.name}", e)
                    }
                }
            }
            appContext.tell(ConversationsLoaded(conversations.sortedByDescending { it.updatedAt }))
        }
    }

    private fun parseConversation(fileName: String, content: String): Conversation? {
        try {
            val id = fileName.removeSuffix(".txt")
            val lines = content.lines()
            if (lines.isEmpty()) return null

            var title = "Untitled"
            var updatedAt = 0L
            var messageStartLine = 0

            for (i in lines.indices) {
                val line = lines[i]
                if (line.startsWith("Title: ")) title = line.removePrefix("Title: ")
                if (line.startsWith("UpdatedAt: ")) updatedAt = line.removePrefix("UpdatedAt: ").toLongOrNull() ?: 0L
                if (line == "---") {
                    messageStartLine = i + 1
                    break
                }
            }

            val messages = mutableListOf<Message>()
            var currentRole: MessageRole? = null
            var currentContent = StringBuilder()

            for (i in messageStartLine until lines.size) {
                val line = lines[i]
                if (line.startsWith("[USER]: ")) {
                    if (currentRole != null) {
                        messages.add(Message(Uuid.random().toString(), currentRole, currentContent.toString().trim(), 0))
                    }
                    currentRole = MessageRole.USER
                    currentContent = StringBuilder(line.removePrefix("[USER]: "))
                } else if (line.startsWith("[AI]: ")) {
                    if (currentRole != null) {
                        messages.add(Message(Uuid.random().toString(), currentRole, currentContent.toString().trim(), 0))
                    }
                    currentRole = MessageRole.AI
                    currentContent = StringBuilder(line.removePrefix("[AI]: "))
                } else {
                    currentContent.append("\n").append(line)
                }
            }
            if (currentRole != null) {
                messages.add(Message(Uuid.random().toString(), currentRole, currentContent.toString().trim(), 0))
            }

            return Conversation(id, title, messages, updatedAt, updatedAt)
        } catch (e: Exception) {
            return null
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun parseMessage(role: MessageRole, content: String): Message {
        return Message(Uuid.random().toString(), role, content, 0)
    }

    private fun saveConversation(conversation: Conversation) {
        val uriString = getPreferences(MODE_PRIVATE).getString(KEY_AI_STORAGE_FOLDER_URI, null) ?: return
        val rootUri = Uri.parse(uriString)
        val root = DocumentFile.fromTreeUri(this, rootUri) ?: return

        lifecycleScope.launch(Dispatchers.IO) {
            val fileName = "${conversation.id}.txt"
            var file = root.findFile(fileName)
            if (file == null) {
                file = root.createFile("text/plain", fileName)
            }

            file?.let {
                contentResolver.openOutputStream(it.uri, "wt")?.use { outputStream ->
                    val content = StringBuilder()
                    content.append("Title: ").append(conversation.title).append("\n")
                    content.append("UpdatedAt: ").append(conversation.updatedAt).append("\n")
                    content.append("---\n")
                    conversation.messages.forEach { msg ->
                        val roleTag = if (msg.role == MessageRole.USER) "[USER]" else "[AI]"
                        content.append(roleTag).append(": ").append(msg.content).append("\n\n")
                    }
                    outputStream.write(content.toString().toByteArray())
                    outputStream.flush()
                }
            }
        }
    }

    private fun deleteConversation(conversationId: String) {
        val uriString = getPreferences(MODE_PRIVATE).getString(KEY_AI_STORAGE_FOLDER_URI, null) ?: return
        val rootUri = Uri.parse(uriString)
        val root = DocumentFile.fromTreeUri(this, rootUri) ?: return

        lifecycleScope.launch(Dispatchers.IO) {
            val fileName = "$conversationId.txt"
            root.findFile(fileName)?.delete()
        }
    }

    private fun logMessage(message: LogData) {
        when (message.level) {
            LogLevel.VERBOSE -> {
                Log.v(message.tag, message.message, message.error)
            }

            LogLevel.DEBUG -> {
                Log.d(message.tag, message.message, message.error)
            }

            LogLevel.INFO -> {
                Log.i(message.tag, message.message, message.error)
            }

            LogLevel.WARN -> {
                Log.w(message.tag, message.message, message.error)
            }

            LogLevel.ERROR -> {
                Log.e(message.tag, message.message, message.error)
            }

            LogLevel.WTF -> {
                Log.wtf(message.tag, message.message, message.error)
            }
        }
    }

    private fun onSelectedFileUriFound(uri: Uri) {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            val contents = inputStream.reader().readText()
            val path = uri.lastPathSegment?.replace("primary:", "") ?: ""
            val textFile = TextFile(
                path = path,
                contents = contents,
            )

            appContext.state.tell(FileSelected(textFile))
        }
    }

    private fun onSelectedSnippetsFileUriFound(uri: Uri) {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            val contents = inputStream.reader().readText()
            val path = uri.lastPathSegment?.replace("primary:", "") ?: ""
            val textFile = TextFile(
                path = path,
                contents = contents,
            )

            appContext.state.tell(SnippetsFileSelected(textFile))
        }
    }

    private fun Uri.queryFileName(): String {
        var result = "unknown_model.litertlm"
        if (this.scheme == "content") {
            this@MainActivity.contentResolver.query(this, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        result = cursor.getString(index)
                    }
                }
            }
        } else if (this.scheme == "file") {
            result = this.lastPathSegment ?: result
        }
        return result
    }

    private fun onSelectedModelFileUriFound(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            engineMutex.withLock {
                val name = uri.queryFileName()
                val destination = File(cacheDir, name)
                try {
                    appContext.tell(ModelInitializationStarted)
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        destination.outputStream().use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }

                    logMessage(LogData(LogLevel.INFO, TAG, "Model file copied: ${destination.absolutePath} (${destination.length()} bytes)"))

                    if (::engine.isInitialized) {
                        logMessage(LogData(LogLevel.DEBUG, TAG, "Closing existing AI Engine"))
                        engine.close()
                    }

                    val modelCacheDir = File(cacheDir, "model_cache")
                    if (!modelCacheDir.exists()) {
                        modelCacheDir.mkdirs()
                    }

                    val engineConfig = EngineConfig(
                        modelPath = destination.absolutePath,
                        backend = Backend.CPU(),
                        cacheDir = modelCacheDir.path,
                        maxNumTokens = 2048
                    )

                    engine = Engine(engineConfig)
                    engine.initialize()
                    aiConversation = null
                    currentAiConversationId = null

                    logMessage(LogData(LogLevel.INFO, TAG, "AI Engine initialized successfully with model: $name"))
                    appContext.tell(ModelInitializationSuccess(name))

                    // Persist model in list
                    val currentModels = getAvailableModels().toMutableList()
                    if (currentModels.none { it.uri == uri.toString() }) {
                        currentModels.add(ModelInfo(name, uri.toString()))
                        saveAvailableModels(currentModels)
                        appContext.tell(AvailableModelsLoaded(currentModels))
                    }

                    getPreferences(MODE_PRIVATE).edit {
                        putString(KEY_SELECTED_MODEL_FILE_URI, uri.toString())
                    }

                } catch (cause: Throwable) {
                    logMessage(LogData(LogLevel.ERROR, TAG, "Failed to initialize AI Engine: ${cause.message}"))
                    appContext.tell(ModelInitializationFailed(cause.message ?: "Unknown initialization error"))
                    // Clean up destination if initialization failed to avoid corrupted state
                    if (destination.exists()) {
                        destination.delete()
                    }
                }
            }
        }
    }

    private fun loadPersistedModel() {
        val uriString = getPreferences(MODE_PRIVATE).getString(KEY_SELECTED_MODEL_FILE_URI, null) ?: return
        try {
            val uri = Uri.parse(uriString)
            onSelectedModelFileUriFound(uri)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse persisted model URI", e)
        }
    }

    private fun getAvailableModels(): List<ModelInfo> {
        val json = getPreferences(MODE_PRIVATE).getString(KEY_AVAILABLE_MODELS, null) ?: return emptyList()
        return try {
            Json.decodeFromString(json)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveAvailableModels(models: List<ModelInfo>) {
        val json = Json.encodeToString(models)
        getPreferences(MODE_PRIVATE).edit {
            putString(KEY_AVAILABLE_MODELS, json)
        }
    }

    private fun deleteModel(model: ModelInfo) {
        val currentModels = getAvailableModels().toMutableList()
        currentModels.removeAll { it.uri == model.uri }
        saveAvailableModels(currentModels)
        appContext.tell(AvailableModelsLoaded(currentModels))

        // If deleted model was the selected one, clear selection
        val selectedUri = getPreferences(MODE_PRIVATE).getString(KEY_SELECTED_MODEL_FILE_URI, null)
        if (selectedUri == model.uri) {
            getPreferences(MODE_PRIVATE).edit {
                putString(KEY_SELECTED_MODEL_FILE_URI, null)
            }
            // Ideally we'd close the engine too, or switch to another one
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val KEY_SELECTED_FILE_URI = "key_selected_file_uri"
        private const val KEY_SELECTED_SNIPPETS_FILE_URI = "key_selected_snippets_file_uri"
        private const val KEY_SELECTED_MODEL_FILE_URI = "key_selected_model_file_uri"
        private const val KEY_AI_STORAGE_FOLDER_URI = "key_ai_storage_folder_uri"
        private const val KEY_AVAILABLE_MODELS = "key_available_models"
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
