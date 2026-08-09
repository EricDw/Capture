package com.dewildte.capture.mcp

import android.util.Log
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.sse.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import io.modelcontextprotocol.kotlin.sdk.client.Client
import io.modelcontextprotocol.kotlin.sdk.client.StreamableHttpClientTransport
import io.modelcontextprotocol.kotlin.sdk.types.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.*

class AndroidMcpManager(
    private val scope: CoroutineScope
) : McpManager {

    private val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { 
                ignoreUnknownKeys = true 
                isLenient = true
            })
        }
        install(SSE)
    }

    private val connections = mutableMapOf<String, McpConnection>()

    override suspend fun connectToServer(
        url: String, 
        authProvider: (suspend () -> String?)?,
        onAuthError: (suspend () -> Unit)?
    ): McpConnection {
        val existing = connections[url]
        if (existing != null && authProvider == null) {
            return existing
        }
        val connection = AndroidMcpConnection(url, httpClient, authProvider, onAuthError)
        connections[url] = connection
        return connection
    }

    override fun getActiveConnections(): List<McpConnection> = connections.values.toList()

    override fun disconnectFromServer(url: String) {
        connections.remove(url)
    }
}

class AndroidMcpConnection(
    override val url: String,
    private val httpClient: HttpClient,
    private val authProvider: (suspend () -> String?)? = null,
    private val onAuthError: (suspend () -> Unit)? = null
) : McpConnection {

    private val client = Client(
        clientInfo = Implementation(name = "Capture-Android", version = "1.0")
    )

    private var isConnected = false

    private suspend fun ensureConnected() {
        if (isConnected) return

        try {
            val token = authProvider?.invoke()
            Log.d(TAG, "Connecting to MCP server at $url (Authenticated: ${token != null})")
            val transport = StreamableHttpClientTransport(
                client = httpClient,
                url = url,
                requestBuilder = {
                    // Fetch the token dynamically for each request (connection and tool calls)
                    Log.d("MCP_DEBUG", "RequestBuilder: Fetching token for $url")
                    val token = runBlocking { authProvider?.invoke() }
                    if (token != null) {
                        Log.d("MCP_DEBUG", "RequestBuilder: Adding Authorization header (starts with ${token.take(10)})")
                        header("Authorization", "Bearer $token")
                    } else {
                        Log.w("MCP_DEBUG", "RequestBuilder: No token available for $url")
                    }
                }
            )
            client.connect(transport)
            isConnected = true
            Log.d(TAG, "Successfully connected to MCP server at $url")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect to MCP server at $url", e)
            throw e
        }
    }

    override suspend fun getTools(): List<McpToolDefinition> {
        return try {
            ensureConnected()
            val response = client.listTools()
            Log.d(TAG, "Fetched ${response.tools.size} tools from $url")
            
            response.tools.map { tool ->
                McpToolDefinition(
                    name = tool.name,
                    description = tool.description ?: "",
                    parameters = emptyMap(), // Map inputSchema if needed
                    schema = Json.encodeToString(ToolSchema.serializer(), tool.inputSchema)
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching tools from $url", e)
            emptyList()
        }
    }

    override suspend fun callTool(name: String, arguments: Map<String, Any?>): String {
        return try {
            ensureConnected()
            val jsonArgs = JsonObject(arguments.mapValues { (_, v) -> 
                when(v) {
                    is String -> JsonPrimitive(v)
                    is Number -> JsonPrimitive(v)
                    is Boolean -> JsonPrimitive(v)
                    is JsonElement -> v
                    else -> JsonPrimitive(v.toString())
                }
            })

            try {
                val result = client.callTool(
                    CallToolRequest(
                        CallToolRequestParams(name, jsonArgs)
                    )
                )

                if (result.isError == true) {
                    val errorMsg = result.content.filterIsInstance<TextContent>().joinToString("\n") { it.text }
                    if (errorMsg.contains("missing required authentication credential") || errorMsg.contains("401")) {
                        Log.w(TAG, "Auth error detected, attempting refresh...")
                        onAuthError?.invoke()
                        // Retry once
                        return callTool(name, arguments)
                    }
                    "Error calling tool $name: $errorMsg"
                } else {
                    result.content.filterIsInstance<TextContent>().joinToString("\n") { it.text }
                }
            } catch (e: McpException) {
                if (e.message?.contains("authentication") == true || e.message?.contains("401") == true) {
                    Log.w(TAG, "McpException auth error, attempting refresh...")
                    onAuthError?.invoke()
                    // Retry once
                    return callTool(name, arguments)
                }
                throw e
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception calling tool $name on $url", e)
            "Error calling tool $name: ${e.message}"
        }
    }

    companion object {
        private const val TAG = "MCP_DEBUG"
    }
}
