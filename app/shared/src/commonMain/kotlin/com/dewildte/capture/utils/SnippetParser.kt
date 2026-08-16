package com.dewildte.capture.utils

import com.dewildte.capture.data.TextFile

object SnippetParser {
    const val SNIPPET_DELIMITER = "\n\n\n"

    fun parse(file: TextFile): List<String> {
        return parse(file.contents)
    }

    fun parse(content: String): List<String> {
        return content.split(SNIPPET_DELIMITER)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
}
