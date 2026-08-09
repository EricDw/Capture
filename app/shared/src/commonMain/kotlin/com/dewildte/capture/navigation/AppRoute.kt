package com.dewildte.capture.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute {
    @Serializable
    data object Editor : AppRoute
    @Serializable
    data object Settings : AppRoute
    @Serializable
    data object AiAssistant : AppRoute
}
