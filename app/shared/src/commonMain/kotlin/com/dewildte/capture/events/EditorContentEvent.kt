package com.dewildte.capture.events

sealed interface EditorContentEvent : Event

data object FindInPageClicked : EditorContentEvent
data object InsertSnippetClicked : EditorContentEvent
data object MoreMenuClicked : EditorContentEvent
data object MoreMenuDismissRequested : EditorContentEvent
data object SelectFileClicked : EditorContentEvent
data object SelectSnippetsFileClicked : EditorContentEvent
data object SettingsClicked : EditorContentEvent
data object NewFileClicked : EditorContentEvent

data object SnippetSelectorDismissRequested : EditorContentEvent

data object SnippetInserted : EditorContentEvent

data class FileTextChanged(
    val newText: String
) : EditorContentEvent
data class TitleChanged(
    val newTitle: String
) : EditorContentEvent
data class SearchTermChanged(
    val newSearchTerm: String,
) : EditorContentEvent

data class SnippetClicked(
    val snippet: String
): EditorContentEvent

data class SetIsNewFile(
    val isNew: Boolean
) : EditorContentEvent

data class RenameFileRequested(val newTitle: String) : EditorContentEvent
data class SaveFileRequested(val contents: String) : EditorContentEvent
