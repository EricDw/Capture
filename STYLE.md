# Coding Style & Conventions

## Naming Conventions
- **Files vs Notes**: Prefer the term "File" over "Note" for all internal code, including:
    - State interfaces and implementations (e.g., `FileListState`, `EditorState.textFile`).
    - UI components (e.g., `FileCard`, `FileListContent`).
    - Data models and functions.
- **User-Facing Copy**: The term "Note" should be reserved for user-facing strings (labels, placeholders, accessibility descriptions) to maintain a friendly, familiar UI.

## Architecture
- **State Trees**: Adhere to the Hierarchical State Tree pattern as defined in `ARCITECTURE.md`.
- **Actor Model**: Use `tell(message)` for all state mutations and communication. "Tell, don't ask."
- **AppContext Casting**: In the UI layer, casting `AppContext` to `MutableAppContext` is strictly prohibited. Use `tell(message)` to communicate state changes.
- **Controller Property**: The `controller` property in `AppContextImpl` is an internal implementation detail and must not be exposed or accessed directly from the UI or state implementations. Use the `tell` method on the context to communicate with the application controller.
