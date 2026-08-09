# Design: Chat UI Material 3 & Smart Input

## Architecture Overview
The Chat UI is implemented as Compose Multiplatform composables residing in `app/shared/src/commonMain/kotlin/com/dewildte/capture/content/ai/`.

### Key Components

1. **`MessageInputBar.kt`**:
   - Uses `OutlinedTextField` with `minLines = 1` and `maxLines = 5`.
   - Listens to input text changes to show a Slash Command suggestion bar (`SlashCommandHint`) above the text field when `text.startsWith("/")`.
   - Sends events `SendMessageClicked`, `StopGeneratingClicked`, or slash command events via `onEvent`.

2. **`ChatContent.kt`**:
   - `MessageBubble`: Wrapped with M3 container shapes (`RoundedCornerShape(12.dp)`).
   - Tool execution badges: Refactored to M3 `AssistChip` composables with `MaterialTheme.colorScheme.primary` (success) or `MaterialTheme.colorScheme.error` (failure).
   - Action row on assistant message hover/focus or bubble bottom: Copy text button and optional file link badges.
   - Code block renderer: Identifies code blocks or file paths, rendering them inside rounded `surfaceVariant` boxes with monospace text.
   - `TypingIndicator`: Clean M3 pill container with animated or styled dots using `MaterialTheme.colorScheme.secondaryContainer`.

## State & Data Flow
- UI state is owned by `AiState` and passed to `ChatContent`.
- All user interactions emit unidirectional `AiContentEvent` events (`MessageInputChanged`, `SendMessageClicked`, `StopGeneratingClicked`, `OpenFileRequested`).
