package com.dewildte.capture

import androidx.compose.runtime.Stable
import com.dewildte.capture.commands.LoadSelectedFile
import com.dewildte.capture.commands.SetContext
import com.dewildte.capture.commands.Start
import com.dewildte.capture.commands.TransitionToState
import com.dewildte.capture.events.FailedToLoadSelectedFile
import com.dewildte.capture.events.FileSelected

@Stable
class InitialStateImpl : InitialState {
    private lateinit var context: MutableAppContext

    override fun tell(message: Any) {
        when (message) {
            is SetContext -> {
                context = message.context
            }

            is Start -> {
                context.run {
                    showLoading = true
                    controller.tell(LoadSelectedFile())
                }
            }

            is FileSelected -> {
                val editorState = context.editorState!!
                editorState.tell(message)
                context.tell(TransitionToState(editorState))
            }

            is FailedToLoadSelectedFile -> {
                context.tell(
                    TransitionToState(
                        EmptyStateImpl()
                    )
                )
            }
        }
    }
}