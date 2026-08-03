package com.dewildte.capture.commands

import com.dewildte.capture.AppState

class TransitionToState(
    val newState: AppState
): Command