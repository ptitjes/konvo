package io.github.ptitjes.konvo.plugin.core.tools

import ai.koog.agents.core.annotation.*
import ai.koog.agents.core.tools.*
import ai.koog.agents.mcp.*
import io.github.ptitjes.konvo.plugin.core.mcp.*
import io.modelcontextprotocol.kotlin.sdk.client.*
import kotlinx.serialization.json.*
import io.modelcontextprotocol.kotlin.sdk.types.Tool as SdkTool

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
            properties = sdkTool.inputSchema.properties?.mapValues { (_, property) -> property.jsonObject },
            required = sdkTool.inputSchema.required,
        )
    override val requiresVetting: Boolean
        get() = doesToolRequirePermission(clientName, sdkTool.name, permissions)

    @OptIn(InternalAgentsApi::class)
    override fun toTool(): Tool<*, *> {
        val outputSchema = sdkTool.outputSchema
        val descriptor = DefaultMcpToolDescriptorParser.parse(sdkTool)

        return if (outputSchema != null) StructuredMcpTool(client, descriptor, metadata = emptyMap())
        else McpTool(client, descriptor, metadata = emptyMap())
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
