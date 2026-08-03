package com.dewildte.capture.events

import com.dewildte.capture.data.TextFile

data class SnippetsFileLoaded(
    val file: TextFile
): Event