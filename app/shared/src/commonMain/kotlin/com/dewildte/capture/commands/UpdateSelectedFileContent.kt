package com.dewildte.capture.commands

data class UpdateSelectedFileContent(
    val newContent: CharSequence,
): Command