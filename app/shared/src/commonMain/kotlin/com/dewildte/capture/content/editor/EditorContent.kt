package com.dewildte.capture.content.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.insert
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dewildte.capture.EditorState
import com.dewildte.capture.data.TextFile
import com.dewildte.capture.events.*
import com.dewildte.capture.utils.samples.SampleText
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce

@OptIn(FlowPreview::class)
@Composable
fun EditorContent(
    modifier: Modifier = Modifier,
    textFile: TextFile = TextFile(),
    isNewFile: Boolean = false,
    searchMode: Boolean = false,
    searchTerm: String = "",
    snippetToInsert: String? = null,
    onEvent: (EditorContentEvent) -> Unit = {},
) {
    val titleFieldState = rememberTextFieldState(textFile.name)
    val contentFieldState = rememberTextFieldState(textFile.contents)
    
    val contentFocusRequester = remember { FocusRequester() }

    LaunchedEffect(isNewFile) {
        if (isNewFile) {
            contentFocusRequester.requestFocus()
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        if (searchMode) {
            val searchTermFieldState = rememberTextFieldState(
                searchTerm
            )
            OutlinedTextField(
                state = searchTermFieldState,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            )

            val searchTermFlow = remember(searchTermFieldState) {
                snapshotFlow { searchTermFieldState.text }
            }

            LaunchedEffect(searchTermFieldState) {
                searchTermFlow.collect {
                    onEvent(SearchTermChanged(it.toString()))
                }
            }
        }

        // Title Field
        BasicTextField(
            state = titleFieldState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            textStyle = MaterialTheme.typography.headlineSmall.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            lineLimits = TextFieldLineLimits.SingleLine,
            decorator = { innerTextField ->
                if (titleFieldState.text.isEmpty()) {
                    Text(
                        text = "Title",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
                innerTextField()
            }
        )

        // Content Field
        BasicTextField(
            state = contentFieldState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .focusRequester(contentFocusRequester),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorator = { innerTextField ->
                if (contentFieldState.text.isEmpty()) {
                    Text(
                        text = "Note",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
                innerTextField()
            }
        )

        val textFlow = snapshotFlow { contentFieldState.text }
        val titleFlow = snapshotFlow { titleFieldState.text }

        LaunchedEffect(onEvent) {
            textFlow.collect { newText ->
                val textString = newText.toString()
                if (textString.endsWith("/") && textString != textFile.contents) {
                    onEvent(InsertSnippetClicked)
                }
                onEvent(FileTextChanged(textString))
            }
        }

        LaunchedEffect(onEvent) {
            textFlow.debounce(1000.milliseconds).collect { newText ->
                onEvent(SaveFileRequested(newText.toString()))
            }
        }
        
        LaunchedEffect(onEvent) {
            titleFlow.collect { newTitle ->
                onEvent(TitleChanged(newTitle.toString()))
            }
        }

        LaunchedEffect(onEvent) {
            titleFlow.debounce(1000.milliseconds).collect { newTitle ->
                onEvent(RenameFileRequested(newTitle.toString()))
            }
        }

        LaunchedEffect(textFile.name) {
            if (titleFieldState.text.toString() != textFile.name) {
                titleFieldState.setTextAndPlaceCursorAtEnd(textFile.name)
            }
        }

        LaunchedEffect(textFile.contents) {
            if (contentFieldState.text.toString() != textFile.contents) {
                contentFieldState.setTextAndPlaceCursorAtEnd(textFile.contents)
            }
        }

        LaunchedEffect(snippetToInsert) {
            snippetToInsert?.let { snippet ->
                contentFieldState.edit {
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
    EditorContent(
        modifier = modifier,
        textFile = state.textFile,
        isNewFile = state.isNewFile,
        searchMode = state.searchMode,
        searchTerm = state.searchTerm,
        snippetToInsert = state.snippetToInsert,
        onEvent = state::tell
    )
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