# Proposal: Upgrade Chat UI to Material 3 & Smart Input

## Intent
Apply the Chat UI Design Prompt (`docs/prompts/chat-ui-design-prompt.md`) to the Capture application by polishing the Compose Multiplatform UI. This upgrade enhances the chat interface with Material Design 3 guidelines, dynamic system theme adaptation, interactive tool execution chips, code block formatting, and a smart multiline input bar with slash command hints.

## Scope
- **`MessageInputBar.kt`**: Upgrade input field to support multiline auto-expansion, slash command detection hints (`/plan`, `/opsx-propose`, `/clear`), and clean send/stop generation controls.
- **`ChatContent.kt`**:
  - Refactor tool execution badges to use Material 3 `AssistChip` / `SuggestionChip` with appropriate M3 system color tokens (`MaterialTheme.colorScheme.primary` and `MaterialTheme.colorScheme.error`).
  - Upgrade `MessageBubble` styling to support formatted code snippets with monospace fonts and `surfaceVariant` background containers.
  - Add quick action buttons (e.g. copy message content to clipboard) on message bubbles.
  - Enhance `TypingIndicator` with clean M3 container alignment and subtle dot pulse visuals.
- **Theme Consistency**: Ensure 100% adherence to dynamic system light and dark color schemes without any hardcoded colors or CSS styles.

## Success Criteria
- All Chat UI composables dynamically switch between Light and Dark mode depending on system settings without visual glitches.
- Tool executions (MCP tool calls and errors) display as clean M3 `AssistChip` badges with proper icons and dynamic system colors.
- Input bar supports slash command hints and auto-expands cleanly.
- Code multiplatform build compiles clean with zero errors.
