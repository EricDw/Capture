package com.dewildte.capture

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import com.dewildte.capture.commands.LoadSnippetsFile
import com.dewildte.capture.commands.SetContext
import com.dewildte.capture.commands.Start
import com.dewildte.capture.commands.TransitionToState
import com.dewildte.capture.data.TextFile
import com.dewildte.capture.events.BackClicked
import com.dewildte.capture.events.SnippetsFileLoaded
import com.dewildte.capture.events.SystemBackButtonClicked

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

        }
    }

    private fun parseSnippets(file: TextFile) {
        // TODO: Extract Snippets
        // TODO: Set the snippets data
    }
}