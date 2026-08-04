package com.dewildte.capture.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.dewildte.capture.data.ModelInfo
import kotlinx.serialization.encodeToString
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
        private const val KEY_AVAILABLE_MODELS = "key_available_models"
    }
}
