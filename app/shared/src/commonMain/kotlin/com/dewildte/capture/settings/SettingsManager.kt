package com.dewildte.capture.settings

import com.dewildte.capture.data.ModelInfo

interface SettingsManager {
    var selectedFileUri: String?
    var selectedSnippetsFileUri: String?
    var selectedModelFileUri: String?
    var aiStorageFolderUri: String?
    var workspaceFolderUri: String?
    var availableModels: List<ModelInfo>
    var mcpServers: List<String>
    var searchToolEnabled: Boolean
    var googleDriveEnabled: Boolean
    var googleCalendarEnabled: Boolean
    var googleGmailEnabled: Boolean
    var googleTasksEnabled: Boolean
    var googleDocsEnabled: Boolean
    var googleSheetsEnabled: Boolean
    var googleSlidesEnabled: Boolean
    var googleClientId: String?
    var googleUserEmail: String?
    var isGoogleAuthenticated: Boolean
}
