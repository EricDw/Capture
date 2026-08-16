package com.dewildte.capture.ai

import com.dewildte.capture.settings.SettingsManager
import com.dewildte.capture.storage.StorageManager
import com.google.ai.edge.litertlm.Tool
import com.google.ai.edge.litertlm.ToolParam
import com.google.ai.edge.litertlm.ToolSet

class SearchTools(
    private val storageManager: StorageManager,
    private val settingsManager: SettingsManager
) : ToolSet {

    @Tool(description = "Searches for a specific text string across all saved files in the storage folder.")
    fun searchFiles(
        @ToolParam(description = "The text query to search for.")
        query: String
    ): String {
        if (!settingsManager.searchToolEnabled) return "Search failed: The search tool is currently disabled in settings."
        val folderUri = settingsManager.aiStorageFolderUri ?: return "Search failed: No storage folder is selected."
        val results = storageManager.searchFiles(folderUri, query)
        
        return if (results.isEmpty()) {
            "No files found containing '$query'."
        } else {
            "Found '$query' in the following files: ${results.joinToString(", ")}"
        }
    }
}
