package com.dewildte.capture.events

import com.dewildte.capture.data.FileNode

sealed interface WorkspaceEvent : Event

data object ToggleDrawerClicked : WorkspaceEvent

data object SelectWorkspaceFolderRequested : WorkspaceEvent

data class WorkspaceFolderSelected(val uri: String) : WorkspaceEvent

data class WorkspaceNodesLoaded(val nodes: List<FileNode>) : WorkspaceEvent

data class FileInDrawerClicked(val file: FileNode.File) : WorkspaceEvent

data object RefreshWorkspaceRequested : WorkspaceEvent

data class CreateFileRequested(val parentPath: String?, val name: String) : WorkspaceEvent

data class CreateFolderRequested(val parentPath: String?, val name: String) : WorkspaceEvent

data class RenameNodeRequested(val node: FileNode, val newName: String) : WorkspaceEvent

data class DeleteNodeRequested(val node: FileNode) : WorkspaceEvent

data object WorkspaceFileOperationSuccess : WorkspaceEvent
