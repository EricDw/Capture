package com.dewildte.capture.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.dewildte.capture.SettingsState
import com.dewildte.capture.events.BackClicked
import com.dewildte.capture.events.Event
import capture.app.shared.generated.resources.Res
import capture.app.shared.generated.resources.desc_navigate_back
import capture.app.shared.generated.resources.title_settings
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsTopBar(
    modifier: Modifier = Modifier,
    onEvent: (event: Event) -> Unit = {},
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                stringResource(Res.string.title_settings),
            )
        },
        navigationIcon = {
            IconButton(onClick = {
                onEvent(BackClicked)
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = stringResource(Res.string.desc_navigate_back),
                )
            }
        },
    )
}

@Composable
fun SettingsTopBar(
    state: SettingsState,
) {
    SettingsTopBar(onEvent = state::tell)
}

@Composable
@Preview
private fun SettingsTopBarPreview() {
    SettingsTopBar()
}