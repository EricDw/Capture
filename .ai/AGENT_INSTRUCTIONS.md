# AI Agent Instructions

To ensure high-quality, architecturally consistent contributions, you must use the following **Relevance Guide** during your initial discovery phase to determine which documentation files to read for your current task. 

**Instruction**: Do not read all files by default. Instead, read only those that are relevant based on the scope of your assignment.

### Relevance Guide

| Document | When to Read | Key Information |
| :--- | :--- | :--- |
| **[STYLE.md](../STYLE.md)** | Naming new entities, adding UI text, or refactoring existing code. | Nomenclature rules (e.g., "File" vs "Note") and general coding conventions. |
| **[ARCITECTURE.md](../ARCITECTURE.md)** | Modifying `State` implementations, the `Actor` model, or `AppContext`. | Implementation of Hierarchical State Trees and message passing. |
| **[VISION.md](../VISION.md)** | Large architectural changes or determining feature priority. | The "Why" behind the project and high-level product goals. |
| **[ORGANIZATION.md](../ORGANIZATION.md)** | Creating new files, packages, modules, or sub-projects. | The "Where" - defines the project structure and file locations. |
| **[DESIGN.md](../DESIGN.md)** | Creating or updating UI components and layouts. | M3 Expressive design rules and component preferences. |

## Critical Constraints
- **Discovery Phase**: Always scan the Relevance Guide above as your first step.
- **Naming**: Use "File" for all code-level entities (States, Components, Data Models). "Note" is for user-facing copy ONLY.
- **Actor Model**: "Tell, don't ask." Use `tell(message)` for all communication.
- **State Trees**: Maintain the hierarchy. Never cast `AppContext` to `MutableAppContext` in the UI.
- **Test-Driven Development (TDD)**: You MUST use TDD. Write a failing test before writing any production code, then implement the minimal code to make it pass.
- **Decision Making**: For significant architectural or design decisions, or when multiple implementation paths exist, you MUST pause and present the options to the user for feedback.
- **Planning Loop**: For large or complex tasks, always create an implementation plan (`implementation_plan.artifact.md`) and wait for explicit user approval before modifying source code.
