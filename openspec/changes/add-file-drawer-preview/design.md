# Design: File Navigation Drawer UI Component

## Component Architecture

The component will be placed in `app/shared/src/commonMain/kotlin/com/dewildte/capture/content/drawer/FileDrawerContent.kt`.

### Data Models
```kotlin
sealed interface FileNode {
    val name: String
    val path: String

    data class File(
        override val name: String,
        override val path: String,
        val extension: String = name.substringAfterLast('.', "")
    ) : FileNode

    data class Folder(
        override val name: String,
        override val path: String,
        val children: List<FileNode> = emptyList(),
        val isExpanded: Boolean = false
    ) : FileNode
}
```

### UI Composables Layout
- `FileDrawerContent`: Main container with `ModalDrawerSheet` or `PermanentDrawerSheet` style (`Surface` with `280.dp` - `320.dp` width).
- `DrawerHeader`: Material 3 workspace title and file action chips.
- `FolderDrawerItem`: Full-width expandable item with folder icon, title, item count chip, and expand/collapse arrow.
- `FileDrawerItem`: Full-width or slightly offset file item with extension-specific icon, file name, and active selection state highlighting (`primaryContainer`).

## Visual Style Guidelines
- **Container**: `MaterialTheme.colorScheme.surface` with `surfaceContainerLow`.
- **Selected File**: `MaterialTheme.colorScheme.primaryContainer` with `onPrimaryContainer` text.
- **Folder Container when Expanded**: Enclosed in subtle `surfaceContainer` card with rounded corners (`12.dp`) to visually group child items without horizontal indent shifts.
