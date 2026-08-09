package com.dewildte.capture.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dewildte.capture.EditorState
import com.dewildte.capture.events.FindInPageClicked
import com.dewildte.capture.events.SelectFileClicked
import com.dewildte.capture.data.TextFile
import com.dewildte.capture.events.EditorContentEvent
import com.dewildte.capture.utils.samples.SampleText
import capture.app.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorTopBar(
    textFile: TextFile,
    onEvent: (EditorContentEvent) -> Unit = {},
) {
    TopAppBar(
        title = { Text(textFile.path) },
        actions = {
            IconButton(
                onClick = {
                    onEvent(SelectFileClicked)
                },
            ) {
                Icon(
                    imageVector = Icons.Default.FileOpen,
                    contentDescription = stringResource(Res.string.desc_select_file)
                )
            }

            IconButton(
                onClick = {
                    onEvent(FindInPageClicked)
                },
            ) {
                Icon(
                    imageVector = Icons.Default.FindInPage,
                    contentDescription = stringResource(Res.string.desc_find_in_page)
                )
            }
        }
    )
}


@Composable
fun EditorTopBar(
    state: EditorState
) {
    EditorTopBar(
        textFile = state.textFile,
        onEvent = state::tell,
    )
}

@Composable
@Preview
private fun EditorTopBarPreview() {
    EditorTopBar(
        textFile = TextFile(SampleText.textFileName),
    )
}
