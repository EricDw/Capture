package com.dewildte.capture.ai

import com.dewildte.capture.AppContext
import com.dewildte.capture.events.FileTextChanged
import com.google.ai.edge.litertlm.Tool
import com.google.ai.edge.litertlm.ToolParam
import com.google.ai.edge.litertlm.ToolSet

class EditorTools(private val appContext: AppContext) : ToolSet {

    @Tool(description = "Reads the content of the currently open file in the editor.")
    fun readCurrentFileContent(): String {
        return appContext.editorState?.textFile?.contents ?: "No file is currently open in the editor."
    }

    @Tool(description = "Writes new content to the currently open file in the editor, completely overwriting it.")
    fun writeToCurrentFile(
        @ToolParam(description = "The new text content to write into the file.")
        content: String
    ): String {
        val editor = appContext.editorState
        return if (editor != null) {
            editor.tell(FileTextChanged(content))
            "Successfully updated the file content."
        } else {
            "Failed to update file: No file is currently open."
        }
    }
}
