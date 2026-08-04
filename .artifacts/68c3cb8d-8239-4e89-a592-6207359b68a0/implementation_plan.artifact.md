# Refactoring AppContext to Support Sibling State Trees

This plan refactors `AppContext` to explicitly manage `EditorState` and `AiState` as persistent sibling state trees, allowing for state preservation when switching between tabs.

## User Review Required

> [!IMPORTANT]
> - `EditorState` and `AiState` will be initialized lazily.
> - Switching tabs will no longer use the `stateStack` for navigation between roots; instead, it will swap the active `state` pointer between the pre-existing sibling trees.
> - A new architecture document will be created to record the "Tree of States" design.

## Proposed Changes

### Core Architecture (commonMain)

#### [MODIFY] [AppContext.kt](file:///Users/ericdewildt/Source/KotlinMultiPlatform/Capture/app/shared/src/commonMain/kotlin/com/dewildte/capture/AppContext.kt)
- Add `editorState: EditorState?` and `aiState: AiState?` to `AppContext`.
- Add mutable overrides to `MutableAppContext`.

#### [MODIFY] [AppContextImpl.kt](file:///Users/ericdewildt/Source/KotlinMultiPlatform/Capture/app/shared/src/commonMain/kotlin/com/dewildte/capture/AppContextImpl.kt)
- Implement `editorState` and `aiState` as lazy-initialized properties (using `mutableStateOf`).
- Ensure they are instantiated only when first requested.

#### [MODIFY] [EditorStateImpl.kt](file:///Users/ericdewildt/Source/KotlinMultiPlatform/Capture/app/shared/src/commonMain/kotlin/com/dewildte/capture/EditorStateImpl.kt)
- Update navigation logic for `AiTabClicked`:
    - Tell the context to transition to its `aiState`.

#### [MODIFY] [AiStateImpl.kt](file:///Users/ericdewildt/Source/KotlinMultiPlatform/Capture/app/shared/src/commonMain/kotlin/com/dewildte/capture/AiStateImpl.kt)
- Update navigation logic for `EditorTabClicked`:
    - Tell the context to transition to its `editorState`.

### Documentation

#### [NEW] [state_tree_architecture.artifact.md](file:///Users/ericdewildt/Source/KotlinMultiPlatform/Capture/.artifacts/68c3cb8d-8239-4e89-a592-6207359b68a0/state_tree_architecture.artifact.md)
Create a document describing the "Tree of States" architecture, detailing how sibling trees (Editor/AI) maintain their internal state and how the app transitions between them.

## Verification Plan

### Automated Tests
- Run `:app:androidApp:assembleDebug`.

### Manual Verification
1. **State Preservation**:
    - Navigate to the AI tab, start a message but don't send it.
    - Switch to the Editor tab.
    - Switch back to the AI tab and verify the unsent message is still there.
2. **Lazy Initialization**:
    - Verify (via logs) that `AiState` is not created until the AI tab is clicked for the first time.
3. **Initialization Flow**:
    - Ensure the app still starts in `InitialState` and transitions correctly after initialization.
