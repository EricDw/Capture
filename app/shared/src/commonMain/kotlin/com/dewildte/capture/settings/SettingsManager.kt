package com.dewildte.capture.settings

import com.dewildte.capture.data.ModelInfo

interface SettingsManager {
    var selectedFileUri: String?
    var selectedSnippetsFileUri: String?
    var selectedModelFileUri: String?
    var aiStorageFolderUri: String?
    var availableModels: List<ModelInfo>
}
