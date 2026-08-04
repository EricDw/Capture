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
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.dewildte.capture.ai.AiManager
import com.dewildte.capture.ai.AndroidAiManager
import com.dewildte.capture.commands.*
import com.dewildte.capture.data.*
import com.dewildte.capture.events.*
import com.dewildte.capture.queries.GetCurrentDateString
import com.dewildte.capture.settings.AndroidSettingsManager
import com.dewildte.capture.settings.SettingsManager
import com.dewildte.capture.storage.AndroidStorageManager
import com.dewildte.capture.storage.StorageManager
import com.dewildte.capture.utils.Actor
import java.time.LocalDateTime

class MainActivity : ComponentActivity(), Actor {

    private val appContext: AppContextImpl = AppContextImpl(
        controller = this
    )

    private lateinit var settingsManager: SettingsManager
    private lateinit var storageManager: StorageManager
    private lateinit var aiManager: AiManager

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

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        settingsManager = AndroidSettingsManager(this)
        storageManager = AndroidStorageManager(this, lifecycleScope, appContext)
        aiManager = AndroidAiManager(this, lifecycleScope, appContext, cacheDir)

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

            is SendAiMessage -> aiManager.sendMessage(message.message, message.conversation)

            is StopAiGeneration -> aiManager.stopGeneration()

            is SelectAiStorageFolder -> aiStorageFolderSelector.launch(null)

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

            is DeleteConversationFromStorage -> {
                settingsManager.aiStorageFolderUri?.let {
                    storageManager.deleteConversation(it, message.conversationId)
                }
            }

            else -> Log.d(TAG, "MESSAGE: $message")
        }
    }

    private fun loadSelectedFile(uri: Uri) {
        storageManager.readTextFile(uri.toString())?.let { contents ->
            val path = uri.lastPathSegment?.replace("primary:", "") ?: ""
            appContext.state.tell(FileSelected(TextFile(path, contents)))
        } ?: appContext.tell(FailedToLoadSelectedFile())
    }

    private fun loadSelectedSnippetsFile(uri: Uri) {
        storageManager.readTextFile(uri.toString())?.let { contents ->
            val path = uri.lastPathSegment?.replace("primary:", "") ?: ""
            appContext.state.tell(SnippetsFileSelected(TextFile(path, contents)))
        } ?: appContext.tell(FailedToLoadSelectedSnippetsFile())
    }

    private fun loadPersistedModel() {
        settingsManager.selectedModelFileUri?.let { aiManager.initializeModel(it) }
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
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
