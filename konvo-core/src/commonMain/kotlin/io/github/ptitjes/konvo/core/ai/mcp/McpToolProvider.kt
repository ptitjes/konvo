package io.github.ptitjes.konvo.core.ai.mcp

import ai.koog.agents.core.tools.*
import ai.koog.agents.mcp.*
import io.github.ptitjes.konvo.core.ai.spi.*
import io.modelcontextprotocol.kotlin.sdk.client.*
import kotlinx.serialization.json.*
import ai.koog.agents.mcp.McpTool as KoogMcpTool
import io.modelcontextprotocol.kotlin.sdk.Tool as McpTool

class McpToolProvider(
    private val serversManager: McpServersManager,
    private val permissions: ToolPermissions?,
) : ToolProvider {
    override suspend fun queryTools(): List<ToolCard> {
        return serversManager.clients.flatMap { (clientName, client) ->
            client.listTools()?.tools?.map { tool ->
                McpToolCard(
                    clientName = clientName,
                    client = client,
                    tool = tool,
                    permissions = permissions,
                )
            } ?: emptyList()
        }
    }

    private inner class McpToolCard(
        val clientName: String,
        val client: Client,
        val tool: McpTool,
        val permissions: ToolPermissions?,
    ) : ToolCard {
        override val name: String get() = tool.name
        override val description: String? get() = tool.description
        override val parameters: ToolParameters
            get() = ToolParameters(
                properties = tool.inputSchema.properties.mapValues { (_, property) -> property.jsonObject },
                required = tool.inputSchema.required ?: emptyList(),
            )
        override val requiresVetting: Boolean
            get() = doesToolRequirePermission(clientName, tool.name, permissions)

        override fun toTool(): Tool<*, *> {
            val descriptor = DefaultMcpToolDescriptorParser.parse(tool)
            return KoogMcpTool(client, descriptor)
        }
    }
}

private fun doesToolRequirePermission(clientName: String, toolName: String, permissions: ToolPermissions?): Boolean {
    if (permissions == null) return true

    val fullName = "$clientName:$toolName"
    for (rule in permissions.rules ?: emptyList()) {
        if (rule.pattern.matches(fullName)) {
            return rule.permission == ToolPermission.ASK
        }
    }

    return permissions.default == ToolPermission.ASK
}
