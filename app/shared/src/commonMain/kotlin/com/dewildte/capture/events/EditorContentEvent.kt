package com.dewildte.capture.events

sealed interface EditorContentEvent : Event

data object FindInPageClicked : EditorContentEvent
data object InsertSnippetClicked : EditorContentEvent
data object MoreMenuClicked : EditorContentEvent
data object MoreMenuDismissRequested : EditorContentEvent
data object SelectFileClicked : EditorContentEvent
data object SelectSnippetsFileClicked : EditorContentEvent
data object SettingsClicked : EditorContentEvent

data object SnippetSelectorDismissRequested : EditorContentEvent

data object SnippetInserted : EditorContentEvent

data class FileTextChanged(
    val newText: String
) : EditorContentEvent
data class SearchTermChanged(
    val newSearchTerm: String,
) : EditorContentEvent

data class SnippetClicked(
    val snippet: String
): EditorContentEvent
