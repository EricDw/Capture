package com.dewildte.capture.content.empty

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.dewildte.capture.utils.Actor
import capture.app.shared.generated.resources.Res
import capture.app.shared.generated.resources.label_select_file
import org.jetbrains.compose.resources.stringResource

@Composable
fun EmptyContent(
    modifier: Modifier = Modifier,
    onEvent: (EmptyContentEvent) -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            TextButton(onClick = { onEvent(SelectTextFileClicked) }) {
                Text(stringResource(Res.string.label_select_file))
            }
        }
    }
}

@Composable
@Preview
private fun EmptyContentPreview() {
    EmptyContent()
}