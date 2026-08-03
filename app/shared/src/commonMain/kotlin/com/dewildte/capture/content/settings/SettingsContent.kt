package com.dewildte.capture.content.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dewildte.capture.SettingsState
import com.dewildte.capture.events.Event
import com.dewildte.capture.events.SnippetClicked

@Composable
fun SettingsContent(
    modifier: Modifier = Modifier,
    snippets: List<String> = emptyList(),
    onEvent: (Event) -> Unit = {}
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        stickyHeader {
            Text("Snippets")
        }
        items(items = snippets, key = { snippet -> snippet }) { snippet ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onEvent(SnippetClicked(snippet))
                }
            ) { Text(modifier = Modifier.padding(16.dp), text = snippet) }
        }
    }
}

@Composable
fun SettingsContent(
    state: SettingsState,
    modifier: Modifier = Modifier
) {
    SettingsContent(
        modifier = modifier,
        snippets = state.snippets,
        onEvent = state::tell
    )
}

@Composable
@Preview
private fun SettingsContentPreview() {
    val snippets = (0..10).map { index ->
        "Snippet $index"
    }
    SettingsContent(
        snippets = snippets
    )
}