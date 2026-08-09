package com.dewildte.capture.data

import androidx.compose.runtime.Immutable

/**
 * Domain representations of files and folders for the Navigation Drawer.
 */
@Immutable
sealed interface FileNode {
    val name: String
    val path: String

    data class File(
        override val name: String,
        override val path: String,
        val extension: String = name.substringAfterLast('.', "").lowercase()
    ) : FileNode

    data class Folder(
        override val name: String,
        override val path: String,
        val children: List<FileNode> = emptyList()
    ) : FileNode
}
