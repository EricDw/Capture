# AI Agent Instructions

Before starting any task, refactor, or implementation, you MUST read and adhere to the following project documentation:

1.  **[STYLE.md](../STYLE.md)**: Contains critical naming conventions (e.g., "File" vs "Note") and architecture rules.
2.  **[ARCITECTURE.md](../ARCITECTURE.md)**: Explains the Hierarchical State Tree and Actor Model implementation.
3.  **[VISION.md](../VISION.md)**: Outlines the high-level goals and "why" behind the project.
4.  **[ORGANIZATION.md](../ORGANIZATION.md)**: Details the project structure and where things live.

## Critical Constraints
- **Naming**: Use "File" for all code-level entities (States, Components, Data Models). "Note" is for user-facing copy ONLY.
- **Actor Model**: "Tell, don't ask." Use `tell(message)` for all communication.
- **State Trees**: Maintain the hierarchy. Never cast `AppContext` to `MutableAppContext` in the UI.
