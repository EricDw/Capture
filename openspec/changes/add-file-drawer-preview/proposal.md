# Proposal: Material 3 File & Folder Navigation Drawer Preview

## Intent
Design and build a Material Design 3 file and folder navigation drawer component (`FileDrawerContent.kt`) using Compose Multiplatform. The component is specifically engineered for Navigation Drawers where horizontal space is constrained, employing flat vertical accordion expansion rather than deep indent nesting. 

## Scope
- Create `FileNode` domain data models for representing hierarchical file & folder trees cleanly.
- Build `FileDrawerContent` composable supporting:
  - Vertical accordion expansion for folder nodes.
  - Material Design 3 color schemes, shapes, and typography.
  - Active file selection state handling.
  - File extension icon mapping (`.kt`, `.txt`, `.md`, `.json`, folder icons).
  - Standalone `@Preview` with comprehensive sample workspace data.
- Place the component under `app/shared/src/commonMain/kotlin/com/dewildte/capture/content/drawer/FileDrawerContent.kt`.

## Success Criteria
- Flat vertical accordion expansion renders nested folders without eating into horizontal drawer width.
- Fully compatible with Compose Multiplatform desktop and mobile previews (`@Preview`).
- Complies strictly with Material Design 3 theme tokens (`MaterialTheme.colorScheme`).
- Compiles cleanly with zero build errors.
