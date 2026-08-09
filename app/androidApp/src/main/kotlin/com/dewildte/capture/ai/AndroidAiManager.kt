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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
    
    private val toolProviders = mutableListOf<ToolProvider>()

    suspend fun requestPermission(toolName: String, arguments: String): PermissionResult {
        val deferred = CompletableDeferred<PermissionResult>()
        uiActor.tell(AiToolCallPermissionRequest(toolName, arguments, deferred))
        return deferred.await()
    }

    fun reportProgress(toolName: String, started: Boolean, success: Boolean = true) {
        if (started) {
            uiActor.tell(AiToolCallStarted(toolName))
        } else {
            uiActor.tell(AiToolCallFinished(toolName, success))
        }
    }

    override fun registerTool(tool: Any) {
        when (tool) {
            is ToolProvider -> toolProviders.add(tool)
            is ToolSet -> toolProviders.add(tool(tool))
            else -> Log.w(TAG, "Attempted to register unsupported tool type: ${tool::class.simpleName}")
        }
    }

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
                        maxNumTokens = 8192
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

    override fun sendMessage(message: String, conversation: Conversation, systemInstruction: String?) {
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
                    
                    val currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy HH:mm:ss"))
                    val enrichedSystemInstruction = (systemInstruction ?: "") + "\n\nIMPORTANT: The current date and time is $currentDateTime. Use this for relative date calculations."
                    val system = Contents.of(enrichedSystemInstruction)
                    
                    val config = ConversationConfig(
                        systemInstruction = system,
                        initialMessages = history,
                        tools = toolProviders,
                        automaticToolCalling = true,
                        thinkingConfig = ThinkingConfig(
                            enableThinking = true,
                            thinkingTokenBudget = 4096
                        )
                    )
                    aiConversation = engine.createConversation(config)
                    currentAiConversationId = conversation.id
                    Log.d(TAG, "AI Conversation (re)initialized for: ${conversation.id} with history size: ${history.size} tools: ${toolProviders.size}")
                }

                aiConversation?.let { convo ->
                    var lastUpdate = 0L
                    var lastThinking: String? = null
                    
                    convo.sendMessageAsync(message).collect { chunk ->
                        Log.d(TAG, "Received chunk: contents=${chunk.contents.contents.size}, channels=${chunk.channels.keys}")
                        val textChunk = chunk.contents.contents
                            .filterIsInstance<Content.Text>()
                            .joinToString("") { it.text }
                        
                        val thinkingChunk = chunk.channels["thought"]
                        
                        // Check for tool calls in the chunk
                        chunk.toolCalls.forEach { toolCall ->
                            Log.d(TAG, "AI calling tool: ${toolCall.name}")
                            reportProgress(toolCall.name, started = true)
                        }

                        val now = System.currentTimeMillis()
                        // Throttling logic:
                        // 1. Text chunks are NEVER throttled.
                        // 2. Thinking chunks are throttled to 2 per second to reduce UI lag.
                        val hasNewText = textChunk.isNotEmpty()
                        val hasNewThinking = thinkingChunk != null && thinkingChunk != lastThinking
                        val shouldUpdateThinking = hasNewThinking && (now - lastUpdate > 500)
                        
                        if (hasNewText || shouldUpdateThinking) {
                            if (shouldUpdateThinking) {
                                lastUpdate = now
                            }
                            
                            val currentLastThinking = lastThinking
                            // Calculate thinking delta (channels are often cumulative)
                            val thinkingDelta = if (currentLastThinking != null && thinkingChunk?.startsWith(currentLastThinking) == true) {
                                thinkingChunk.substring(currentLastThinking.length)
                            } else {
                                thinkingChunk
                            }
                            
                            lastThinking = thinkingChunk
                            
                            // Get token count safely (throttle to 1s)
                            val tokenCount = if (now - lastUpdate > 1000 || hasNewText) {
                                convo.getTokenCount()
                            } else null
                            
                            uiActor.tell(AiResponseChunk(textChunk, thinkingDelta, tokenCount))
                        }
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
