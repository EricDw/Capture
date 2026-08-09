package com.dewildte.capture

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dewildte.capture.commands.*
import com.dewildte.capture.data.TextFile
import com.dewildte.capture.events.*
import com.dewildte.capture.navigation.AppRoute
import com.dewildte.capture.queries.GetCurrentDateString
import com.dewildte.capture.utils.tellErrorLog

@Stable
class EditorStateImpl(
    textFile: TextFile = TextFile(),
    searchMode: Boolean = false,
    searchTerm: String = "",
    moreMenuExpanded: Boolean = false,
    snippetSelectorExpanded: Boolean = false,
    snippetSelectorIsEmpty: Boolean = true,
    snippets: List<String> = emptyList()
) : EditorState {

    private lateinit var context: MutableAppContext
    private lateinit var previousState: AppState

    override var textFile: TextFile by mutableStateOf(textFile)
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
                previousState = context.state
            }

            is Start -> {
                context.apply {
                    backNavigationEnabled = false
                    state = this@EditorStateImpl
                    showLoading = false
                }

                if (snippetsFile == null)
                    context.controller.tell(LoadSnippetsFile)
            }

            is EditorContentEvent -> {
                handleEditorContentEvent(message)
            }

            is FileSelected -> {
                textFile = message.textFile
            }

            is SnippetsFileSelected -> {
                snippetsFile = message.textFile
                val newSnippets = message.textFile.contents.split("\n\n\n")
                snippets.clear()
                snippets.addAll(newSnippets)
                snippetSelectorIsEmpty = false
            }

            is SnippetsFileLoaded -> {
                snippetsFile = message.file
                val newSnippets = message.file.contents.split("\n\n\n")
                snippets.clear()
                snippets.addAll(newSnippets)
                snippetSelectorIsEmpty = false
            }

            is NavigationEvent -> {
                handleNavigationEvent(message)
            }

            is FailedToUpdateFileContent -> {
                context.controller.tellErrorLog(
                    tag = TAG,
                    message = message.toString(),
                    error = message.cause,
                )
            }

            is FailedToLoadSelectedFile -> {
                context.controller.tellErrorLog(
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

    private fun handleNavigationEvent(event: NavigationEvent) {
        when (event) {
            is AiTabClicked -> {
                if (!context.navBackStack.contains(AppRoute.AiAssistant)) {
                    context.navBackStack.add(AppRoute.AiAssistant)
                }
            }

            is MenuTabClicked -> {
                context.controller.tell(SelectTextFile)
            }

            is EditorTabClicked -> {
                // Already here
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
                context.controller.tell(LoadSnippetsFile)
            }

            is MoreMenuDismissRequested -> {
                moreMenuExpanded = false
            }

            is SelectFileClicked -> {
                moreMenuExpanded = false
                context.controller.tell(SelectTextFile)
            }

            is SelectSnippetsFileClicked -> {
                moreMenuExpanded = false
                context.controller.tell(SelectSnippetsFile())
            }

            is SettingsClicked -> {
                moreMenuExpanded = false
                context.tell(TransitionToState(SettingsStateImpl()))
            }

            is SearchTermChanged -> {
                searchTerm = event.newSearchTerm
            }

            is FileTextChanged -> {
                val (newText) = event
                if (textFile.contents != newText) {
                    context.controller.tell(UpdateSelectedFileContent(newText))
                    textFile = textFile.copy(contents = newText)
                }
            }

            is SnippetSelectorDismissRequested -> {
                snippetSelectorExpanded = false
            }

            is SnippetClicked -> {
                // TODO: Replace $DATE$ with date
                val (snippet) = event
                context.controller.tell(
                    GetCurrentDateString { dateString ->
                        snippetToInsert = snippet.replace($$"$DATE$", dateString)
                        snippetSelectorExpanded = false
                    }
                )

            }

            is SnippetInserted -> {
                snippetToInsert = null
            }
        }
    }

    companion object {
        private const val TAG = "EditorState"
    }
}