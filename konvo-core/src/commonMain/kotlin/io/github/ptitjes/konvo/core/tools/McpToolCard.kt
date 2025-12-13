package io.github.ptitjes.konvo.core.tools

import ai.koog.agents.core.tools.*
import ai.koog.agents.mcp.*
import io.github.ptitjes.konvo.core.mcp.*
import io.modelcontextprotocol.kotlin.sdk.client.*
import kotlinx.serialization.json.*
import io.modelcontextprotocol.kotlin.sdk.Tool as SdkTool

internal class McpToolCard(
    val clientName: String,
    val client: Client,
    val sdkTool: SdkTool,
    val permissions: ToolPermissions?,
) : ToolCard {
    override val name: String get() = sdkTool.name
    override val description: String? get() = sdkTool.description
    override val parameters: ToolParameters
        get() = ToolParameters(
            properties = sdkTool.inputSchema.properties.mapValues { (_, property) -> property.jsonObject },
            required = sdkTool.inputSchema.required ?: emptyList(),
        )
    override val requiresVetting: Boolean
        get() = doesToolRequirePermission(clientName, sdkTool.name, permissions)

    override suspend fun toTool(): Tool<*, *> {
        val descriptor = DefaultMcpToolDescriptorParser.parse(sdkTool)
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
