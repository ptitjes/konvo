package io.github.ptitjes.konvo.backend.mcp

import io.github.ptitjes.konvo.core.ai.spi.Tool
import io.github.ptitjes.konvo.core.ai.spi.ToolParameters
import io.github.ptitjes.konvo.core.ai.spi.ToolProvider
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.client.*
import kotlinx.serialization.json.*
import io.modelcontextprotocol.kotlin.sdk.Tool as McpTool

class McpToolProvider(
    private val serversManager: McpServersManager,
    private val permissions: ToolPermissions?,
) : ToolProvider {
    override suspend fun queryTools(): List<Tool> {
        return serversManager.clients.flatMap { (clientName, client) ->
            client.listTools()?.tools?.map { tool ->
                buildKonvoTool(
                    clientName = clientName,
                    client = client,
                    tool = tool,
                    permissions = permissions,
                )
            } ?: emptyList()
        }
    }
}

private fun buildKonvoTool(
    clientName: String,
    client: Client,
    tool: McpTool,
    permissions: ToolPermissions?,
): Tool {
    val fullName = clientName + "_" + tool.name
    return Tool(
        name = fullName,
        description = tool.description ?: "",
        parameters = ToolParameters(
            properties = tool.inputSchema.properties.mapValues { (_, property) -> property.jsonObject },
            required = tool.inputSchema.required ?: emptyList(),
        ),
        askPermission = doesToolRequirePermission(fullName, permissions),
        evaluator = { arguments ->
            val result = client.callTool(tool.name, arguments)
            val content = result?.content?.joinToString("\n") { (it as? TextContent)?.text ?: "" } ?: ""
            if (result?.isError == true) error("MCP tool call failed: $content")
            content
        },
    )
}

private fun doesToolRequirePermission(toolName: String, permissions: ToolPermissions?): Boolean {
    if (permissions == null) return true
    for (rule in permissions.rules ?: emptyList()) {
        if (rule.pattern.matches(toolName)) {
            return rule.permission == ToolPermission.ASK
        }
    }
    return permissions.default == ToolPermission.ASK
}
