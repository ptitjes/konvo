package io.github.ptitjes.konvo.core.ai.mcp

import io.modelcontextprotocol.kotlin.sdk.client.*

class McpServersManager(private val specifications: Map<String, ServerSpecification>?) {
    private lateinit var handlers: Map<String, McpServerHandler>

    suspend fun startAndConnectServers() {
        handlers = specifications?.mapValues { (_, specification) ->
            McpServerHandler(specification).also { it.connect() }
        } ?: emptyMap()
    }

    suspend fun closeServers() {
        handlers.forEach { (_, handler) ->
            handler.close()
        }
    }

    val clients: Map<String, Client> get() = handlers.mapValues { (_, handler) -> handler.client }
}
