package com.dewildte.capture.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopAppBar(
    onMenuClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAiClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search your notes",
    showProfile: Boolean = false,
    showBackButton: Boolean = false,
    showNavActions: Boolean = true,
    showMenuIcon: Boolean = true,
    onBackClick: () -> Unit = {},
) {
    SearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = "",
                onQueryChange = {},
                onSearch = {},
                expanded = false,
                onExpandedChange = {},
                placeholder = { Text(placeholder) },
                leadingIcon = {
                    if (showBackButton) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    } else if (showMenuIcon) {
                        IconButton(onClick = onMenuClick) {
                            Icon(Icons.Default.Menu, contentDescription = "Toggle Drawer")
                        }
                    }
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (showNavActions) {
                            IconButton(onClick = onAiClick) {
                                Icon(Icons.Default.Bolt, contentDescription = "AI Assistant")
                            }
                            
                            IconButton(onClick = onSettingsClick) {
                                Icon(Icons.Default.Settings, contentDescription = "Settings")
                            }
                        }
                        
                        if (showProfile) {
                            IconButton(onClick = { /* User Profile */ }) {
                                Icon(Icons.Default.AccountCircle, contentDescription = "User Profile")
                            }
                        }
                    }
                },
            )
        },
        expanded = false,
        onExpandedChange = {},
        modifier = modifier
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        // Search results or history could go here
    }
}
