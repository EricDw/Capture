# Refactoring MainActivity for Better Separation of Concerns

This plan outlines the extraction of AI logic, storage management, and preference handling from `MainActivity` into a `ViewModel` and specialized manager classes.

## User Review Required

> [!IMPORTANT]
> This refactor moves almost all logic out of `MainActivity`. While functionality should remain identical, the internal wiring changes significantly.
> `ActivityResultLauncher`s must remain in the `Activity`, but their results will be passed to the `MainViewModel`.

## Proposed Changes

### [NEW] Step 0: Version Control
- Commit all current changes related to "Multiple AI Model Support".
- Push to the remote repository.

### Core Infrastructure (Android-only)

#### [NEW] [SettingsManager.kt](file:///Users/ericdewildt/Source/KotlinMultiPlatform/Capture/app/androidApp/src/main/kotlin/com/dewildte/capture/SettingsManager.kt)
Extract `SharedPreferences` logic.
- Methods for getting/setting URIs (Selected File, Snippets, AI Storage, Model).
- Methods for getting/setting the list of `ModelInfo`.

#### [NEW] [AndroidStorageManager.kt](file:///Users/ericdewildt/Source/KotlinMultiPlatform/Capture/app/androidApp/src/main/kotlin/com/dewildte/capture/storage/AndroidStorageManager.kt)
Extract file I/O logic.
- Handle `ContentResolver` operations for reading/writing text files.
- Handle `DocumentFile` operations for conversation persistence.
- Parsing and serialization of conversation files.

#### [NEW] [AndroidAiManager.kt](file:///Users/ericdewildt/Source/KotlinMultiPlatform/Capture/app/androidApp/src/main/kotlin/com/dewildte/capture/ai/AndroidAiManager.kt)
Extract LiteRT logic.
- Manage `Engine` lifecycle.
- Handle model initialization and copying files to `cacheDir`.
- Handle `SendAiMessage` and generation streaming.
- Handle `StopAiGeneration`.

### Coordination Layer

#### [NEW] [MainViewModel.kt](file:///Users/ericdewildt/Source/KotlinMultiPlatform/Capture/app/androidApp/src/main/kotlin/com/dewildte/capture/MainViewModel.kt)
The primary `Actor` for the application.
- Owns `AppContextImpl`.
- Injects `SettingsManager`, `AndroidAiManager`, and `AndroidStorageManager`.
- Implements `tell` by delegating to managers or handling app-level state transitions.
- Provides callbacks for the Activity to report picker results.

---

### Cleaned Up Activity

#### [MODIFY] [MainActivity.kt](file:///Users/ericdewildt/Source/KotlinMultiPlatform/Capture/app/androidApp/src/main/kotlin/com/dewildte/capture/MainActivity.kt)
- Remove `Engine`, `Job`, and all AI/Storage private methods.
- Remove the large `tell` implementation.
- Use `viewModels<MainViewModel>()` to obtain the controller.
- Delegate `ActivityResultLauncher` results to the ViewModel.
- Keep only Compose setup and system back handling.

## Verification Plan

### Automated Tests
- Run `:app:androidApp:assembleDebug` to ensure no regression in compilation or dependency wiring.

### Manual Verification
1. **AI Flow**: Verify model loading, message sending, and switching still works.
2. **File Flow**: Verify selecting text files and snippets still loads content into the editor.
3. **Storage Flow**: Verify conversations are still loaded from and saved to the selected storage folder.
4. **Persistence**: Verify all settings (URIs, selected model) survive app restart.
