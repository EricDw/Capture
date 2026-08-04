package com.dewildte.capture.events

import com.dewildte.capture.data.ModelInfo

data class SwitchModelClicked(val model: ModelInfo) : AiContentEvent
data class DeleteModelClicked(val model: ModelInfo) : AiContentEvent
data class AvailableModelsLoaded(val models: List<ModelInfo>) : Event
