package com.dewildte.capture.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dewildte.capture.EditorState
import com.dewildte.capture.events.FindInPageClicked
import com.dewildte.capture.events.InsertSnippetClicked
import com.dewildte.capture.events.MoreMenuClicked
import com.dewildte.capture.events.MoreMenuDismissRequested
import com.dewildte.capture.events.SelectFileClicked
import com.dewildte.capture.events.SettingsClicked
import com.dewildte.capture.data.TextFile
import com.dewildte.capture.events.EditorContentEvent
import com.dewildte.capture.events.Event
import com.dewildte.capture.utils.samples.SampleText
import capture.app.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun EditorTopBar(
    textFile: TextFile,
    moreMenuExpanded: Boolean = false,
    onEvent: (EditorContentEvent) -> Unit = {},
) {
    TopAppBar(
        title = { Text(textFile.path) },
        actions = {
            Box {
                IconButton(
                    onClick = {
                        onEvent(MoreMenuClicked)
                    },
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                }
                DropdownMenu(
                    expanded = moreMenuExpanded,
                    onDismissRequest = {
                        onEvent(MoreMenuDismissRequested)
                    },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.label_select_file)) },
                        onClick = {
                            onEvent(SelectFileClicked)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.FileOpen,
                                contentDescription = stringResource(Res.string.desc_select_file)
                            )
                        }
                    )

                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.label_insert_snippet)) },
                        onClick = {
                            onEvent(InsertSnippetClicked)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.TextSnippet,
                                contentDescription = stringResource(Res.string.desc_insert_snippet)
                            )
                        }
                    )

                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.label_find_in_page)) },
                        onClick = {
                            onEvent(FindInPageClicked)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.FindInPage,
                                contentDescription = stringResource(Res.string.desc_find_in_page)
                            )
                        }
                    )

                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.label_settings)) },
                        onClick = {
                            onEvent(SettingsClicked)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(Res.string.desc_settings)
                            )
                        }
                    )
                }
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
        moreMenuExpanded = state.moreMenuExpanded,
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

@Composable
@Preview
private fun EditorTopBarMenuPreview() {
    Box(Modifier.height(275.dp)) {
        EditorTopBar(
            textFile = TextFile(SampleText.textFileName),
            moreMenuExpanded = true,
        )
    }
}
