package com.dewildte.capture

import androidx.compose.runtime.Stable
import com.dewildte.capture.commands.SelectModelFile
import com.dewildte.capture.commands.SelectTextFile
import com.dewildte.capture.commands.SetContext
import com.dewildte.capture.commands.Start
import com.dewildte.capture.commands.TransitionToState
import com.dewildte.capture.content.empty.SelectTextFileClicked
import com.dewildte.capture.events.FileSelected

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
                appContext.tell(
                    TransitionToState(
                        EditorStateImpl(
                            textFile = message.textFile
                        )
                    )
                )
            }
        }
    }
}