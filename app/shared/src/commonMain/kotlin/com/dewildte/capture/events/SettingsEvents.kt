package com.dewildte.capture.events

sealed interface SettingsEvent : Event
data class AddMcpServerClicked(val url: String) : SettingsEvent
data class RemoveMcpServerClicked(val url: String) : SettingsEvent
data class ToggleSearchToolClicked(val enabled: Boolean) : SettingsEvent

enum class WorkspaceTool { DRIVE, CALENDAR, GMAIL, TASKS, DOCS, SHEETS, SLIDES }
data class ToggleWorkspaceToolClicked(val tool: WorkspaceTool, val enabled: Boolean) : SettingsEvent

data class UpdateGoogleClientIdClicked(val clientId: String) : SettingsEvent

data object SignInWithGoogleClicked : SettingsEvent
data object SignOutWithGoogleClicked : SettingsEvent
data class GoogleAuthenticated(val email: String) : SettingsEvent
data object GoogleSignedOut : SettingsEvent
