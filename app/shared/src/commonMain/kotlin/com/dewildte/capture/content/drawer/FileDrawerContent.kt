package com.dewildte.capture.content.drawer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import com.dewildte.capture.data.FileNode
import kotlinx.coroutines.launch

/**
 * Material Design 3 File Navigation Drawer Content.
 * Strictly adheres to Material Design 3 Navigation Drawer specifications:
 * - Standard width: 360dp (or 280dp-360dp drawer range)
 * - Container color: MaterialTheme.colorScheme.surfaceContainerLow
 * - Standard 12dp horizontal margin padding
 * - NavigationDrawerItem components with stadium/pill shape selection
 * - Flat vertical accordion expansion (no horizontal indent shifts)
 *
 * @see <a href="https://m3.material.io/components/navigation-drawer/overview">Material 3 Navigation Drawer Guidelines</a>
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileDrawerContent(
    nodes: List<FileNode>,
    selectedFilePath: String?,
    onFileSelected: (FileNode.File) -> Unit,
    expandedFolders: MutableMap<String, Boolean>,
    onCreateFileRequested: (parentPath: String?, name: String) -> Unit,
    onCreateFolderRequested: (parentPath: String?, name: String) -> Unit,
    onRenameRequested: (node: FileNode, newName: String) -> Unit,
    onDeleteRequested: (node: FileNode) -> Unit,
    modifier: Modifier = Modifier,
    onRefreshClicked: () -> Unit = {},
    onCollapseClicked: () -> Unit = {},
    isInitializing: Boolean = false,
    isFolderSelected: Boolean = true,
    onSelectFolderClicked: () -> Unit = {},
) {
    var createParentPath by remember { mutableStateOf<String?>(null) }
    var isCreateFileVisible by remember { mutableStateOf(false) }
    var isCreateFolderVisible by remember { mutableStateOf(false) }

    var renameNode by remember { mutableStateOf<FileNode?>(null) }
    var deleteNode by remember { mutableStateOf<FileNode?>(null) }
    var contextMenuNode by remember { mutableStateOf<FileNode?>(null) }

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier.fillMaxHeight(),
    ) {
        Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            // M3 Standard Drawer Header (28dp start padding, 16dp vertical padding)
            FileDrawerHeader(
                fileCount = countTotalFiles(nodes),
                onRefreshClicked = onRefreshClicked,
                onCollapseClicked = onCollapseClicked,
                onCreateFileClicked = {
                    createParentPath = null
                    isCreateFileVisible = true
                },
                onCreateFolderClicked = {
                    createParentPath = null
                    isCreateFolderVisible = true
                },
                showActions = isFolderSelected && !isInitializing
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when {
                    isInitializing -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    !isFolderSelected -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "No Folder Selected",
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Select a workspace folder to view and manage files in this project.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(24.dp))
                            Button(
                                onClick = onSelectFolderClicked,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Select Folder")
                            }
                        }
                    }
                    else -> {
                        // Flat Vertical Accordion List (12dp M3 drawer item padding)
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(items = nodes, key = { it.path }) { node ->
                                FileNodeItem(
                                    node = node,
                                    selectedFilePath = selectedFilePath,
                                    expandedFolders = expandedFolders,
                                    onFileSelected = onFileSelected,
                                    onFolderToggle = { folderPath ->
                                        expandedFolders[folderPath] = !(expandedFolders[folderPath] ?: false)
                                    }
                                ) { contextMenuNode = it }
                            }
                        }
                    }
                }
            }
        }
    }

    // Context Menu
    contextMenuNode?.let { node ->
        FileNodeContextMenu(
            node = node,
            sheetState = sheetState,
            onDismissRequest = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    contextMenuNode = null
                }
            },
            onRenameRequested = { renameNode = node },
            onDeleteRequested = { deleteNode = node },
            onCreateFileRequested = if (node is FileNode.Folder) {
                {
                    createParentPath = node.path
                    isCreateFileVisible = true
                }
            } else null,
            onCreateFolderRequested = if (node is FileNode.Folder) {
                {
                    createParentPath = node.path
                    isCreateFolderVisible = true
                }
            } else null
        )
    }

    // Dialogs
    if (isCreateFileVisible) {
        InputDialog(
            title = "Create New File",
            onDismiss = { isCreateFileVisible = false },
            onConfirm = { name ->
                onCreateFileRequested(createParentPath, name)
                isCreateFileVisible = false
            }
        )
    }

    if (isCreateFolderVisible) {
        InputDialog(
            title = "Create New Folder",
            onDismiss = { isCreateFolderVisible = false },
            onConfirm = { name ->
                onCreateFolderRequested(createParentPath, name)
                isCreateFolderVisible = false
            }
        )
    }

    renameNode?.let { node ->
        InputDialog(
            title = "Rename",
            initialValue = node.name,
            onDismiss = { renameNode = null },
            onConfirm = { newName ->
                onRenameRequested(node, newName)
                renameNode = null
            }
        )
    }

    deleteNode?.let { node ->
        AlertDialog(
            onDismissRequest = { deleteNode = null },
            title = { Text("Delete ${if (node is FileNode.Folder) "Folder" else "File"}") },
            text = { Text("Are you sure you want to delete '${node.name}'? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteRequested(node)
                        deleteNode = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteNode = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Material Design 3 Drawer Header
 * Follows M3 spec: 28dp start padding, titleMedium typography, 56dp height header bar.
 */
