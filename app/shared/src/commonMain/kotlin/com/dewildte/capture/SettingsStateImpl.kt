package com.dewildte.capture

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import com.dewildte.capture.commands.*
import com.dewildte.capture.data.TextFile
import com.dewildte.capture.events.*

@Stable
class SettingsStateImpl(
    snippets: List<String> = emptyList()
) : SettingsState {

    private lateinit var context: MutableAppContext
    private lateinit var previousState: AppState

    override var snippets: List<String> = mutableStateListOf(*snippets.toTypedArray())

    override fun tell(message: Any) {
        when (message) {
            is SetContext -> {
                previousState = message.context.state
                context = message.context
            }

            is Start -> {
                context.backNavigationEnabled = previousState != this
                context.state = this

                context.controller.tell(LoadSnippetsFile)
            }

            is SnippetsFileLoaded -> {
                // TODO: Parse the Snippets out of the file.
                parseSnippets(message.file)
            }

            is BackClicked, is SystemBackButtonClicked -> {
                context.tell(TransitionToState(previousState))
            }

            is NavigationEvent -> {
                handleNavigationEvent(message)
            }

        }
    }

    private fun handleNavigationEvent(event: NavigationEvent) {
        when (event) {
            is EditorTabClicked -> {
                context.tell(TransitionToState(EditorStateImpl()))
            }

            is AiTabClicked -> {
                context.tell(TransitionToState(AiStateImpl()))
            }

            is MenuTabClicked -> {
                context.controller.tell(SelectTextFile)
            }
        }
    }

    private fun parseSnippets(file: TextFile) {
        // TODO: Extract Snippets
        // TODO: Set the snippets data
    }
}