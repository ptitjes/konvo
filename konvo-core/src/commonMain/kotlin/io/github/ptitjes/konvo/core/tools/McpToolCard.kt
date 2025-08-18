package io.github.ptitjes.konvo.core.tools

import ai.koog.agents.mcp.*
import io.github.ptitjes.konvo.core.mcp.*
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.client.*
import kotlinx.serialization.json.*

internal class McpToolCard(
    val clientName: String,
    val client: Client,
    val tool: Tool,
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

    override suspend fun toTool(): ai.koog.agents.core.tools.Tool<*, *> {
        val descriptor = DefaultMcpToolDescriptorParser.parse(tool)
        return McpTool(client, descriptor)
    }

    private fun doesToolRequirePermission(
        clientName: String,
        toolName: String,
        permissions: ToolPermissions?,
    ): Boolean {
        if (permissions == null) return true

        val fullName = "$clientName:$toolName"
        for (rule in permissions.rules ?: emptyList()) {
            if (rule.pattern.matches(fullName)) {
                return rule.permission == ToolPermission.ASK
            }
        }

        return permissions.default == ToolPermission.ASK
    }
}
