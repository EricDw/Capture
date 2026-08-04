# Tree of States Architecture

The Capture app uses a "Tree of States" architecture to manage UI state and navigation. This design ensures that complex feature areas maintain their internal state while allowing the user to switch between them seamlessly.

## Core Concepts

### 1. AppContext as the Root
The `AppContext` serves as the root of the state tree. It holds references to the primary feature state trees and the current active state.

### 2. Sibling State Trees
Features like the **Editor** and **AI Chat** are treated as sibling state trees.
- They are initialized lazily upon first access.
- They are persisted within the `AppContext` for the duration of the app session.
- Switching between tabs (e.g., from AI to Editor) simply updates the `state` pointer in the `AppContext` to the pre-existing sibling tree.

### 3. Hierarchical State Transitions
Within each sibling tree, states can have their own child states.
- Transitions are handled via the `tell(TransitionToState(newState))` mechanism.
- Since the sibling trees are persisted, returning to a "tab" restores the exact state it was in (e.g., preserving a half-typed message or search results).

## State Management Flow

1. **Initialization**: The app starts in `InitialState`.
2. **Feature Access**: When a user clicks a tab, the `AppContext` lazily initializes the corresponding `EditorState` or `AiState`.
3. **Switching**:
   - `EditorState` receives an `AiTabClicked` event -> Tells `AppContext` to transition to `context.aiState`.
   - `AiState` receives an `EditorTabClicked` event -> Tells `AppContext` to transition to `context.editorState`.
4. **Persistence**: Because `_editorState` and `_aiState` are stored as properties in `AppContextImpl`, their internal `mutableStateOf` properties (like `currentMessage` or `textFile`) are preserved.

## Benefits
- **Zero-loss Navigation**: Users can switch context without losing work.
- **Resource Efficiency**: States are only created when needed (lazy initialization).
- **Clear Hierarchy**: The tree structure mirrors the user's mental model of the app's features.
