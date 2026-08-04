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

    private lateinit var context: MutableAppContext
    private var previousState: AppState? = null

    override val conversations: MutableList<Conversation> = mutableStateListOf(*conversations.toTypedArray())
    override var currentConversation: Conversation? by mutableStateOf(currentConversation)
    override var currentMessage: String by mutableStateOf(currentMessage)
    override var isGenerating: Boolean by mutableStateOf(isGenerating)

    private var inferenceError: String? by mutableStateOf(null)

    override val selectedModelName: String? get() = context.selectedAiModelName
    override val isModelLoading: Boolean get() = context.isAiModelLoading
    override val isModelReady: Boolean get() = context.isAiModelReady
    override val error: String? get() = context.aiModelError ?: inferenceError

    override fun tell(message: Any) {
        when (message) {
            is SetContext -> {
                context = message.context
                if (context.state != this) {
                    previousState = context.state
                }
            }

            is Start -> {
                context.controller.tellInfoLog(TAG, "Starting AiState")
                context.apply {
                    backNavigationEnabled = false
                    state = this@AiStateImpl
                    showLoading = false
                }
                context.controller.tell(LoadConversationsFromStorage)
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
        when (event) {
            is ConversationsLoaded -> {
                context.controller.tellDebugLog(TAG, "Loaded ${event.conversations.size} conversations")
                conversations.clear()
                conversations.addAll(event.conversations)
            }
            is AiStorageFolderSelected -> {
                context.controller.tellInfoLog(TAG, "Storage folder selected: ${event.folderPath}")
            }
            is FailedToSelectStorageFolder -> {
                context.controller.tellDebugLog(TAG, "Failed to select storage folder")
            }
        }
    }

    private fun handleNavigationEvent(event: NavigationEvent) {
        context.controller.tellDebugLog(TAG, "Navigation event: $event")
        when (event) {
            is EditorTabClicked -> {
                val stateToTransitionTo = if (previousState is EditorState) {
                    previousState!!
                } else {
                    EditorStateImpl()
                }
                context.tell(TransitionToState(stateToTransitionTo))
            }

            is MenuTabClicked -> {
                context.controller.tell(SelectTextFile)
            }

            is AiTabClicked -> {
                // Already here
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun handleAiResponseEvent(event: AiResponseEvent) {
        when (event) {
            is AiResponseChunk -> {
                // Chunk logging is skipped to avoid noise, or sampled
                val conversation = currentConversation ?: return
                val lastMessage = conversation.messages.lastOrNull() ?: return
                if (lastMessage.role == MessageRole.AI) {
                    val updatedMessage = lastMessage.copy(
                        content = lastMessage.content + event.content
                    )
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
                    context.controller.tell(SaveConversationToStorage(updatedConversation))
                }
            }

            is AiResponseComplete -> {
                context.controller.tellInfoLog(TAG, "AI response complete")
                isGenerating = false
            }

            is AiResponseError -> {
                context.controller.tellDebugLog(TAG, "AI response error: ${event.error.message}")
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
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun handleAiContentEvent(event: AiContentEvent) {
        context.controller.tellDebugLog(TAG, "AI content event: $event")
        when (event) {
            is NewConversationClicked -> {
                context.controller.tellInfoLog(TAG, "Creating new conversation")
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
                currentConversation = conversations.find { it.id == event.conversationId }
                currentMessage = ""
            }

            is DeleteConversationClicked -> {
                context.controller.tellInfoLog(TAG, "Deleting conversation: ${event.conversationId}")
                conversations.removeAll { it.id == event.conversationId }
                if (currentConversation?.id == event.conversationId) {
                    currentConversation = null
                }
                context.controller.tell(DeleteConversationFromStorage(event.conversationId))
            }

            is MessageInputChanged -> {
                currentMessage = event.text
            }

            is SendMessageClicked -> {
                if (currentMessage.isNotBlank() && currentConversation != null && isModelReady) {
                    context.controller.tellInfoLog(TAG, "Sending message")
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
                    context.controller.tell(SendAiMessage(messageToSend))
                    context.controller.tell(SaveConversationToStorage(updatedConversation))
                }
            }

            is StopGeneratingClicked -> {
                context.controller.tellInfoLog(TAG, "Stopping AI generation")
                isGenerating = false
                context.controller.tell(StopAiGeneration)
            }

            is BackToConversationsClicked -> {
                currentConversation = null
                currentMessage = ""
            }

            is SelectStorageFolderClicked -> {
                context.controller.tell(SelectAiStorageFolder)
            }

            is SelectModelClicked -> {
                context.controller.tell(SelectModelFile)
            }

            is ModelSelected -> {
                context.controller.tellInfoLog(TAG, "Model selected: ${event.name}")
                // Global status will be updated via initialization events
            }
        }
    }

    companion object {
        private const val TAG = "AiState"
    }
}
