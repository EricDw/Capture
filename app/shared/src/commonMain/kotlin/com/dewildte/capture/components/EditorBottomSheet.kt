package com.dewildte.capture.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dewildte.capture.EditorState
import com.dewildte.capture.events.Event
import com.dewildte.capture.events.SelectSnippetsFileClicked
import com.dewildte.capture.events.SnippetClicked
import com.dewildte.capture.events.SnippetSelectorDismissRequested
import com.dewildte.capture.utils.samples.SampleSnippets
import capture.app.shared.generated.resources.Res
import capture.app.shared.generated.resources.label_select_snippets_file
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorBottomSheet(
    snippets: List<String> = emptyList(),
    modifier: Modifier = Modifier,
    expand: Boolean = true,
    isEmpty: Boolean = true,
    onEvent: (Event) -> Unit = {},
) {
    val sheetState = rememberModalBottomSheetState()

    if (expand) {
        ModalBottomSheet(
            onDismissRequest = {
                onEvent(SnippetSelectorDismissRequested)
            },
            sheetState = sheetState,
            modifier = modifier,
        ) {
            if (isEmpty) {
                Box(
                    modifier = modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(
                        onClick = {
                            onEvent(SelectSnippetsFileClicked)
                        },
                    ) {
                        Text(stringResource(Res.string.label_select_snippets_file))
                    }
                }
            } else {
                TextButton(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    onClick = { onEvent(SelectSnippetsFileClicked) },
                ) {
                    Text(stringResource(Res.string.label_select_snippets_file))
                }

                Column(
                    modifier = modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    snippets.forEach { snippet ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                onEvent(SnippetClicked(snippet))
                            }
                        ) {
                            Text(modifier = Modifier.padding(16.dp), text = snippet)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditorBottomSheet(
    state: EditorState,
    modifier: Modifier = Modifier,
) {
    EditorBottomSheet(
        snippets = state.snippets,
        modifier = modifier,
        expand = state.snippetSelectorExpanded,
        isEmpty = state.snippetSelectorIsEmpty,
        onEvent = state::tell
    )
}


@Composable
@Preview
fun EditorBottomSheetPreview() {
    EditorBottomSheet(
        isEmpty = false,
        snippets = SampleSnippets.basic10,
    )
}

@Composable
@Preview
fun EmptyEditorBottomSheetPreview() {
    EditorBottomSheet(
        isEmpty = true,
    )
}