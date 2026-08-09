package com.dewildte.capture.mcp

interface McpManager {
    suspend fun connectToServer(
        url: String, 
        authProvider: (suspend () -> String?)? = null,
        onAuthError: (suspend () -> Unit)? = null
    ): McpConnection
    fun getActiveConnections(): List<McpConnection>
    fun disconnectFromServer(url: String)
}

interface McpConnection {
    val url: String
    suspend fun getTools(): List<McpToolDefinition>
    suspend fun callTool(name: String, arguments: Map<String, Any?>): String
}

data class McpToolDefinition(
    val name: String,
    val description: String,
    val parameters: Map<String, McpParameter>,
    val schema: String? = null
)

data class McpParameter(
    val type: String,
    val description: String,
    val required: Boolean = true
)
