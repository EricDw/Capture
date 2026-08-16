package com.dewildte.capture

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dewildte.capture.commands.*
import com.dewildte.capture.data.FileNode
import com.dewildte.capture.data.TextFile
import com.dewildte.capture.events.*
import com.dewildte.capture.queries.GetCurrentDateString
import com.dewildte.capture.utils.SnippetParser
import com.dewildte.capture.utils.tellErrorLog

@Stable
class EditorStateImpl(
    textFile: TextFile = TextFile(),
    isNewFile: Boolean = false,
    searchMode: Boolean = false,
    searchTerm: String = "",
    moreMenuExpanded: Boolean = false,
    snippetSelectorExpanded: Boolean = false,
    snippetSelectorIsEmpty: Boolean = true,
    snippets: List<String> = emptyList()
) : EditorState {

    private lateinit var context: MutableAppContext
    private var previousState: AppState? = null

    override var textFile: TextFile by mutableStateOf(textFile)
    override var isNewFile: Boolean by mutableStateOf(isNewFile)
    private var initialTitle: String = textFile.name
    private var diskName: String = textFile.name
    
    override val title: String get() = textFile.name
    override val contents: String get() = textFile.contents

    override var searchMode: Boolean by mutableStateOf(searchMode)
    override var searchTerm: String by mutableStateOf(searchTerm)
    override var moreMenuExpanded: Boolean by mutableStateOf(moreMenuExpanded)
    override var snippetSelectorExpanded: Boolean by mutableStateOf(snippetSelectorExpanded)
    override var snippetSelectorIsEmpty: Boolean by mutableStateOf(snippetSelectorIsEmpty)
    override val snippets: MutableList<String> = mutableStateListOf(*snippets.toTypedArray())

    override var snippetToInsert: String? by mutableStateOf(null)
    private var snippetsFile: TextFile? = null

    override fun tell(message: Any) {
        when (message) {

            is SetContext -> {
                context = message.context
                if (message.context.state != this) {
                    previousState = context.state
                }
            }

            is Start -> {
                context.apply {
                    state = this@EditorStateImpl
                    showLoading = false
                }

                if (snippetsFile == null)
                    context.tell(LoadSnippetsFile)
            }

            is EditorContentEvent -> {
                handleEditorContentEvent(message)
            }

            is FileSelected -> {
                textFile = message.textFile
                initialTitle = message.textFile.name
                diskName = message.textFile.name
                isNewFile = false
            }

            is SnippetsFileSelected -> {
                snippetsFile = message.textFile
                val newSnippets = SnippetParser.parse(message.textFile)
                snippets.clear()
                snippets.addAll(newSnippets)
                snippetSelectorIsEmpty = false
            }

            is SnippetsFileLoaded -> {
                snippetsFile = message.file
                val newSnippets = SnippetParser.parse(message.file)
                snippets.clear()
                snippets.addAll(newSnippets)
                snippetSelectorIsEmpty = false
            }

            is NavigationEvent -> {
                handleNavigationEvent(message)
            }

            is BackClicked, is SystemBackButtonClicked -> {
                handleBackNavigation()
            }

            is FailedToUpdateFileContent -> {
                context.tellErrorLog(
                    tag = TAG,
                    message = message.toString(),
                    error = message.cause,
                )
            }

            is FailedToLoadSelectedFile -> {
                context.tellErrorLog(
                    tag = TAG,
                    message = message.toString(),
                    error = message.cause,
                )
            }

            is FailedToLoadSelectedSnippetsFile -> {
                snippetSelectorIsEmpty = true
            }

            is FailedToSelectSnippetsFile -> {
                snippetSelectorIsEmpty = true
            }
        }
    }

    private fun handleBackNavigation() {
        if (contents.isBlank() && diskName == initialTitle && diskName.startsWith("Note_")) {
            // Auto-delete empty new note with default title
            context.tell(DeleteNodeRequested(FileNode.File(diskName, textFile.path)))
        } else {
            // Save latest content
            context.tell(UpdateSelectedFileContent(contents))
            
            // Handle rename if title changed and not yet renamed
            val finalTitle = validateAndNormalizeTitle(title)
            
            if (diskName != finalTitle && finalTitle.isNotBlank()) {
                val currentNode = FileNode.File(diskName, textFile.path)
                context.tell(RenameNodeRequested(currentNode, finalTitle))
            }
        }
    }

    private fun validateAndNormalizeTitle(input: String): String {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return diskName

        val commonExtensions = listOf(".txt", ".md", ".csv", ".json", ".log", ".litertlm")
        val hasKnownExtension = commonExtensions.any { trimmed.endsWith(it, ignoreCase = true) }
        
        // Also check for any 2-4 char extension if not in the common list
        val hasAnyExtension = hasKnownExtension || (trimmed.contains(".") && trimmed.substringAfterLast(".").length in 2..4)

        return if (hasAnyExtension) trimmed else "$trimmed.txt"
    }

    private fun handleNavigationEvent(event: NavigationEvent) {
        when (event) {
            is AiTabClicked -> {
                context.tell(ToggleAiAssistant)
            }

            is MenuTabClicked -> {
                context.tell(SelectTextFile)
            }

            is EditorTabClicked -> {
                // Already here
            }

            else -> {
                // Handled in AppContextImpl
            }
        }
    }

    private fun handleEditorContentEvent(event: EditorContentEvent) {
        when (event) {

            is MoreMenuClicked -> {
                moreMenuExpanded = !moreMenuExpanded
            }

            is FindInPageClicked -> {
                moreMenuExpanded = false
            }

            is InsertSnippetClicked -> {
                moreMenuExpanded = false
                snippetSelectorExpanded = true
                context.tell(LoadSnippetsFile)
            }

            is MoreMenuDismissRequested -> {
                moreMenuExpanded = false
            }

            is NewFileClicked -> {
                // Handled in AppContextImpl
            }

            is SelectFileClicked -> {
                moreMenuExpanded = false
                context.tell(SelectTextFile)
            }

            is SelectSnippetsFileClicked -> {
                moreMenuExpanded = false
                context.tell(SelectSnippetsFile())
            }

            is SettingsClicked -> {
                moreMenuExpanded = false
                context.tell(TransitionToState(SettingsStateImpl()))
            }

            is SearchTermChanged -> {
                searchTerm = event.newSearchTerm
            }

            is TitleChanged -> {
                if (textFile.name != event.newTitle && event.newTitle.isNotBlank()) {
                    textFile = textFile.copy(name = event.newTitle)
                }
            }

            is RenameFileRequested -> {
                val newTitle = validateAndNormalizeTitle(event.newTitle)

                if (diskName != newTitle && newTitle.isNotBlank()) {
                    val currentNode = FileNode.File(diskName, textFile.path)
                    val finalTitle = newTitle
                    context.tell(RenameNodeRequested(currentNode, finalTitle) { newUri ->
                        diskName = finalTitle
                        textFile = textFile.copy(name = finalTitle, path = newUri)
                    })
                }
            }

            is FileTextChanged -> {
                if (textFile.contents != event.newText) {
                    textFile = textFile.copy(contents = event.newText)
                }
            }

            is SaveFileRequested -> {
                if (textFile.contents == event.contents) {
                    context.tell(UpdateSelectedFileContent(event.contents))
                }
            }

            is SnippetSelectorDismissRequested -> {
                snippetSelectorExpanded = false
            }

            is SnippetClicked -> {
                // TODO: Replace $DATE$ with date
                val (snippet) = event
                context.tell(
                    GetCurrentDateString { dateString ->
                        snippetToInsert = snippet.replace("\$DATE$", dateString)
                        snippetSelectorExpanded = false
                    }
                )

            }

            is SnippetInserted -> {
                snippetToInsert = null
            }

            is SetIsNewFile -> {
                isNewFile = event.isNew
            }
        }
    }

    companion object {
        private const val TAG = "EditorState"
    }
}