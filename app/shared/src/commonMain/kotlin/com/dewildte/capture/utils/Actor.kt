package com.dewildte.capture.utils

import androidx.compose.runtime.Stable

@Stable
fun interface Actor {
    fun tell(message: Any)
}