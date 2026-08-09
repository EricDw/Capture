package com.dewildte.capture.content.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.insert
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dewildte.capture.EditorState
import com.dewildte.capture.data.TextFile
import com.dewildte.capture.events.EditorContentEvent
import com.dewildte.capture.events.FileTextChanged
import com.dewildte.capture.events.InsertSnippetClicked
import com.dewildte.capture.events.SearchTermChanged
import com.dewildte.capture.events.SnippetInserted
import com.dewildte.capture.utils.samples.SampleText

@Composable
fun EditorContent(
    modifier: Modifier = Modifier,
    textFile: TextFile = TextFile(),
    searchMode: Boolean = false,
    searchTerm: String = "",
    snippetToInsert: String? = null,
    onEvent: (EditorContentEvent) -> Unit = {},
) {

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        if (searchMode) {
            val searchTermFieldState = rememberTextFieldState(
                searchTerm
            )
            OutlinedTextField(
                state = searchTermFieldState,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(8.dp))

            val searchTermFlow = remember(searchTermFieldState) {
                snapshotFlow { searchTermFieldState.text }
            }

            LaunchedEffect(searchTermFieldState) {
                searchTermFlow.collect {
                    onEvent(SearchTermChanged(it.toString()))
                }
            }
        }

        val textFieldState = rememberTextFieldState(
            initialText = textFile.contents,
        )

        BasicTextField(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            state = textFieldState,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
        )

        val textFlow = snapshotFlow { textFieldState.text }

        LaunchedEffect(onEvent) {
            textFlow.collect { newText ->
                val textString = newText.toString()
                if (textString.endsWith("/") && textString != textFile.contents) {
                    onEvent(InsertSnippetClicked)
                }
                onEvent(FileTextChanged(textString))
            }
        }

        LaunchedEffect(textFile.path) {
            if (textFieldState.text.toString() != textFile.contents) {
                textFieldState.setTextAndPlaceCursorAtEnd(textFile.contents)
            }
        }

        LaunchedEffect(snippetToInsert) {
            snippetToInsert?.let { snippet ->
                textFieldState.edit {
                    // Remove the slash if it's there
                    val cursor = selection.start
                    if (cursor > 0 && asCharSequence()[cursor - 1] == '/') {
                        replace(cursor - 1, cursor, "")
                    }
                    insert(selection.start, snippet)
                }
                onEvent(SnippetInserted)
            }
        }

    }
}

@Composable
fun EditorContent(
    state: EditorState,
    modifier: Modifier = Modifier,
) {
    with(state) {
        EditorContent(
            modifier = modifier,
            textFile = textFile,
            searchMode = searchMode,
            searchTerm = searchTerm,
            snippetToInsert = state.snippetToInsert,
            onEvent = state::tell
        )
    }
}

@Composable
@Preview
private fun EditorContentPreview() {
    EditorContent(
        modifier = Modifier.fillMaxSize(),
        textFile = TextFile(
            path = SampleText.textFileName,
            contents = SampleText.loremIpsum
        ),
        searchTerm = "",
    )
}