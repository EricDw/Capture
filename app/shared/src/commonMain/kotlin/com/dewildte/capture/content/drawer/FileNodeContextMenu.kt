package com.dewildte.capture.content.drawer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.dewildte.capture.data.FileNode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileNodeContextMenu(
    node: FileNode,
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    onRenameRequested: () -> Unit,
    onDeleteRequested: () -> Unit,
    onCreateFileRequested: (() -> Unit)? = null,
    onCreateFolderRequested: (() -> Unit)? = null,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = node.name,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleMedium
            )

            if (onCreateFileRequested != null) {
                ContextMenuItem(
                    label = "New File",
                    icon = Icons.Default.NoteAdd,
                    onClick = {
                        onDismissRequest()
                        onCreateFileRequested()
                    }
                )
            }

            if (onCreateFolderRequested != null) {
                ContextMenuItem(
                    label = "New Folder",
                    icon = Icons.Default.CreateNewFolder,
                    onClick = {
                        onDismissRequest()
                        onCreateFolderRequested()
                    }
                )
            }

            ContextMenuItem(
                label = "Rename",
                icon = Icons.Default.DriveFileRenameOutline,
                onClick = {
                    onDismissRequest()
                    onRenameRequested()
                }
            )

            ContextMenuItem(
                label = "Delete",
                icon = Icons.Default.Delete,
                onClick = {
                    onDismissRequest()
                    onDeleteRequested()
                },
                contentColor = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun ContextMenuItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    contentColor: Color = LocalContentColor.current
) {
    ListItem(
        headlineContent = { Text(label, color = contentColor) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}