@Composable
private fun FileDrawerHeader(
    fileCount: Int,
    onRefreshClicked: () -> Unit,
    onCollapseClicked: () -> Unit,
    onCreateFileClicked: () -> Unit,
    onCreateFolderClicked: () -> Unit,
    showActions: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(start = 16.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            IconButton(onClick = onCollapseClicked, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuOpen,
                    contentDescription = "Collapse Workspace",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = "Workspace",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (showActions) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Text(text = fileCount.toString(), style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        if (showActions) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onCreateFileClicked, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.NoteAdd,
                        contentDescription = "New File",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onCreateFolderClicked, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.CreateNewFolder,
                        contentDescription = "New Folder",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onRefreshClicked, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh files",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FileNodeItem(
    node: FileNode,
    selectedFilePath: String?,
    expandedFolders: Map<String, Boolean>,
    onFileSelected: (FileNode.File) -> Unit,
    onFolderToggle: (String) -> Unit,
    onNodeLongClick: (FileNode) -> Unit,
) {
    when (node) {
        is FileNode.File -> {
            FileDrawerItemRow(
                file = node,
                isSelected = node.path == selectedFilePath,
                onFileSelected = onFileSelected,
                onLongClick = { onNodeLongClick(node) }
            )
        }
        is FileNode.Folder -> {
            FolderAccordionGroup(
                folder = node,
                selectedFilePath = selectedFilePath,
                expandedFolders = expandedFolders,
                onFileSelected = onFileSelected,
                onFolderToggle = onFolderToggle,
                onFolderLongClick = { onNodeLongClick(node) },
                onNodeLongClick = onNodeLongClick
            )
        }
    }
}

/**
 * Material Design 3 Folder Accordion Container.
 * Groups nested files inside a rounded M3 surface container without horizontal indent shifts.
 */
@Composable
private fun FolderAccordionGroup(
    folder: FileNode.Folder,
    selectedFilePath: String?,
    expandedFolders: Map<String, Boolean>,
    onFileSelected: (FileNode.File) -> Unit,
    onFolderToggle: (String) -> Unit,
    onFolderLongClick: () -> Unit,
    onNodeLongClick: (FileNode) -> Unit,
) {
    val isExpanded = expandedFolders[folder.path] ?: false

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) {
                MaterialTheme.colorScheme.surfaceContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
        ),
        shape = ShapeDefaults.Large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Folder Header Item (56dp standard touch height)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(ShapeDefaults.Large)
                    .combinedClickable(
                        onClick = { onFolderToggle(folder.path) },
                        onLongClick = onFolderLongClick
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                        contentDescription = null,
                        tint = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = folder.name,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (isExpanded) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${folder.children.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse folder" else "Expand folder",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Expanded Children Flat Vertical Grouping with left accent indicator (Chrome-style)
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 4.dp, bottom = 4.dp, top = 0.dp)
                ) {
                    // Chrome-style Vertical Accent Line
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .fillMaxHeight()
                            .padding(vertical = 4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(1.dp)
                            )
                    )

                    Spacer(Modifier.width(10.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        folder.children.forEach { childNode ->
                            FileNodeItem(
                                node = childNode,
                                selectedFilePath = selectedFilePath,
                                expandedFolders = expandedFolders,
                                onFileSelected = onFileSelected,
                                onFolderToggle = onFolderToggle,
                                onNodeLongClick = onNodeLongClick
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Material Design 3 NavigationDrawerItem Implementation.
 * Uses official M3 NavigationDrawerItem API with stadium pill shape (CircleShape / Full).
 */
@Composable
private fun FileDrawerItemRow(
    file: FileNode.File,
    isSelected: Boolean,
    onFileSelected: (FileNode.File) -> Unit,
    onLongClick: () -> Unit,
) {
    NavigationDrawerItem(
        label = {
            Text(
                text = file.name,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        selected = isSelected,
        onClick = { onFileSelected(file) },
        icon = {
            Icon(
                imageVector = getFileIcon(file.extension),
                contentDescription = null,
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    getIconTint(file.extension)
                },
                modifier = Modifier.size(20.dp)
            )
        },
        shape = CircleShape,
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
            unselectedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .combinedClickable(
                onClick = { onFileSelected(file) },
                onLongClick = onLongClick
            )
    )
}

private fun getFileIcon(extension: String): ImageVector {
    return when (extension) {
        "kt", "kts", "java", "json", "xml" -> Icons.Default.Code
        "md", "txt", "doc" -> Icons.Default.Description
        "png", "jpg", "jpeg", "svg" -> Icons.Default.Image
        else -> Icons.AutoMirrored.Filled.InsertDriveFile
    }
}

@Composable
private fun getIconTint(extension: String): androidx.compose.ui.graphics.Color {
    return when (extension) {
        "kt", "kts" -> MaterialTheme.colorScheme.primary
        "md", "txt" -> MaterialTheme.colorScheme.secondary
        "json", "xml" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@Composable
private fun InputDialog(
    title: String,
    initialValue: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun countTotalFiles(nodes: List<FileNode>): Int {
    var count = 0
    fun traverse(nodeList: List<FileNode>) {
        nodeList.forEach { node ->
            when (node) {
                is FileNode.File -> count++
                is FileNode.Folder -> traverse(node.children)
            }
        }
    }
    traverse(nodes)
    return count
}

// ============================================================================
// PREVIEWS
// ============================================================================

/**
 * Sample Workspace dataset for Standalone Material 3 Navigation Drawer Preview.
 */
private val SampleWorkspace = listOf(
    FileNode.Folder(
        name = "app/shared",
        path = "app/shared",
        children = listOf(
            FileNode.File("App.kt", "app/shared/App.kt"),
            FileNode.File("AiState.kt", "app/shared/AiState.kt"),
            FileNode.Folder(
                name = "content/ai",
                path = "app/shared/content/ai",
                children = listOf(
                    FileNode.File("ChatContent.kt", "app/shared/content/ai/ChatContent.kt"),
                    FileNode.File("MessageInputBar.kt", "app/shared/content/ai/MessageInputBar.kt"),
                    FileNode.File("AiContent.kt", "app/shared/content/ai/AiContent.kt")
                )
            )
        )
    ),
    FileNode.Folder(
        name = "docs/prompts",
        path = "docs/prompts",
        children = listOf(
            FileNode.File("chat-ui-design-prompt.md", "docs/prompts/chat-ui-design-prompt.md"),
            FileNode.File("architecture-code-review-prompt.md", "docs/prompts/architecture-code-review-prompt.md"),
            FileNode.File("performance-code-review-prompt.md", "docs/prompts/performance-code-review-prompt.md"),
            FileNode.File("ui-design-code-review-prompt.md", "docs/prompts/ui-design-code-review-prompt.md")
        )
    ),
    FileNode.File("README.md", "README.md"),
    FileNode.File("build.gradle.kts", "build.gradle.kts")
)

@Preview
@Composable
fun FileDrawerPreview() {
    var selectedFile by remember { mutableStateOf("app/shared/content/ai/ChatContent.kt") }
    val expandedFolders = remember { mutableStateMapOf<String, Boolean>() }

    MaterialTheme {
        FileDrawerContent(
            nodes = SampleWorkspace,
            selectedFilePath = selectedFile,
            onFileSelected = { file -> selectedFile = file.path },
            expandedFolders = expandedFolders,
            onCollapseClicked = {},
            onCreateFileRequested = { _, _ -> },
            onCreateFolderRequested = { _, _ -> },
            onRenameRequested = { _, _ -> },
            onDeleteRequested = { _ -> }
        )
    }
}

@Preview
@Composable
fun FileDrawerInitializingPreview() {
    MaterialTheme {
        FileDrawerContent(
            nodes = emptyList(),
            selectedFilePath = null,
            onFileSelected = {},
            onCollapseClicked = {},
            isInitializing = true,
            expandedFolders = mutableMapOf(),
            onCreateFileRequested = { _, _ -> },
            onCreateFolderRequested = { _, _ -> },
            onRenameRequested = { _, _ -> },
            onDeleteRequested = { _ -> }
        )
    }
}

@Preview
@Composable
fun FileDrawerNoFolderPreview() {
    MaterialTheme {
        FileDrawerContent(
            nodes = emptyList(),
            selectedFilePath = null,
            onFileSelected = {},
            onCollapseClicked = {},
            isFolderSelected = false,
            onSelectFolderClicked = {},
            expandedFolders = mutableMapOf(),
            onCreateFileRequested = { _, _ -> },
            onCreateFolderRequested = { _, _ -> },
            onRenameRequested = { _, _ -> },
            onDeleteRequested = { _ -> }
        )
    }
}
