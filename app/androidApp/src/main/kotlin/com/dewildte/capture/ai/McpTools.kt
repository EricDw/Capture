package com.dewildte.capture.ai

import com.dewildte.capture.mcp.McpManager
import android.util.Log
import com.dewildte.capture.events.PermissionResult
import com.google.ai.edge.litertlm.Tool
import com.google.ai.edge.litertlm.ToolParam
import com.google.ai.edge.litertlm.ToolSet
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*

class McpTools(
    private val mcpManager: McpManager,
    private val aiManager: AndroidAiManager
) : ToolSet {

    @Tool(description = "Lists all registered MCP (Model Context Protocol) servers.")
    fun listMcpServers(): String {
        val connections = mcpManager.getActiveConnections()
        return if (connections.isEmpty()) {
            "No MCP servers are currently registered."
        } else {
            "Registered MCP servers:\n" + connections.joinToString("\n") { "- ${it.url}" }
        }
    }

    @Tool(description = "Lists all available tools on a specific MCP server. Returns names and descriptions only.")
    fun listMcpTools(
        @ToolParam(description = "The URL of the MCP server.")
        serverUrl: String
    ): String {
        Log.d(TAG, "AI requested tool list for: $serverUrl")
        return runBlocking {
            try {
                val connection = mcpManager.connectToServer(serverUrl)
                val tools = connection.getTools()
                if (tools.isEmpty()) {
                    Log.w(TAG, "No tools returned for: $serverUrl")
                    "No tools found on server $serverUrl."
                } else {
                    Log.d(TAG, "Returning ${tools.size} tools for: $serverUrl")
                    val result = StringBuilder("Available tools on $serverUrl:\n")
                    tools.forEach { tool ->
                        result.append("- ${tool.name}: ${tool.description}\n")
                    }
                    result.append("\nTo use a tool, first call 'getMcpToolSchema' to see its required parameters.")
                    result.toString()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error listing tools for $serverUrl", e)
                "Error listing tools for $serverUrl: ${e.message}"
            }
        }
    }

    @Tool(description = "Returns the formal JSON Schema for a specific MCP tool. Use this BEFORE calling a tool to know what parameters are required.")
    fun getMcpToolSchema(
        @ToolParam(description = "The URL of the MCP server.")
        serverUrl: String,
        @ToolParam(description = "The name of the tool.")
        toolName: String
    ): String {
        Log.d(TAG, "AI requested schema for: $toolName on $serverUrl")
        return runBlocking {
            try {
                val connection = mcpManager.connectToServer(serverUrl)
                val tools = connection.getTools()
                val tool = tools.find { it.name == toolName }
                if (tool != null) {
                    "Schema for $toolName:\n${tool.schema ?: "No schema provided."}"
                } else {
                    "Tool '$toolName' not found on server $serverUrl."
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching schema for $toolName", e)
                "Error fetching schema: ${e.message}"
            }
        }
    }

    @Tool(description = "Calls a specific tool on an MCP server.")
    fun callMcpTool(
        @ToolParam(description = "The URL of the MCP server.")
        serverUrl: String,
        @ToolParam(description = "The name of the tool to call.")
        toolName: String,
        @ToolParam(description = "The arguments for the tool as a JSON string.")
        argumentsJson: String
    ): String {
        Log.d(TAG, "AI calling tool $toolName on $serverUrl")
        return runBlocking {
            aiManager.reportProgress(toolName, started = true)
            var success = false
            try {
                // HIL Permission Check
                val permission = aiManager.requestPermission(toolName, argumentsJson)
                if (permission == PermissionResult.DENY) {
                    return@runBlocking "User denied permission to call tool $toolName."
                }

                val connection = mcpManager.connectToServer(serverUrl)
                val args = Json.decodeFromString<Map<String, JsonElement>>(argumentsJson)
                val simpleArgs = args.mapValues { (_, v) -> 
                    if (v is JsonPrimitive) {
                        if (v.isString) v.content else v.toString()
                    } else {
                        v.toString()
                    }
                }
                val result = connection.callTool(toolName, simpleArgs)
                Log.d(TAG, "Tool call $toolName completed")
                success = !result.startsWith("Error")
                result
            } catch (e: Exception) {
                Log.e(TAG, "Error calling tool $toolName on $serverUrl", e)
                "Error calling tool $toolName on $serverUrl: ${e.message}"
            } finally {
                aiManager.reportProgress(toolName, started = false, success = success)
            }
        }
    }

    companion object {
        private const val TAG = "MCP_TOOLS"
    }
}
