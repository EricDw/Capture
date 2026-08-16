package com.dewildte.capture.content.files

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dewildte.capture.FileListState
import com.dewildte.capture.components.FileCard
import com.dewildte.capture.events.FileInDrawerClicked

@Composable
fun FileListContent(
    state: FileListState,
    modifier: Modifier = Modifier
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(160.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalItemSpacing = 12.dp
    ) {
        items(state.files, key = { it.path }) { file ->
            FileCard(
                file = file,
                onClick = { state.tell(FileInDrawerClicked(file)) },
                // TODO: Load real previews if possible, otherwise null
                contentPreview = "Preview content for ${file.name} goes here. This would eventually show a snippet of the file body."
            )
        }
    }
}
