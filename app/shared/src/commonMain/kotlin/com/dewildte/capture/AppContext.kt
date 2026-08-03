package com.dewildte.capture

import com.dewildte.capture.data.TextFile
import com.dewildte.capture.utils.Actor

interface AppContext : Actor {
    val showLoading: Boolean
    val backNavigationEnabled: Boolean
    val error: Throwable?
    val state: AppState
    val stateStack: List<AppState>

    val controller: Actor

    override fun tell(message: Any) {
        /* no-op */
    }
}

interface MutableAppContext: AppContext {
    override var showLoading: Boolean
    override var backNavigationEnabled: Boolean
    override var error: Throwable?
    override var state: AppState
    override var controller: Actor
    override val stateStack: MutableList<AppState>
}