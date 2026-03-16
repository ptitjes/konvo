package io.github.ptitjes.konvo.plugin.core.tools

import io.github.ptitjes.konvo.plugin.core.mcp.*

class McpToolProvider(
    private val serversManager: McpServersManager,
    private val permissions: ToolPermissions?,
) : ToolProvider {
    override val name: String = "MCP"

    override suspend fun query(): List<ToolCard> {
        return serversManager.clients.flatMap { (clientName, client) ->
            val serverCapabilities = client.serverCapabilities
            if (serverCapabilities == null || serverCapabilities.tools == null) return@flatMap emptyList()

            client.listTools().tools.map { tool ->
                McpToolCard(
                    clientName = clientName,
                    client = client,
                    sdkTool = tool,
                    permissions = permissions,
                )
            }
        }
    }
}
