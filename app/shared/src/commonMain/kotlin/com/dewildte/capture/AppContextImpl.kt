package com.dewildte.capture

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.dewildte.capture.commands.Command
import com.dewildte.capture.commands.SetContext
import com.dewildte.capture.commands.Start
import com.dewildte.capture.commands.TransitionToState
import com.dewildte.capture.events.*
import com.dewildte.capture.queries.Query
import com.dewildte.capture.utils.Actor
import com.dewildte.capture.utils.tellDebugLog

@Stable
class AppContextImpl(
    error: Throwable? = null,
    showLoading: Boolean = true,
    state: AppState = InitialStateImpl(),
    controller: Actor = {},
) : MutableAppContext, Actor {

    override var showLoading: Boolean by mutableStateOf(showLoading)
    override var backNavigationEnabled: Boolean by mutableStateOf(false)
    override var error: Throwable? by mutableStateOf(error)
    override var state: AppState by mutableStateOf(state)
    override var stateStack: MutableList<AppState> = mutableStateListOf(state)
    override var controller: Actor by mutableStateOf(controller)

    override var isAiModelLoading: Boolean by mutableStateOf(false)
    override var isAiModelReady: Boolean by mutableStateOf(false)
    override var selectedAiModelName: String? by mutableStateOf(null)
    override var aiModelError: String? by mutableStateOf(null)

    override fun tell(message: Any) {
        when (message) {
            is Event -> {
                handleEvent(message)
            }
            is Command -> {
                handleCommand(message)
            }
            is Query -> {
                handleQuery(message)
            }
        }
    }

    private fun handleEvent(event: Event) {
        when (event) {
            is ModelInitializationStarted -> {
                isAiModelLoading = true
                isAiModelReady = false
                aiModelError = null
            }
            is ModelInitializationSuccess -> {
                isAiModelLoading = false
                isAiModelReady = true
                selectedAiModelName = event.name
                aiModelError = null
            }
            is ModelInitializationFailed -> {
                isAiModelLoading = false
                isAiModelReady = false
                aiModelError = event.error
            }
            else -> {
                /* no-op */
            }
        }
        state.tell(event)
    }

    private fun handleCommand(command: Command) {
        when (command) {
            is Start -> {
                state.tell(SetContext(this))
                state.tell(Start)
            }
            is TransitionToState -> {
                controller.tellDebugLog(
                    tag = TAG,
                    message = "$command"
                )
                command.newState.tell(SetContext(this))
                command.newState.tell(Start)
            }
            else -> state.tell(command)
        }
    }

    private fun handleQuery(query: Query) {
        state.tell(query)
    }

    companion object {
        private const val TAG = "AppContext"
    }

}

@Composable
fun rememberAppContext(
    error: Throwable? = null,
    state: AppState = InitialStateImpl(),
    controller: Actor = {}
): AppContextImpl {
    return remember {
        AppContextImpl(
            error = error,
            state = state,
            controller = controller,
        )
    }
}

