# Global AI Model Loading State

This plan addresses the requirement to communicate the AI model initialization status to the UI state and block the conversation UI until the model is loaded. Currently, model initialization events are sent to the `AppContext`, but they are only handled if the app is already in the `AiState`. During app initialization, the app is in `InitialState`, causing these events to be lost.

## User Review Required

> [!NOTE]
> I am moving the AI model initialization status (loading, ready, error, name) into the `AppContext` level. This allows the status to be tracked globally, regardless of which screen the user is currently on.

## Proposed Changes

### [App Core State]

#### [MODIFY] [AppContext.kt](file:///Users/ericdewildt/Source/KotlinMultiPlatform/Capture/app/shared/src/commonMain/kotlin/com/dewildte/capture/AppContext.kt)
Add AI model state properties to `AppContext` and `MutableAppContext` interfaces.

#### [MODIFY] [AppContextImpl.kt](file:///Users/ericdewildt/Source/KotlinMultiPlatform/Capture/app/shared/src/commonMain/kotlin/com/dewildte/capture/AppContextImpl.kt)
- Implement new AI model state properties using `mutableStateOf`.
- Update `handleEvent` to intercept `AiResponseEvent` (specifically initialization events) and update the global properties.

#### [MODIFY] [AiState.kt](file:///Users/ericdewildt/Source/KotlinMultiPlatform/Capture/app/shared/src/commonMain/kotlin/com/dewildte/capture/AiState.kt)
Change properties to be non-mutable in the interface where they should be driven by the global state.

#### [MODIFY] [AiStateImpl.kt](file:///Users/ericdewildt/Source/KotlinMultiPlatform/Capture/app/shared/src/commonMain/kotlin/com/dewildte/capture/AiStateImpl.kt)
- Delegate `isModelLoading`, `isModelReady`, `selectedModelName`, and `error` to the `context`.
- Remove local state management for these properties in `handleAiResponseEvent`.

## Verification Plan

### Manual Verification
1. Launch the app.
2. Observe if the loading indicator appears in the AI tab if the model is still initializing from `onCreate`.
3. Verify that the conversation UI is blocked (e.g., Send button disabled or loading indicator shown) until the model is ready.
4. Test selecting a new model and verify the state updates correctly across tab switches.
