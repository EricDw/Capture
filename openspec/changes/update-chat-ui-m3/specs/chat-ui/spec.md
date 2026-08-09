# Delta Spec: Chat UI Material 3 & Smart Input

## ADDED Requirements

### Requirement: Material 3 System Theme Adherence
The Chat UI MUST use `MaterialTheme.colorScheme` tokens exclusively (`primaryContainer`, `onPrimaryContainer`, `secondaryContainer`, `onSecondaryContainer`, `surfaceVariant`, `onSurfaceVariant`, `outline`, `error`) for all text, background containers, borders, and status chips. It MUST dynamically adapt when the system toggles between light and dark mode.

### Requirement: Tool Execution Badges
Tool execution statuses (including successful MCP tool calls and errors) MUST be rendered using Material 3 chips (`AssistChip` or `SuggestionChip`) displaying proper system icon tints and typography (`MaterialTheme.typography.labelSmall`).

### Requirement: Smart Message Input Dock
The `MessageInputBar` MUST provide multiline auto-expansion up to 5 lines, display a helpful hint popup or placeholder when a slash `/` character is typed (suggesting commands like `/plan`, `/opsx-propose`, `/clear`), and disable appropriately when AI is generating or initializing.

### Requirement: Code Block & Action Buttons
Message bubbles MUST format code snippets with monospace typography, `surfaceVariant` containers, and provide a copy-to-clipboard action icon for convenient text copying.
