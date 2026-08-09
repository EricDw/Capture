package com.dewildte.capture.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dewildte.capture.events.PermissionResult
import kotlinx.serialization.json.*

@Composable
fun McpToolPermissionDialog(
    toolName: String,
    arguments: String,
    onResult: (PermissionResult) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onResult(PermissionResult.DENY) },
        title = {
            Text(
                text = "Tool Permission Request",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "The AI wants to call a tool:",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Tool Name",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = toolName, style = MaterialTheme.typography.bodySmall)
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Arguments",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    val formattedArguments = remember(arguments) {
                        try {
                            val json = Json { prettyPrint = true }
                            val element = json.parseToJsonElement(arguments)
                            json.encodeToString(JsonElement.serializer(), element)
                        } catch (e: Exception) {
                            arguments
                        }
                    }
                    Text(
                        text = formattedArguments,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = { onResult(PermissionResult.ALWAYS_ALLOW) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Always Allow")
                }
                Button(
                    onClick = { onResult(PermissionResult.ALLOW_ONCE) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Allow Once")
                }
                OutlinedButton(
                    onClick = { onResult(PermissionResult.DENY) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Don't Allow")
                }
            }
        }
    )
}
