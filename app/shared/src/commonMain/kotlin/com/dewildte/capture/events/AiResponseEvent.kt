package com.dewildte.capture.events

import kotlinx.coroutines.CompletableDeferred

sealed interface AiResponseEvent : Event
data class AiResponseChunk(val content: String, val thinking: String? = null, val tokenUsage: Int? = null) : AiResponseEvent
data object AiResponseComplete : AiResponseEvent
data class AiResponseError(val error: Throwable) : AiResponseEvent

data object ModelInitializationStarted : AiResponseEvent
data class ModelInitializationSuccess(val name: String) : AiResponseEvent
data class ModelInitializationFailed(val error: String) : AiResponseEvent

// New events for Tool HIL and Progress
data class AiToolCallStarted(val toolName: String) : AiResponseEvent
data class AiToolCallFinished(val toolName: String, val success: Boolean) : AiResponseEvent

enum class PermissionResult {
    DENY,
    ALLOW_ONCE,
    ALWAYS_ALLOW
}

data class AiToolCallPermissionRequest(
    val toolName: String,
    val arguments: String,
    val result: CompletableDeferred<PermissionResult>
) : AiResponseEvent

// Event to trigger Google OAuth consent screen
data class RequestGoogleAuthResolution(val intent: Any) : Event
