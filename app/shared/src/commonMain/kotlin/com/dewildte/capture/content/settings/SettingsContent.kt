package com.dewildte.capture.content.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dewildte.capture.SettingsState
import com.dewildte.capture.events.*

@Composable
fun SettingsContent(
    modifier: Modifier = Modifier,
    mcpServers: List<String> = emptyList(),
    searchToolEnabled: Boolean = true,
    isGoogleAuthenticated: Boolean = false,
    googleUserEmail: String? = null,
    googleDriveEnabled: Boolean = true,
    googleCalendarEnabled: Boolean = true,
    googleGmailEnabled: Boolean = true,
    googleTasksEnabled: Boolean = true,
    googleDocsEnabled: Boolean = true,
    googleSheetsEnabled: Boolean = true,
    googleSlidesEnabled: Boolean = true,
    googleClientId: String? = null,
    onEvent: (Event) -> Unit = {}
) {
    var showAddMcpDialog by remember { mutableStateOf(false) }
    var newMcpUrl by remember { mutableStateOf("") }
    
    var editingClientId by remember(googleClientId) { mutableStateOf(googleClientId ?: "") }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text("Google Workspace", style = MaterialTheme.typography.titleLarge)
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Configuration", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editingClientId,
                        onValueChange = { 
                            editingClientId = it
                            onEvent(UpdateGoogleClientIdClicked(it)) 
                        },
                        label = { Text("Web Client ID") },
                        placeholder = { Text("xxxxxx-yyyyyy.apps.googleusercontent.com") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(16.dp))

                    if (isGoogleAuthenticated) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Google Workspace", style = MaterialTheme.typography.titleMedium)
                                Text("Connected as: $googleUserEmail", style = MaterialTheme.typography.bodySmall)
                            }
                            Button(onClick = { onEvent(SignOutWithGoogleClicked) }) {
                                Text("Disconnect")
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))
                        
                        WorkspaceToggle("Google Drive", googleDriveEnabled) { 
                            onEvent(ToggleWorkspaceToolClicked(WorkspaceTool.DRIVE, it)) 
                        }
                        WorkspaceToggle("Google Calendar", googleCalendarEnabled) { 
                            onEvent(ToggleWorkspaceToolClicked(WorkspaceTool.CALENDAR, it)) 
                        }
                        WorkspaceToggle("Gmail", googleGmailEnabled) { 
                            onEvent(ToggleWorkspaceToolClicked(WorkspaceTool.GMAIL, it)) 
                        }
                        WorkspaceToggle("Google Tasks", googleTasksEnabled) { 
                            onEvent(ToggleWorkspaceToolClicked(WorkspaceTool.TASKS, it)) 
                        }
                        WorkspaceToggle("Google Docs", googleDocsEnabled) { 
                            onEvent(ToggleWorkspaceToolClicked(WorkspaceTool.DOCS, it)) 
                        }
                        WorkspaceToggle("Google Sheets", googleSheetsEnabled) { 
                            onEvent(ToggleWorkspaceToolClicked(WorkspaceTool.SHEETS, it)) 
                        }
                        WorkspaceToggle("Google Slides", googleSlidesEnabled) { 
                            onEvent(ToggleWorkspaceToolClicked(WorkspaceTool.SLIDES, it)) 
                        }
                    } else {
                        Text("Connect your Google account to enable Drive, Docs, and Calendar tools.")
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { onEvent(SignInWithGoogleClicked) }) {
                            Text("Sign in with Google")
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
            Text("AI Tools", style = MaterialTheme.typography.titleLarge)
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Global Search Tool", modifier = Modifier.weight(1f))
                Switch(
                    checked = searchToolEnabled,
                    onCheckedChange = { onEvent(ToggleSearchToolClicked(it)) }
                )
            }
        }
        
        item {
            Spacer(Modifier.height(16.dp))
            Text("Custom MCP Servers", style = MaterialTheme.typography.titleMedium)
        }
        
        items(mcpServers) { url ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(url, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    IconButton(onClick = { onEvent(RemoveMcpServerClicked(url)) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
        
        item {
            OutlinedButton(
                onClick = { showAddMcpDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add MCP Server")
            }
        }
    }

    if (showAddMcpDialog) {
        AlertDialog(
            onDismissRequest = { showAddMcpDialog = false },
            title = { Text("Add MCP Server") },
            text = {
                Column {
                    Text("Enter the URL of the MCP server (SSE or HTTP endpoint).", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    TextField(
                        value = newMcpUrl,
                        onValueChange = { newMcpUrl = it },
                        label = { Text("Server URL") },
                        placeholder = { Text("https://example.com/mcp") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newMcpUrl.isNotBlank()) {
                            onEvent(AddMcpServerClicked(newMcpUrl.trim()))
                            newMcpUrl = ""
                            showAddMcpDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMcpDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsContent(
    state: SettingsState,
    modifier: Modifier = Modifier
) {
    SettingsContent(
        modifier = modifier,
        mcpServers = state.mcpServers,
        searchToolEnabled = state.searchToolEnabled,
        isGoogleAuthenticated = state.isGoogleAuthenticated,
        googleUserEmail = state.googleUserEmail,
        googleDriveEnabled = state.googleDriveEnabled,
        googleCalendarEnabled = state.googleCalendarEnabled,
        googleGmailEnabled = state.googleGmailEnabled,
        googleTasksEnabled = state.googleTasksEnabled,
        googleDocsEnabled = state.googleDocsEnabled,
        googleSheetsEnabled = state.googleSheetsEnabled,
        googleSlidesEnabled = state.googleSlidesEnabled,
        googleClientId = state.googleClientId,
        onEvent = state::tell
    )
}

@Composable
private fun WorkspaceToggle(
    label: String,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Switch(checked = enabled, onCheckedChange = onCheckedChange)
    }
}

@Composable
@Preview
private fun SettingsContentPreview() {
    SettingsContent()
}
