package com.dewildte.capture.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.dewildte.capture.data.ModelInfo
import kotlinx.serialization.json.Json

class AndroidSettingsManager(context: Context) : SettingsManager {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override var selectedFileUri: String?
        get() = prefs.getString(KEY_SELECTED_FILE_URI, null)
        set(value) = prefs.edit { putString(KEY_SELECTED_FILE_URI, value) }

    override var selectedSnippetsFileUri: String?
        get() = prefs.getString(KEY_SELECTED_SNIPPETS_FILE_URI, null)
        set(value) = prefs.edit { putString(KEY_SELECTED_SNIPPETS_FILE_URI, value) }

    override var selectedModelFileUri: String?
        get() = prefs.getString(KEY_SELECTED_MODEL_FILE_URI, null)
        set(value) = prefs.edit { putString(KEY_SELECTED_MODEL_FILE_URI, value) }

    override var aiStorageFolderUri: String?
        get() = prefs.getString(KEY_AI_STORAGE_FOLDER_URI, null)
        set(value) = prefs.edit { putString(KEY_AI_STORAGE_FOLDER_URI, value) }

    override var workspaceFolderUri: String?
        get() = prefs.getString(KEY_WORKSPACE_FOLDER_URI, null)
        set(value) = prefs.edit { putString(KEY_WORKSPACE_FOLDER_URI, value) }

    override var mcpServers: List<String>
        get() {
            val json = prefs.getString(KEY_MCP_SERVERS, null) ?: return emptyList()
            return try {
                Json.decodeFromString(json)
            } catch (e: Exception) {
                emptyList()
            }
        }
        set(value) {
            val json = Json.encodeToString(value)
            prefs.edit { putString(KEY_MCP_SERVERS, json) }
        }

    override var searchToolEnabled: Boolean
        get() = prefs.getBoolean(KEY_SEARCH_TOOL_ENABLED, true)
        set(value) = prefs.edit { putBoolean(KEY_SEARCH_TOOL_ENABLED, value) }

    override var googleDriveEnabled: Boolean
        get() = prefs.getBoolean(KEY_GOOGLE_DRIVE_ENABLED, true)
        set(value) = prefs.edit { putBoolean(KEY_GOOGLE_DRIVE_ENABLED, value) }

    override var googleCalendarEnabled: Boolean
        get() = prefs.getBoolean(KEY_GOOGLE_CALENDAR_ENABLED, true)
        set(value) = prefs.edit { putBoolean(KEY_GOOGLE_CALENDAR_ENABLED, value) }

    override var googleGmailEnabled: Boolean
        get() = prefs.getBoolean(KEY_GOOGLE_GMAIL_ENABLED, true)
        set(value) = prefs.edit { putBoolean(KEY_GOOGLE_GMAIL_ENABLED, value) }

    override var googleTasksEnabled: Boolean
        get() = prefs.getBoolean(KEY_GOOGLE_TASKS_ENABLED, true)
        set(value) = prefs.edit { putBoolean(KEY_GOOGLE_TASKS_ENABLED, value) }

    override var googleDocsEnabled: Boolean
        get() = prefs.getBoolean(KEY_GOOGLE_DOCS_ENABLED, true)
        set(value) = prefs.edit { putBoolean(KEY_GOOGLE_DOCS_ENABLED, value) }

    override var googleSheetsEnabled: Boolean
        get() = prefs.getBoolean(KEY_GOOGLE_SHEETS_ENABLED, true)
        set(value) = prefs.edit { putBoolean(KEY_GOOGLE_SHEETS_ENABLED, value) }

    override var googleSlidesEnabled: Boolean
        get() = prefs.getBoolean(KEY_GOOGLE_SLIDES_ENABLED, true)
        set(value) = prefs.edit { putBoolean(KEY_GOOGLE_SLIDES_ENABLED, value) }

    override var googleClientId: String?
        get() = prefs.getString(KEY_GOOGLE_CLIENT_ID, null)
        set(value) = prefs.edit { putString(KEY_GOOGLE_CLIENT_ID, value) }

    override var googleUserEmail: String?
        get() = prefs.getString(KEY_GOOGLE_USER_EMAIL, null)
        set(value) = prefs.edit { putString(KEY_GOOGLE_USER_EMAIL, value) }

    override var isGoogleAuthenticated: Boolean
        get() = prefs.getBoolean(KEY_IS_GOOGLE_AUTHENTICATED, false)
        set(value) = prefs.edit { putBoolean(KEY_IS_GOOGLE_AUTHENTICATED, value) }

    override var availableModels: List<ModelInfo>
        get() {
            val json = prefs.getString(KEY_AVAILABLE_MODELS, null) ?: return emptyList()
            return try {
                Json.decodeFromString(json)
            } catch (e: Exception) {
                emptyList()
            }
        }
        set(value) {
            val json = Json.encodeToString(value)
            prefs.edit { putString(KEY_AVAILABLE_MODELS, json) }
        }

    companion object {
        private const val PREFS_NAME = "MainActivity" // Keep compatible with existing prefs
        private const val KEY_SELECTED_FILE_URI = "key_selected_file_uri"
        private const val KEY_SELECTED_SNIPPETS_FILE_URI = "key_selected_snippets_file_uri"
        private const val KEY_SELECTED_MODEL_FILE_URI = "key_selected_model_file_uri"
        private const val KEY_AI_STORAGE_FOLDER_URI = "key_ai_storage_folder_uri"
        private const val KEY_WORKSPACE_FOLDER_URI = "key_workspace_folder_uri"
        private const val KEY_AVAILABLE_MODELS = "key_available_models"
        private const val KEY_MCP_SERVERS = "key_mcp_servers"
        private const val KEY_SEARCH_TOOL_ENABLED = "key_search_tool_enabled"
        private const val KEY_GOOGLE_DRIVE_ENABLED = "key_google_drive_enabled"
        private const val KEY_GOOGLE_CALENDAR_ENABLED = "key_google_calendar_enabled"
        private const val KEY_GOOGLE_GMAIL_ENABLED = "key_google_gmail_enabled"
        private const val KEY_GOOGLE_TASKS_ENABLED = "key_google_tasks_enabled"
        private const val KEY_GOOGLE_DOCS_ENABLED = "key_google_docs_enabled"
        private const val KEY_GOOGLE_SHEETS_ENABLED = "key_google_sheets_enabled"
        private const val KEY_GOOGLE_SLIDES_ENABLED = "key_google_slides_enabled"
        private const val KEY_GOOGLE_CLIENT_ID = "key_google_client_id"
        private const val KEY_GOOGLE_USER_EMAIL = "key_google_user_email"
        private const val KEY_IS_GOOGLE_AUTHENTICATED = "key_is_google_authenticated"
    }
}
