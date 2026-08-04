package com.dewildte.capture.ai

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.dewildte.capture.data.Conversation
import com.dewildte.capture.data.MessageRole
import com.dewildte.capture.events.*
import com.dewildte.capture.utils.Actor
import com.google.ai.edge.litertlm.*
import com.google.ai.edge.litertlm.Message as LmMessage
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File

class AndroidAiManager(
    private val context: Context,
    private val scope: CoroutineScope,
    private val uiActor: Actor,
    private val cacheDir: File
) : AiManager {

    private lateinit var engine: Engine
    private val engineMutex = Mutex()
    private var aiJob: Job? = null
    private var aiConversation: com.google.ai.edge.litertlm.Conversation? = null
    private var currentAiConversationId: String? = null

    override fun initializeModel(uri: String) {
        val modelUri = Uri.parse(uri)
        scope.launch(Dispatchers.IO) {
            engineMutex.withLock {
                val name = modelUri.queryFileName()
                val destination = File(cacheDir, name)
                try {
                    uiActor.tell(ModelInitializationStarted)
                    context.contentResolver.openInputStream(modelUri)?.use { inputStream ->
                        destination.outputStream().use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }

                    Log.i(TAG, "Model file copied: ${destination.absolutePath} (${destination.length()} bytes)")

                    if (::engine.isInitialized) {
                        Log.d(TAG, "Closing existing AI Engine")
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

                    Log.i(TAG, "AI Engine initialized successfully with model: $name")
                    uiActor.tell(ModelInitializationSuccess(name))

                } catch (cause: Throwable) {
                    Log.e(TAG, "Failed to initialize AI Engine: ${cause.message}")
                    uiActor.tell(ModelInitializationFailed(cause.message ?: "Unknown initialization error"))
                    if (destination.exists()) {
                        destination.delete()
                    }
                }
            }
        }
    }

    override fun sendMessage(message: String, conversation: Conversation) {
        Log.i(TAG, "Sending AI message: ${message.take(50)}...")
        if (!::engine.isInitialized) {
            Log.e(TAG, "AI Engine not initialized")
            uiActor.tell(AiResponseError(IllegalStateException("AI Engine not initialized. Please select a model file.")))
            return
        }

        aiJob?.cancel()
        aiJob = scope.launch(Dispatchers.IO) {
            try {
                if (aiConversation == null || currentAiConversationId != conversation.id) {
                    aiConversation?.close()
                    
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
                    Log.d(TAG, "AI Conversation (re)initialized for: ${conversation.id} with history size: ${history.size}")
                }

                aiConversation?.let { convo ->
                    convo.sendMessageAsync(message).collect { chunk ->
                        val textChunk = (chunk.contents.contents.firstOrNull() as? com.google.ai.edge.litertlm.Content.Text)?.text ?: ""
                        uiActor.tell(AiResponseChunk(textChunk))
                    }
                    uiActor.tell(AiResponseComplete)
                }
            } catch (cause: Throwable) {
                uiActor.tell(AiResponseError(cause))
            } finally {
                aiJob = null
            }
        }
    }

    override fun stopGeneration() {
        Log.i(TAG, "Stopping AI generation")
        aiJob?.cancel()
        aiJob = null
        uiActor.tell(AiResponseComplete)
    }

    override fun close() {
        if (::engine.isInitialized) {
            try {
                engine.close()
                Log.i(TAG, "AI Engine closed")
            } catch (e: Exception) {
                Log.e(TAG, "Error closing engine", e)
            }
        }
    }

    private fun Uri.queryFileName(): String {
        var result = "unknown_model.litertlm"
        if (this.scheme == "content") {
            context.contentResolver.query(this, null, null, null, null)?.use { cursor ->
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

    companion object {
        private const val TAG = "AndroidAiManager"
    }
}
