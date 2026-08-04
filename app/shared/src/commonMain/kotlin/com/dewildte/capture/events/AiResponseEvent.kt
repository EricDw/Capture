package com.dewildte.capture.events

sealed interface AiResponseEvent : Event
data class AiResponseChunk(val content: String) : AiResponseEvent
data object AiResponseComplete : AiResponseEvent
data class AiResponseError(val error: Throwable) : AiResponseEvent

data object ModelInitializationStarted : AiResponseEvent
data class ModelInitializationSuccess(val name: String) : AiResponseEvent
data class ModelInitializationFailed(val error: String) : AiResponseEvent
