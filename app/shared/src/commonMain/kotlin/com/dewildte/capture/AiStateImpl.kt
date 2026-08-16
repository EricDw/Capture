package com.dewildte.capture

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dewildte.capture.commands.*
import com.dewildte.capture.data.Conversation
import com.dewildte.capture.data.Message
import com.dewildte.capture.data.MessageRole
import com.dewildte.capture.data.ModelInfo
import com.dewildte.capture.events.*
import com.dewildte.capture.utils.tellDebugLog
import com.dewildte.capture.utils.tellInfoLog
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Stable
class AiStateImpl(
    conversations: List<Conversation> = emptyList(),
    currentConversation: Conversation? = null,
    currentMessage: String = "",
    isGenerating: Boolean = false,
) : AiState {

    private var context: MutableAppContext? = null
    private var previousState: AppState? = null

    override val conversations: MutableList<Conversation> = mutableStateListOf(*conversations.toTypedArray())
    override var currentConversation: Conversation? by mutableStateOf(currentConversation)
    override var currentMessage: String by mutableStateOf(currentMessage)
    override var isGenerating: Boolean by mutableStateOf(isGenerating)

    private var inferenceError: String? by mutableStateOf(null)

    override val selectedModelName: String? get() = context?.selectedAiModelName
    override val availableModels: List<ModelInfo> get() = context?.availableAiModels ?: emptyList()
    override val isModelLoading: Boolean get() = context?.isAiModelLoading ?: false
    override val isModelReady: Boolean get() = context?.isAiModelReady ?: false
    override val error: String? get() = context?.aiModelError ?: inferenceError
    override val activeToolName: String? get() = context?.activeToolName
    override val tokenUsage: Int? get() = context?.tokenUsage

    override fun tell(message: Any) {
        when (message) {
            is SetContext -> {
                context = message.context
                if (message.context.state != this) {
                    previousState = message.context.state
                }
            }

            is Start -> {
                context?.let { ctx ->
                    ctx.tellInfoLog(TAG, "Starting AiState")
                    ctx.state = this@AiStateImpl
                    ctx.showLoading = false
                    ctx.tell(LoadConversationsFromStorage)
                    ctx.tell(LoadAvailableModels)
                }
            }

            is AiContentEvent -> {
                handleAiContentEvent(message)
            }

            is AiResponseEvent -> {
                handleAiResponseEvent(message)
            }

            is StorageEvent -> {
                handleStorageEvent(message)
            }

            is NavigationEvent -> {
                handleNavigationEvent(message)
            }
        }
    }

    private fun handleStorageEvent(event: StorageEvent) {
        val ctx = context ?: return
        when (event) {
            is ConversationsLoaded -> {
                ctx.tellDebugLog(TAG, "Loaded ${event.conversations.size} conversations")
                conversations.clear()
                conversations.addAll(event.conversations)
            }
            is AiStorageFolderSelected -> {
                ctx.tellInfoLog(TAG, "Storage folder selected: ${event.folderPath}")
            }
            is FailedToSelectStorageFolder -> {
                ctx.tellDebugLog(TAG, "Failed to select storage folder")
            }
        }
    }

    private fun handleNavigationEvent(event: NavigationEvent) {
        val ctx = context ?: return
        ctx.tellDebugLog(TAG, "Navigation event: $event")
        when (event) {
            is EditorTabClicked -> {
                // Handled in AppContextImpl
            }

            is MenuTabClicked -> {
                ctx.tell(SelectTextFile)
            }

            is AiTabClicked -> {
                // Already here
            }

            else -> {
                // Handled in AppContextImpl
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun handleAiResponseEvent(event: AiResponseEvent) {
        val ctx = context ?: return
        when (event) {
            is AiResponseChunk -> {
                // Chunk logging is skipped to avoid noise, or sampled
                val conversation = currentConversation ?: return
                val lastMessage = conversation.messages.lastOrNull() ?: return
                if (lastMessage.role == MessageRole.AI) {
                    val updatedMessage = lastMessage.copy(
                        content = lastMessage.content + event.content,
                        thinking = if (event.thinking != null) {
                            (lastMessage.thinking ?: "") + event.thinking
                        } else {
                            lastMessage.thinking
                        }
                    )
                    
                    // Log for debugging "stuck" responses
                    if (event.content.isNotEmpty()) {
                        ctx.tellDebugLog(TAG, "Added ${event.content.length} chars to message")
                    }
                    
                    val updatedConversation = conversation.copy(
                        messages = conversation.messages.dropLast(1) + updatedMessage,
                        updatedAt = Clock.System.now().toEpochMilliseconds()
                    )
                    
                    // Update in list
                    val index = conversations.indexOfFirst { it.id == updatedConversation.id }
                    if (index >= 0) {
                        conversations[index] = updatedConversation
                    }
                    currentConversation = updatedConversation
                    ctx.tell(SaveConversationToStorage(updatedConversation))
                }
            }

            is AiResponseComplete -> {
                ctx.tellInfoLog(TAG, "AI response complete")
                isGenerating = false
            }

            is AiResponseError -> {
                ctx.tellDebugLog(TAG, "AI response error: ${event.error.message}")
                isGenerating = false
                inferenceError = event.error.message
                
                // Remove the "ghost" empty AI message if it was just added
                currentConversation?.let { conversation ->
                    if (conversation.messages.lastOrNull()?.role == MessageRole.AI && 
                        conversation.messages.lastOrNull()?.content?.isEmpty() == true) {
                        val updatedConversation = conversation.copy(
                            messages = conversation.messages.dropLast(1)
                        )
                        val index = conversations.indexOfFirst { it.id == updatedConversation.id }
                        if (index >= 0) {
                            conversations[index] = updatedConversation
                        }
                        currentConversation = updatedConversation
                    }
                }
            }

            is ModelInitializationStarted -> {
                // Global status handled in AppContext
            }

            is ModelInitializationSuccess -> {
                // Global status handled in AppContext
            }

            is ModelInitializationFailed -> {
                // Global status handled in AppContext
            }

            is AiToolCallStarted, is AiToolCallFinished, is AiToolCallPermissionRequest -> {
                // Handled in AppContext for global state
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun handleAiContentEvent(event: AiContentEvent) {
        val ctx = context ?: return
        ctx.tellDebugLog(TAG, "AI content event: $event")
        when (event) {
            is NewConversationClicked -> {
                ctx.tellInfoLog(TAG, "Creating new conversation")
                ctx.tokenUsage = null
                val now = Clock.System.now().toEpochMilliseconds()
                val newConversation = Conversation(
                    id = Uuid.random().toString(),
                    title = "New Conversation",
                    messages = emptyList(),
                    createdAt = now,
                    updatedAt = now,
                )
                conversations.add(0, newConversation)
                currentConversation = newConversation
                currentMessage = ""
            }

            is ConversationClicked -> {
                ctx.tokenUsage = null
                currentConversation = conversations.find { it.id == event.conversationId }
                currentMessage = ""
            }

            is DeleteConversationClicked -> {
                ctx.tellInfoLog(TAG, "Deleting conversation: ${event.conversationId}")
                conversations.removeAll { it.id == event.conversationId }
                if (currentConversation?.id == event.conversationId) {
                    currentConversation = null
                }
                ctx.tell(DeleteConversationFromStorage(event.conversationId))
            }

            is MessageInputChanged -> {
                currentMessage = event.text
            }

            is SendMessageClicked -> {
                if (currentMessage.isNotBlank() && currentConversation != null && isModelReady) {
                    ctx.tellInfoLog(TAG, "Sending message")
                    inferenceError = null
                    val now = Clock.System.now().toEpochMilliseconds()
                    val userMessage = Message(
                        id = Uuid.random().toString(),
                        role = MessageRole.USER,
                        content = currentMessage.trim(),
                        timestamp = now,
                    )

                    val aiMessage = Message(
                        id = Uuid.random().toString(),
                        role = MessageRole.AI,
                        content = "",
                        timestamp = now + 1,
                    )

                    val updatedConversation = currentConversation!!.copy(
                        messages = currentConversation!!.messages + userMessage + aiMessage,
                        updatedAt = now,
                        title = if (currentConversation!!.messages.isEmpty()) {
                            // Use first message as title (truncated)
                            currentMessage.trim().take(30).let {
                                if (currentMessage.length > 30) "$it..." else it
                            }
                        } else {
                            currentConversation!!.title
                        }
                    )

                    // Update in list
                    val index = conversations.indexOfFirst { it.id == updatedConversation.id }
                    if (index >= 0) {
                        conversations[index] = updatedConversation
                    }
                    currentConversation = updatedConversation
                    
                    val messageToSend = currentMessage
                    currentMessage = ""
                    isGenerating = true
                    ctx.tell(SendAiMessage(messageToSend, updatedConversation))
                    ctx.tell(SaveConversationToStorage(updatedConversation))
                }
            }

            is StopGeneratingClicked -> {
                ctx.tellInfoLog(TAG, "Stopping AI generation")
                isGenerating = false
                ctx.tell(StopAiGeneration)
            }

            is BackToConversationsClicked -> {
                currentConversation = null
                currentMessage = ""
            }

            is OpenFileRequested -> {
                ctx.tell(TransitionToState(ctx.editorState!!))
            }

            is SelectStorageFolderClicked -> {
                ctx.tell(SelectAiStorageFolder)
            }

            is SelectModelClicked -> {
                ctx.tell(SelectModelFile)
            }

            is SwitchModelClicked -> {
                ctx.tell(SwitchModel(event.model))
            }

            is DeleteModelClicked -> {
                ctx.tell(DeleteModel(event.model))
            }

            is ModelSelected -> {
                ctx.tellInfoLog(TAG, "Model selected: ${event.name}")
                // Global status will be updated via initialization events
            }
        }
    }

    companion object {
        private const val TAG = "AiState"
    }
}
