package com.dewildte.capture

import androidx.compose.runtime.Stable
import com.dewildte.capture.commands.*
import com.dewildte.capture.content.empty.SelectTextFileClicked
import com.dewildte.capture.events.*

@Stable
class EmptyStateImpl : EmptyState {

    private lateinit var appContext: MutableAppContext

    override fun tell(message: Any) {
        when (message) {
            is SetContext -> {
                appContext = message.context
            }

            is Start -> {
                appContext.apply {
                    backNavigationEnabled = false
                    showLoading = false
                    state = this@EmptyStateImpl
                }
            }

            is SelectTextFileClicked -> {
//                appContext.controller.tell(SelectModelFile)
                appContext.controller.tell(SelectTextFile)
            }

            is FileSelected -> {
                val editorState = appContext.editorState!!
                editorState.tell(message)
                appContext.tell(TransitionToState(editorState))
            }

            is NavigationEvent -> {
                handleNavigationEvent(message)
            }
        }
    }

    private fun handleNavigationEvent(event: NavigationEvent) {
        when (event) {
            is AiTabClicked -> {
                appContext.tell(TransitionToState(appContext.aiState!!))
            }

            is EditorTabClicked -> {
                // Stay here or maybe transition to EditorState with default if possible? 
                // Currently EditorState requires a file.
            }

            is MenuTabClicked -> {
                appContext.controller.tell(SelectTextFile)
            }
        }
    }
}