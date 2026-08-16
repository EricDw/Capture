package com.dewildte.capture.events

sealed interface NavigationEvent : Event
data object EditorTabClicked : NavigationEvent
data object AiTabClicked : NavigationEvent
data object MenuTabClicked : NavigationEvent
data object ToggleAiAssistant : NavigationEvent
data object GoBack : NavigationEvent
